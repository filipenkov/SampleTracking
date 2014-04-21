package com.atlassian.instrumentation.operations;

import junit.framework.TestCase;

/**
 */
public class TestExternalOpInstrument extends TestCase
{
    public void testConstruction()
    {
        try
        {
            new ExternalOpInstrument(null, new EOIImpl());
            fail("Should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            new ExternalOpInstrument("name", null);
            fail("Should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testValues()
    {
        ExternalOpInstrument externalOpInstrument = new ExternalOpInstrument("myName", new EOIImpl());
        assertEquals("myName", externalOpInstrument.getName());
        assertEquals(333, externalOpInstrument.getInvocationCount());
        assertEquals(666, externalOpInstrument.getMillisecondsTaken());
        assertEquals(999, externalOpInstrument.getResultSetSize());

        assertEquals(666, externalOpInstrument.getValue());
    }

    public void testComparator()
    {
        ExternalOpInstrument externalOpInstrument1 = new ExternalOpInstrument("myName", new EOIImpl(1));
        ExternalOpInstrument externalOpInstrument2 = new ExternalOpInstrument("myName", new EOIImpl(2));
        ExternalOpInstrument externalOpInstrument3 = new ExternalOpInstrument("myName", new EOIImpl(1));

        assertTrue(externalOpInstrument1.compareTo(externalOpInstrument2) > 0);
        assertTrue(externalOpInstrument2.compareTo(externalOpInstrument1) < 0);
        assertTrue(externalOpInstrument1.compareTo(externalOpInstrument3) == 0);
    }

    private static class EOIImpl implements ExternalOpValue
    {
        private final int multiplier;

        private EOIImpl()
        {
            this(1);
        }

        private EOIImpl(int multiplier)
        {
            this.multiplier = multiplier;
        }

        public OpSnapshot getSnapshot()
        {
            return new OpSnapshot("name", multiplier*333, multiplier*666, multiplier*999, multiplier*1212);
        }
    }
}
