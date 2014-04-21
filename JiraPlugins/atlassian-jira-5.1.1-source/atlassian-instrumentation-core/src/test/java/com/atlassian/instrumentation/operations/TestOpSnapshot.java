package com.atlassian.instrumentation.operations;

import junit.framework.TestCase;

/**
 * A test case for OpSnapshot
 *
 * @since v4.0
 */
public class TestOpSnapshot extends TestCase
{
    public void testConstruction()
    {
        OpSnapshot snapshot = new OpSnapshot("name", 7, 8, 9, 10);
        assertEquals("name", snapshot.getName());
        assertEquals(7, snapshot.getInvocationCount());
        assertEquals(8, snapshot.getMillisecondsTaken());
        assertEquals(8, snapshot.getValue());
        assertEquals(9, snapshot.getResultSetSize());
        assertEquals(10, snapshot.getCpuTime());

        snapshot = new OpSnapshot("name", 77, 88);
        assertEquals("name", snapshot.getName());
        assertEquals(77, snapshot.getInvocationCount());
        assertEquals(88, snapshot.getMillisecondsTaken());
        assertEquals(88, snapshot.getValue());
        assertEquals(0, snapshot.getResultSetSize());
        assertEquals(0, snapshot.getCpuTime());
    }

    public void testAdditionAndSubtraction()
    {
        OpSnapshot snapshot1 = new OpSnapshot("name1", 7, 8, 9, 10);
        OpSnapshot snapshot2 = new OpSnapshot("name2", 2, 2, 2, 2);
        OpSnapshot snapshot3 = snapshot1.add(snapshot2);
        OpSnapshot snapshot4 = snapshot1.substract(snapshot2);

        assertEquals("name1", snapshot3.getName());
        assertEquals(9, snapshot3.getInvocationCount());
        assertEquals(10, snapshot3.getMillisecondsTaken());
        assertEquals(10, snapshot3.getValue());
        assertEquals(11, snapshot3.getResultSetSize());
        assertEquals(12, snapshot3.getCpuTime());

        assertEquals("name1", snapshot4.getName());
        assertEquals(5, snapshot4.getInvocationCount());
        assertEquals(6, snapshot4.getMillisecondsTaken());
        assertEquals(6, snapshot4.getValue());
        assertEquals(7, snapshot4.getResultSetSize());
        assertEquals(8, snapshot4.getCpuTime());
    }

    public void testEqualsAndHashCode()
    {

        OpSnapshot snapshotDiff1 = new OpSnapshot("name1", 7, 8, 9, 10);
        OpSnapshot snapshotDiff2 = new OpSnapshot("name2", 2, 2, 2, 2);

        assertFalse(snapshotDiff1.equals(null));
        assertFalse(snapshotDiff1.equals(snapshotDiff2));
        assertFalse(snapshotDiff2.equals(snapshotDiff1));
        assertFalse(snapshotDiff1.hashCode() != snapshotDiff1.hashCode());

        OpSnapshot snapshotSame1 = new OpSnapshot("name", 7, 8, 9, 10);
        OpSnapshot snapshotSame2 = new OpSnapshot("name", 7, 8, 9, 10);

        assertTrue(snapshotSame1.equals(snapshotSame1));
        assertTrue(snapshotSame1.equals(snapshotSame2));
        assertTrue(snapshotSame2.equals(snapshotSame1));
        assertTrue(snapshotSame1.hashCode() == snapshotSame2.hashCode());

        assertFalse(new OpSnapshot("", 7, 8, 9, 10).equals(new OpSnapshot("", 7, 8, 10, 9)));
        assertFalse(new OpSnapshot("", 7, 8, 9, 10).equals(new OpSnapshot("", 7, 9, 10, 5)));
        assertFalse(new OpSnapshot("z", 7, 8, 9, 10).equals(new OpSnapshot("x", 7, 8, 9, 10)));
        assertTrue(new OpSnapshot("x", 7, 8, 9, 10).equals(new OpSnapshot("x", 7, 8, 9, 10)));
    }

    public void testCompareTo()
    {
        OpSnapshot snapshotDiff1 = new OpSnapshot("name1", 7, 8, 9, 10);
        OpSnapshot snapshotSame1 = new OpSnapshot("name1", 7, 8, 9, 10);
        OpSnapshot snapshotDiff2 = new OpSnapshot("name2", 2, 2, 2, 2);

        assertEquals(0, snapshotDiff1.compareTo(snapshotSame1));
        assertEquals(0, snapshotSame1.compareTo(snapshotDiff1));
        assertEquals(0, snapshotSame1.compareTo(snapshotSame1));

        assertTrue(snapshotDiff1.compareTo(snapshotDiff2) > 0);
        assertTrue(snapshotDiff2.compareTo(snapshotDiff1) < 0);
        assertTrue(snapshotDiff1.compareTo(null) < 0);

    }
}