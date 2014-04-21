/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 9:29:21 AM
 */
package com.atlassian.renderer.v2.components.table;

import junit.framework.TestCase;

public class TestTableCell extends TestCase
{
    public void testCreateEquals()
    {
        TableCell c1 = new TableCell("foo");
        TableCell c2 = new TableCell("foo");
        assertEquals(c1, c2);
        TableCell c3 = new TableCell("bar");
        assertFalse(c1.equals(c3));

        TableCell h1 = new TableCell("bar", true);
        assertFalse(c3.equals(h1));
        TableCell h2 = new TableCell("bar", true);
        assertEquals(h1, h2);
    }

    public void testToString()
    {
        TableCell c = new TableCell("bar");
        assertEquals("|bar", c.toString());
        c = new TableCell("bar", true);
        assertEquals("||bar", c.toString());
    }
}