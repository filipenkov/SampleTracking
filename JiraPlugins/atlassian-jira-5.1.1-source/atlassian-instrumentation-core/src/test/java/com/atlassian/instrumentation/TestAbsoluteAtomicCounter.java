package com.atlassian.instrumentation;

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @since v4.0
 */
public class TestAbsoluteAtomicCounter extends TestCase
{
    public void testConstructor()
    {
        AbsoluteCounter absoluteCounter = new AbsoluteAtomicCounter("name", 666);
        assertEquals("name", absoluteCounter.getName());
        assertEquals(666, absoluteCounter.getValue());
        assertEquals(0, absoluteCounter.getValue());

        absoluteCounter = new AbsoluteAtomicCounter("name");
        assertEquals("name", absoluteCounter.getName());
        assertEquals(0, absoluteCounter.getValue());
        assertEquals(0, absoluteCounter.getValue());

        try
        {
            new AbsoluteAtomicCounter(null);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new AbsoluteAtomicCounter(null, 666);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new AbsoluteAtomicCounter("name", null);
            fail("Should have barfed on null long reference");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testChanges()
    {
        AbsoluteCounter AbsoluteCounter = new AbsoluteAtomicCounter("name", 666);
        assertEquals(666, AbsoluteCounter.getValue());
        assertEquals(0, AbsoluteCounter.getValue());
        assertEquals(1, AbsoluteCounter.incrementAndGet());
        assertEquals(1, AbsoluteCounter.getValue());
        assertEquals(3, AbsoluteCounter.addAndGet(3));
        assertEquals(23, AbsoluteCounter.addAndGet(20));
        assertEquals(23, AbsoluteCounter.getValue());
        assertEquals(0, AbsoluteCounter.getValue());
    }

    public void testWithReferenceToOutsideAtomicLong()
    {
        AtomicLong outsideLong = new AtomicLong(666);

        AbsoluteCounter AbsoluteCounter = new AbsoluteAtomicCounter("name", outsideLong);
        assertEquals("name", AbsoluteCounter.getName());
        assertEquals(666, AbsoluteCounter.getValue());
        assertEquals(0, AbsoluteCounter.getValue());

        outsideLong.incrementAndGet();
        outsideLong.incrementAndGet();
        assertEquals(2, AbsoluteCounter.getValue());
        assertEquals(0, AbsoluteCounter.getValue());

        outsideLong.addAndGet(20);
        assertEquals(20, AbsoluteCounter.getValue());
        assertEquals(0, AbsoluteCounter.getValue());

    }
}