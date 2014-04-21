/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 20, 2004
 * Time: 12:25:03 PM
 */
package com.atlassian.renderer.v2.components.table;

import junit.framework.TestCase;

public class TestTable extends TestCase
{
    public void testSimpleTable()
    {
        Table t = Table.createTable("|one|row|");
        Table expected = new Table();
        expected.addRow(TableRow.createRow("|one|row|"));
        assertEquals(expected, t);
    }

    public void testTwoRows()
    {
        Table t = Table.createTable("|one|row|\n|two|row");
        Table expected = new Table();
        expected.addRow(TableRow.createRow("|one|row|"));
        expected.addRow(TableRow.createRow("|two|row|"));
        assertEquals(expected, t);
    }

    public void testSplitRowedTable()
    {
        Table t = Table.createTable("|one\nsplit|row|\n|two|row");
        Table expected = new Table();
        expected.addRow(TableRow.createRow("|one\nsplit|row|"));
        expected.addRow(TableRow.createRow("|two|row|"));
        assertEquals(expected, t);
    }
}