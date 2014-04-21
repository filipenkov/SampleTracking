package com.atlassian.instrumentation.operations;

import junit.framework.TestCase;

/**
 * A test case for OpCounter
 *
 * @since v4.0
 */
public class TestOpCounter extends TestCase
{
    public void testConstruction()
    {
        OpCounter opCounter = new OpCounter("name", 7, 8, 9, 10);
        assertEquals("name", opCounter.getName());
        assertEquals(7, opCounter.getInvocationCount());
        assertEquals(8, opCounter.getMillisecondsTaken());
        assertEquals(8, opCounter.getValue());
        assertEquals(9, opCounter.getResultSetSize());
        assertEquals(10, opCounter.getCpuTime());

        opCounter = new OpCounter("name", 77, 88);
        assertEquals("name", opCounter.getName());
        assertEquals(77, opCounter.getInvocationCount());
        assertEquals(88, opCounter.getMillisecondsTaken());
        assertEquals(88, opCounter.getValue());
        assertEquals(0, opCounter.getResultSetSize());
        assertEquals(0, opCounter.getCpuTime());

        opCounter = new OpCounter(new OpSnapshot("name", 77, 88, 99, 1010));
        assertEquals("name", opCounter.getName());
        assertEquals(77, opCounter.getInvocationCount());
        assertEquals(88, opCounter.getMillisecondsTaken());
        assertEquals(88, opCounter.getValue());
        assertEquals(99, opCounter.getResultSetSize());
        assertEquals(1010, opCounter.getCpuTime());

        opCounter = new OpCounter("name", 66);
        assertEquals("name", opCounter.getName());
        assertEquals(1, opCounter.getInvocationCount());
        assertEquals(66, opCounter.getMillisecondsTaken());
        assertEquals(66, opCounter.getValue());
        assertEquals(0, opCounter.getResultSetSize());
        assertEquals(0, opCounter.getCpuTime());

        opCounter = new OpCounter("name");
        assertEquals("name", opCounter.getName());
        assertEquals(0, opCounter.getInvocationCount());
        assertEquals(0, opCounter.getMillisecondsTaken());
        assertEquals(0, opCounter.getValue());
        assertEquals(0, opCounter.getResultSetSize());
        assertEquals(0, opCounter.getCpuTime());

    }

    public void testAdditionAndSubtraction()
    {
        OpCounter opCounter1 = new OpCounter("name1", 7, 8, 9, 10);
        OpCounter opCounter2 = new OpCounter("name2", 2, 2, 2, 2);
        OpCounter opCounter3 = opCounter1.add(opCounter2);

        assertEquals("name1", opCounter3.getName());
        assertEquals(9, opCounter3.getInvocationCount());
        assertEquals(10, opCounter3.getMillisecondsTaken());
        assertEquals(10, opCounter3.getValue());
        assertEquals(11, opCounter3.getResultSetSize());
        assertEquals(12, opCounter3.getCpuTime());
    }

    public void testEqualsAndHashCode()
    {

        OpCounter diff1 = new OpCounter("name1", 7, 8, 9, 10);
        OpCounter diff2 = new OpCounter("name2", 2, 2, 2, 2);

        //noinspection ObjectEqualsNull
        assertFalse(diff1.equals(null));
        assertFalse(diff1.equals(diff2));
        assertFalse(diff2.equals(diff1));
        assertFalse(diff1.hashCode() != diff1.hashCode());

        OpCounter same1 = new OpCounter("name", 7, 8, 9, 10);
        OpCounter same2 = new OpCounter("name", 7, 8, 9, 10);

        assertTrue(same1.equals(same1));
        assertTrue(same1.equals(same2));
        assertTrue(same2.equals(same1));
        assertTrue(same1.hashCode() == same2.hashCode());

        assertFalse(new OpCounter("", 7, 8, 9, 0).equals(new OpCounter("", 7, 8, 10, 0)));
        assertFalse(new OpCounter("", 7, 8, 9, 0).equals(new OpCounter("", 7, 9, 10, 0)));
        assertFalse(new OpCounter("z", 7, 8, 9, 0).equals(new OpCounter("x", 7, 8, 9, 0)));
        assertTrue(new OpCounter("x", 7, 8, 9, 0).equals(new OpCounter("x", 7, 8, 9, 0)));
    }

    public void testCompareTo()
    {
        OpCounter diff1 = new OpCounter("name1", 7, 8, 9, 0);
        OpCounter same1 = new OpCounter("name1", 7, 8, 9, 0);
        OpCounter diff2 = new OpCounter("name2", 2, 2, 2, 0);
        OpCounter diff3 = new OpCounter("name2", 3, 2, 2, 0);
        OpCounter diff4 = new OpCounter("name2", 3, 2, 7, 0);

        assertEquals(0, diff1.compareTo(same1));
        assertEquals(0, same1.compareTo(diff1));
        assertEquals(0, same1.compareTo(same1));

        assertTrue(diff1.compareTo(diff2) > 0);
        assertTrue(diff2.compareTo(diff1) < 0);
        assertTrue(diff1.compareTo(null) < 0);

        assertTrue(diff2.compareTo(diff3) < 0);
        assertTrue(diff3.compareTo(diff2) > 0);

        assertTrue(diff3.compareTo(diff4) < 0);
        assertTrue(diff4.compareTo(diff3) > 0);

    }

}