package com.atlassian.instrumentation;

import junit.framework.TestCase;

/**
 *
 * @since v4.0
 */
public class TestAtomicGauge extends TestCase
{
    public void testConstructor()
    {
        Gauge gauge = new AtomicGauge("name", 666);
        assertEquals("name",gauge.getName());
        assertEquals(666,gauge.getValue());

        gauge = new AtomicGauge("name");
        assertEquals("name",gauge.getName());
        assertEquals(0,gauge.getValue());

        try
        {
            new AtomicGauge(null);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new AtomicGauge(null, 666);
            fail("Should have barfed on null name");
        }
        catch (IllegalArgumentException ignored)
        {
        }try
        {
            new AtomicGauge("name", null);
            fail("Should have barfed on null atomic long");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testChanges()
    {
        Gauge gauge = new AtomicGauge("name", 666);
        assertEquals(666,gauge.getValue());
        assertEquals(667,gauge.incrementAndGet());
        assertEquals(667,gauge.getValue());
        assertEquals(670,gauge.addAndGet(3));
        assertEquals(650,gauge.addAndGet(-20));
        assertEquals(670,gauge.addAndGet(20));
        assertEquals(669,gauge.decrementAndGet());
    }
}