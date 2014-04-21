package com.atlassian.instrumentation;

import junit.framework.TestCase;

/**
 *
 * @since v4.0
 */
public class TestExternalCounter extends TestCase
{

    private static class ExternalInstrumentImpl implements ExternalValue
    {
        long invCount = 0;

        public long getValue()
        {
            return invCount++;
        }
    }

    
    public void testConstructor()
    {
        ExternalCounter counter = new ExternalCounter("external", new ExternalInstrumentImpl());
        assertEquals("external",counter.getName());
        assertEquals(0,counter.getValue());
        assertEquals(1,counter.getValue());
        assertEquals(2,counter.getValue());
        assertEquals(3,counter.getValue());

        try
        {
            new ExternalCounter(null, new ExternalInstrumentImpl());
            fail("Should have barfed on null parameter");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new ExternalCounter("name", null);
            fail("Should have barfed on null parameter");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    public void testReadOnly()
    {
        ExternalCounter counter = new ExternalCounter("name", new ExternalInstrumentImpl());
        try
        {
            counter.addAndGet(34);
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }
        try
        {
            counter.incrementAndGet();
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }
        try
        {
            counter.addAndGet(-23);
            fail("Should have barfed on mutate method");
        }
        catch (Exception ignored)
        {
        }

    }
}