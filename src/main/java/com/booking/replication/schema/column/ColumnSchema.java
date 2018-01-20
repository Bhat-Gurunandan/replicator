package com.booking.replication.schema.column;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bosko on 11/6/15.
 */
public class ColumnSchema {

    private String columnName;
    private String columnKey;
    private String characterSetName;
    private String columnType;
    private int ordinalPosition; // ColumnSchema position in the table
    private int characterMaximumLength;
    private boolean isNullable;

    public ColumnSchema() {

    }

    public ColumnSchema(ResultSet tableInfoResultSet) throws SQLException {
        this.setColumnName(tableInfoResultSet.getString("COLUMN_NAME"));
        this.setColumnKey(tableInfoResultSet.getString("COLUMN_KEY"));
        this.setColumnType(tableInfoResultSet.getString("COLUMN_TYPE"));
        this.setNullable(tableInfoResultSet.getBoolean("IS_NULLABLE"));
        this.setOrdinalPosition(tableInfoResultSet.getInt("ORDINAL_POSITION"));
        this.setCharacterSetName(tableInfoResultSet.getString("CHARACTER_SET_NAME"));
        this.setCharacterMaximumLength(tableInfoResultSet.getInt("CHARACTER_MAXIMUM_LENGTH"));
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    public int getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    public void setCharacterMaximumLength(int characterMaximumLength) {
        this.characterMaximumLength = characterMaximumLength;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        this.isNullable = nullable;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}
