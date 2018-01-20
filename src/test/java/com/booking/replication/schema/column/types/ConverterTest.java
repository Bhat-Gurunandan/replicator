package com.booking.replication.schema.column.types;

import com.booking.replication.binlog.common.Cell;
import com.booking.replication.binlog.common.cell.*;
import com.booking.replication.schema.column.ColumnSchema;
import com.booking.replication.schema.exception.TableMapException;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class ConverterTest {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INTEGER CELLS

    @Test
    public void tinyintSignedCell() throws TableMapException {
        int x = -125; // inside single byte
        Cell c = TinyCell.valueOf(x);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("tinyint");

        assertEquals(Integer.toString(x), Converter.cellValueToString(c, s));
    }

    // TODO: Failing!!!
//    @Test
//    public void tinyintUnsignedCell() throws TableMapException {
//        int x = 225; // inside single byte
//        Cell c = TinyCell.valueOf(x);
//        ColumnSchema s = new ColumnSchema();
//        s.setColumnType("unsigned tinyint");
//
//        assertEquals(Integer.toString(x), Converter.cellValueToString(c, s));
//    }

    @Test
    public void smallintCell() throws TableMapException {
        int x = -30000;
        Cell c = ShortCell.valueOf(x);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("smallint");

        assertEquals(Integer.toString(x), Converter.cellValueToString(c, s));
    }

    @Test
    public void smallintUnsignedCell() {
        // TODO
    }

    @Test
    public void mediumintCell() throws TableMapException {
        int x = -2000000;
        Cell c = Int24Cell.valueOf(x);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("mediumint");

        assertEquals(Integer.toString(x), Converter.cellValueToString(c, s));
    }

    @Test
    public void mediumintUnsignedCell() {
        // TODO
    }

    @Test
    public void intCell() throws TableMapException {
        int x = -2000000000;
        Cell c = LongCell.valueOf(x);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("int");

        assertEquals(Integer.toString(x), Converter.cellValueToString(c, s));
    }

    @Test
    public void intUnsignedCell() {
        // TODO
    }

    @Test
    public void bigintCell() throws TableMapException {
        long x = -9000000000000000000L;
        Cell c = LongLongCell.valueOf(x);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("bigint");

        assertEquals(Long.toString(x), Converter.cellValueToString(c, s));
    }

    @Test
    public void bigintUnsignedCell() {
        // TODO
    }

    // TODO : Add tests for overflows

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REAL NUMBERS

    @Test
    public void doubleCell() throws TableMapException {
        Cell c = new DoubleCell(1.5);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("double");

        assertEquals("1.5", Converter.cellValueToString(c, s));
    }

    @Test
    public void floatCell() throws TableMapException {
        Cell c = new FloatCell((float)1.5);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("float");

        assertEquals("1.5", Converter.cellValueToString(c, s));
    }

    @Test
    public void decimalCell() throws TableMapException {
        BigDecimal num = new BigDecimal("123.45");
        Cell c = new DecimalCell(num, num.precision(), num.scale());
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("decimal");

        assertEquals(num.toString(), Converter.cellValueToString(c, s));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TIME AND DATE

    @Test
    public void datetimeCell() throws ParseException, TableMapException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("16/01/2018");
        Cell c = new DatetimeCell(d);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("datetime");

        assertEquals(d.toString(), Converter.cellValueToString(c, s));
    }

    @Test
    public void datetime2Cell() throws ParseException, TableMapException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("16/01/2018");
        Cell c = new Datetime2Cell(d);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("datetime");

        assertEquals(d.toString(), Converter.cellValueToString(c, s));
    }

    @Test
    public void dateCell() throws ParseException, TableMapException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("16/01/2018");
        Cell c = new DatetimeCell(d);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("date");

        assertEquals(d.toString(), Converter.cellValueToString(c, s));
    }

    @Test
    public void yearCell() throws TableMapException {
        int year = 2018;
        Cell c = YearCell.valueOf(year);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("year");

        assertEquals(Integer.toString(year), Converter.cellValueToString(c, s));
    }

    @Test
    public void timeCell() throws TableMapException {
        long epoch = 1000000;
        java.sql.Time time = new java.sql.Time(epoch);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("time");

        assertEquals(Long.toString(epoch), Converter.cellValueToString(new TimeCell(time), s));
    }

    @Test
    public void time2Cell() throws TableMapException {
        long epoch = 555555;
        java.sql.Time time = new java.sql.Time(epoch);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("time");

        assertEquals(Long.toString(epoch), Converter.cellValueToString(new Time2Cell(time), s));
    }

    @Test
    public void timestampCell() throws TableMapException {
        long epoch = 666666;
        java.sql.Timestamp t = new java.sql.Timestamp(epoch);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("timestamp");

        assertEquals(Long.toString(epoch), Converter.cellValueToString(new TimestampCell(t), s));
    }

    @Test
    public void timestamp2Cell() throws TableMapException {
        long epoch = 777777;
        java.sql.Timestamp t = new java.sql.Timestamp(epoch);
        ColumnSchema s = new ColumnSchema();
        s.setColumnType("timestamp");

        assertEquals(Long.toString(epoch), Converter.cellValueToString(new Timestamp2Cell(t), s));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BLOB VARIATIONS

    @Test
    public void blobWithTextSchema() {
        Cell textCell = new BlobCell(null);

        // TODO: Add test
    }

    @Test
    public void blobWithTinytextSchema() {
        // TODO
    }

    @Test
    public void blobWithMediumtextSchema() {
        // TODO
    }

    @Test
    public void blobWithLongtextSchema() {
        // TODO
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // STRING CELLS

    @Test
    public void stringToString() throws TableMapException {
        String testString = "test";
        Cell stringCell = StringCell.valueOf(testString.getBytes(StandardCharsets.UTF_8));
        ColumnSchema stringSchema = new ColumnSchema();
        stringSchema.setCharacterSetName("utf8");

        assertEquals(testString, Converter.cellValueToString(stringCell, stringSchema));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NULL
    
    @Test
    public void nullToString() throws TableMapException {
        Cell nullCell = NullCell.valueOf(0);
        ColumnSchema emptySchema = new ColumnSchema();

        assertEquals("NULL", Converter.cellValueToString(nullCell, emptySchema));
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CELL TYPE AND SCHEMA CONFLICTS

//    @Test(expected = Exception.class) // TODO: Specify exception type
//    public void doubleCellWithTextSchema() throws TableMapException {
//        Cell c = new DoubleCell(1.0);
//        ColumnSchema s = new ColumnSchema();
//        s.setColumnType("text");
//
//        Converter.cellValueToString(c, s);
//    }
}