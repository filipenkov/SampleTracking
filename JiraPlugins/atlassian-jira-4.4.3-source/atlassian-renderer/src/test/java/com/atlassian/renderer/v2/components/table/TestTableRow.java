/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 9:25:25 AM
 */
package com.atlassian.renderer.v2.components.table;

import junit.framework.TestCase;

public class TestTableRow extends TestCase
{
    public void testSimpleCell()
    {
        TableRow row = TableRow.createRow("|foo|");
        TableRow rowNoEnding = TableRow.createRow("|foo");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo"));
        assertEquals(expected, row);
        assertEquals(expected, rowNoEnding);
    }

    public void testSimpleRow()
    {
        TableRow row = TableRow.createRow("|foo|bar|");
        TableRow rowNoEnding = TableRow.createRow("|foo|bar");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo"));
        expected.addCell(new TableCell("bar"));
        assertEquals(expected, row);
        assertEquals(expected, rowNoEnding);
    }

    public void testEscapedChars()
    {
        TableRow row = TableRow.createRow("|foo\\|bar|");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo\\|bar"));
        assertEquals(expected, row);
    }

    public void testHeaderCell()
    {
        TableRow row = TableRow.createRow("||foo||");
        TableRow rowShortEnding = TableRow.createRow("||foo|");
        TableRow rowNoEnding = TableRow.createRow("||foo");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo", true));
        assertEquals(expected, row);
        assertEquals(expected, rowShortEnding);
        assertEquals(expected, rowNoEnding);
    }

    public void testHeaderRow()
    {
        TableRow row = TableRow.createRow("||foo||bar||");
        TableRow rowSmallEnding = TableRow.createRow("||foo||bar|");
        TableRow rowNoEnding = TableRow.createRow("||foo||bar");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo", true));
        expected.addCell(new TableCell("bar", true));
        assertEquals(expected, row);
        assertEquals(expected, rowSmallEnding);
        assertEquals(expected, rowNoEnding);
    }

    public void testMixedRow()
    {
        TableRow row = TableRow.createRow("||foo|bar|");
        TableRow rowLongEnding = TableRow.createRow("||foo|bar||");
        TableRow rowNoEnding = TableRow.createRow("||foo|bar");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo", true));
        expected.addCell(new TableCell("bar"));
        assertEquals(expected, row);
        assertEquals(expected, rowLongEnding);
        assertEquals(expected, rowNoEnding);
    }

    public void testSplitRow()
    {
        TableRow row = TableRow.createRow("||foo\nbar|bar|");
        TableRow expected = new TableRow();
        expected.addCell(new TableCell("foo\nbar", true));
        expected.addCell(new TableCell("bar"));
        assertEquals(expected, row);
    }
}