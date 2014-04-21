package com.atlassian.instrumentation;

import junit.framework.TestCase;

/**
 *
 * @since v4.0
 */
public class TestExternalGauge extends TestCase
{

    private static class ExternalValueImpl implements ExternalValue
    {
        long invCount = 0;

        public long getValue()
        {
            return invCount++;
        }
    }


    public void testConstructor()
    {
        ExternalGauge gauge = new ExternalGauge("external", new ExternalValueImpl());
        assertEquals("external",gauge.getName());
        assertEquals(0,gauge.getValue());
        assertEquals(1,gauge.getValue());
        assertEquals(2,gauge.getValue());
        assertEquals(3,gauge.getValue());

        try
        {
            new ExternalCounter(null, new ExternalValueImpl());
            fail("Should have barfed on null name parameter");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new ExternalCounter("name", null);
            fail("Should have barfed on null external value parameter");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testReadOnly()
    {
        ExternalGauge gauge = new ExternalGauge("external", new ExternalValueImpl());
        try
        {
            gauge.addAndGet(34);
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }
        try
        {
            gauge.incrementAndGet();
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }
        try
        {
            gauge.addAndGet(-23);
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }
        try
        {
            gauge.decrementAndGet();
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }

    }
}