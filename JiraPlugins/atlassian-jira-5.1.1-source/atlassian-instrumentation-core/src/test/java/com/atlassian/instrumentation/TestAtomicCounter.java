package com.atlassian.instrumentation;

import junit.framework.TestCase;

/**
 *
 * @since v4.0
 */
public class TestAtomicCounter extends TestCase
{
    public void testConstructor()
    {
        Counter counter = new AtomicCounter("name", 666);
        assertEquals("name",counter.getName());
        assertEquals(666,counter.getValue());

        counter = new AtomicCounter("name");
        assertEquals("name",counter.getName());
        assertEquals(0,counter.getValue());

        try
        {
            new AtomicCounter(null);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new AtomicCounter(null, 666);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }try
        {
            new AtomicCounter("name", null);
            fail("Should have barfed on null atomic long");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testOnlyGoesUpwards()
    {
        Counter counter = new AtomicCounter("name", 666);
        assertEquals(666,counter.getValue());
        assertEquals(667,counter.incrementAndGet());
        assertEquals(667,counter.getValue());
        assertEquals(670,counter.addAndGet(3));

        try
        {
            counter.addAndGet(-23);
            fail("Should have barfed on negative parameter");
        }
        catch (IllegalArgumentException ignored)
        {
        }

    }
}
