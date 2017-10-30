package com.booking.replication.pipeline;

import com.booking.replication.binlog.EventPosition;
import com.booking.replication.binlog.event.*;
import com.booking.replication.pipeline.event.handler.TransactionException;
import com.booking.replication.schema.exception.TableMapException;
import com.booking.replication.sql.QueryInspector;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.BinlogEventV4HeaderImpl;
import com.google.code.or.binlog.impl.event.QueryEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.XidEvent;
import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by bosko on 11/10/15.
 */
public class CurrentTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentTransaction.class);

    public static final long FAKEXID = 0;

    private final UUID uuid = UUID.randomUUID();
    private long xid;
    private final Map<Long,String> tableID2Name = new HashMap<>();
    private final Map<Long, String> tableID2DBName = new HashMap<>();
    private RawBinlogEventQuery beginEvent = null;
    private RawBinlogEvent finishEvent = null;
    private boolean isRewinded = false;

    private RawBinlogEventTableMap firstMapEventInTransaction = null;
    private Queue<RawBinlogEvent> events = new LinkedList<>();

    private final Map<String, RawBinlogEventTableMap> currentTransactionTableMapEvents = new HashMap<>();

    public CurrentTransaction() {
    }

    public CurrentTransaction(RawBinlogEventQuery event) {
        if (!QueryEventType.BEGIN.equals(QueryInspector.getQueryEventType(event))) {
            throw new RuntimeException("Can't set beginEvent for transaction to a wrong event type: " + event);
        }
        beginEvent = event;
    }

    @Override
    public String toString() {
        String beginEventBinlogFilename = beginEvent == null ? null : EventPosition.getEventBinlogFileName(beginEvent);
        Long beginEventBinlogPosition = beginEvent == null ? null : EventPosition.getEventBinlogPosition(beginEvent);
        return "uuid: " + uuid +
                ", xid: " + xid +
                ", tableID2Name: " + tableID2Name +
                ", tableID2DBName: " + tableID2DBName +
                ", beginBinlogFileName: " + beginEventBinlogFilename +
                ", beginBinlogPosition: " + beginEventBinlogPosition +
                ", events size: " + events.size() +
                ", beginEvent: " + beginEvent +
                ", finishEvent: " + finishEvent +
                ", events:\n    - " + Joiner.on("\n    - ").join(events);
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getXid() {
        return xid;
    }

    void setXid(long xid) {
        this.xid = xid;
    }

    RawBinlogEventQuery getBeginEvent() {
        return beginEvent;
    }


    void setFinishEvent(RawBinlogEventXid finishEvent) throws TransactionException {
        // xa-capable engines block (InnoDB)
        if (this.finishEvent == null) {
            setXid(finishEvent.getXid());
            this.finishEvent = finishEvent;
            return;
        }
        if (isRewinded) {
            if (((RawBinlogEventXid) this.finishEvent).getXid() != finishEvent.getXid()) {
                throw new TransactionException(
                        "XidEvents must match if in rewinded transaction. Old: " +
                        this.finishEvent +
                        ", new: " +
                        finishEvent
                );
            }
        } else {
            throw new TransactionException("Trying to set a finish event twice. Old: " + this.finishEvent + ", new: " + finishEvent);
        }
    }

    void setFinishEvent(RawBinlogEventQuery finishEvent) throws TransactionException {
        // MyIsam block
        if (!QueryEventType.COMMIT.equals(QueryInspector.getQueryEventType(finishEvent))) {
            throw new TransactionException("Can't set finishEvent for transaction to a wrong event type: " + finishEvent);
        }
        if (this.finishEvent == null) {
            setXid(FAKEXID);
            this.finishEvent = finishEvent;
            return;
        }
        if (isRewinded) {
            BinlogPositionInfo oldFinishEventPosition = new BinlogPositionInfo(
                    this.finishEvent.getBinlogFilename(), this.finishEvent.getPosition()
            );
            BinlogPositionInfo newFinishEventPosition = new BinlogPositionInfo(
                    finishEvent.getBinlogFilename(),
                    finishEvent.getPosition()
            );
            try {
                if (BinlogPositionInfo.compare(oldFinishEventPosition, newFinishEventPosition) != 0) {
                    throw new TransactionException("XidEvents must match if in rewinded transaction. Old: " + this.finishEvent + ", new: " + finishEvent);
                }
            } catch (BinlogPositionComparationException e) {
                throw new TransactionException("Failed to compare old and new commit events. Old: " + this.finishEvent + ", new: " + finishEvent, e);
            }
        } else {
            throw new TransactionException("Trying to set a finish event twice. Old: " + this.finishEvent + ", new: " + finishEvent);
        }
    }

    RawBinlogEvent getFinishEvent() {
        return finishEvent;
    }

    boolean hasBeginEvent() {
        return ((beginEvent != null) && (beginEvent.getSql().equals("BEGIN")));
    }

    boolean hasFinishEvent() {
        return (finishEvent != null);
    }

    void addEvent(RawBinlogEvent event) {
        events.add(event);
    }

    boolean hasEvents() {
        return (events.peek() != null);
    }

    public Queue<RawBinlogEvent> getEvents() {
        return events;
    }

    long getEventsCounter() {
        return events.size();
    }

    void clearEvents() {
        events = new LinkedList<>();
    }

    void setBeginEventTimestamp(long timestamp) throws TransactionException {
        beginEvent.setTimestamp(timestamp);
    }

    void setBeginEventTimestampToFinishEvent() throws TransactionException {
        if (!hasFinishEvent()) {
            throw new TransactionException("Can't set timestamp to timestamp of finish event while no finishEvent exists");
        }
        setBeginEventTimestamp(finishEvent.getTimestamp());
    }

    void setEventsTimestamp(long timestamp) throws TransactionException {
        for (RawBinlogEvent event : events) {
           event.overrideTimestamp(timestamp);
        }
    }

    void setEventsTimestampToFinishEvent() throws TransactionException {
        if (!hasFinishEvent()) {
            throw new TransactionException("Can't set timestamp to timestamp of finish event while no finishEvent exists");
        }
        setEventsTimestamp(finishEvent.getTimestamp());
    }

    /**
     * Update table map cache.
     */
    public void updateCache(RawBinlogEventTableMap event) {
        LOGGER.debug("Updating cache. firstMapEventInTransaction: " + firstMapEventInTransaction + ", event: " + event);
        if (firstMapEventInTransaction == null) {
            firstMapEventInTransaction = event;
        }

        String tableName = event.getTableName().toString();

        tableID2Name.put(
                event.getTableId(),
                tableName
        );

        tableID2DBName.put(
                event.getTableId(),
                event.getDatabaseName().toString()
        );

        currentTransactionTableMapEvents.put(tableName, event);
    }

    /**
     * Map table id to table name.
     */
    public String getTableNameFromID(Long tableID) throws TableMapException {
        if (! tableID2DBName.containsKey(tableID)) {
            LOGGER.error(String.format(
                    "Table ID not known. Known tables and ids are: %s",
                    Joiner.on(" ").join(tableID2DBName.keySet(), " ")));
            throw new TableMapException("Table ID not present in CurrentTransaction!");
        }

        return tableID2Name.get(tableID);
    }

    /**
     * Map table id to schema name.
     */
    public String getDBNameFromTableID(Long tableID) throws TableMapException {
        String dbName = tableID2DBName.get(tableID);

        if (dbName == null) {
            throw new TableMapException("Table ID not present in CurrentTransaction!");
        } else {
            return dbName;
        }
    }

    public boolean isRewinded() {
        return isRewinded;
    }

    void setRewinded(boolean rewinded) {
        isRewinded = rewinded;
    }

    public RawBinlogEventTableMap getTableMapEvent(String tableName) {
        return currentTransactionTableMapEvents.get(tableName);
    }

    public RawBinlogEventTableMap getFirstMapEventInTransaction() {
        return firstMapEventInTransaction;
    }

    boolean hasMappingInTransaction() {
        return (firstMapEventInTransaction != null)
                && (firstMapEventInTransaction.getDatabaseName() != null)
                && (firstMapEventInTransaction.getTableName() != null);
    }

    Map<String, RawBinlogEventTableMap> getCurrentTransactionTableMapEvents() {
        return currentTransactionTableMapEvents;
    }

}
