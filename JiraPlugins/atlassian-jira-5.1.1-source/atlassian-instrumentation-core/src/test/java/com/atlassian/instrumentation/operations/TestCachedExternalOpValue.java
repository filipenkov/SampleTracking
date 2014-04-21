package com.atlassian.instrumentation.operations;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class TestCachedExternalOpValue extends TestCase
{
    private static final int NEIGHBOUR_OF_THE_BEAST = 664;
    private static final int OPPOSITE_THE_BEAST = 665;
    private static final int THE_BEAST = 666;

    public void testConstruction()
    {
        try
        {
            new TestCachedExternalOpValue.CountingCachedExternalOpValue(-1, TimeUnit.MILLISECONDS);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            new TestCachedExternalOpValue.CountingCachedExternalOpValue(10, null);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
        // and this should be cool
        new TestCachedExternalOpValue.CountingCachedExternalOpValue(10, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests that the value is lazily read and that it caches the value after that!
     */
    public void testLazyRead()
    {
        TestCachedExternalOpValue.CountingCachedExternalOpValue opValue = new TestCachedExternalOpValue.CountingCachedExternalOpValue();
        assertEquals(NEIGHBOUR_OF_THE_BEAST, opValue.getSnapshot().getInvocationCount());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getMillisecondsTaken());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getValue());
        assertEquals(THE_BEAST, opValue.getSnapshot().getResultSetSize());
    }

    /**
     * Tests that the value returned is in fact cached for some time and the old value is given out
     * until it expires.
     *
     * @throws InterruptedException never
     */
    public void testTimeout() throws InterruptedException
    {
        TestCachedExternalOpValue.CountingCachedExternalOpValue opValue = new TestCachedExternalOpValue.CountingCachedExternalOpValue(1000, TimeUnit.MILLISECONDS);
        assertEquals(NEIGHBOUR_OF_THE_BEAST, opValue.getSnapshot().getInvocationCount());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getMillisecondsTaken());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getValue());
        assertEquals(THE_BEAST, opValue.getSnapshot().getResultSetSize());

        Thread.sleep(50);

        assertEquals(NEIGHBOUR_OF_THE_BEAST, opValue.getSnapshot().getInvocationCount());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getMillisecondsTaken());
        assertEquals(OPPOSITE_THE_BEAST, opValue.getSnapshot().getValue());
        assertEquals(THE_BEAST, opValue.getSnapshot().getResultSetSize());

        Thread.sleep(1000);

        assertEquals(NEIGHBOUR_OF_THE_BEAST+1, opValue.getSnapshot().getInvocationCount());
        assertEquals(OPPOSITE_THE_BEAST+1, opValue.getSnapshot().getMillisecondsTaken());
        assertEquals(OPPOSITE_THE_BEAST+1, opValue.getSnapshot().getValue());
        assertEquals(THE_BEAST+1, opValue.getSnapshot().getResultSetSize());


        Thread.sleep(1200);
        assertEquals(NEIGHBOUR_OF_THE_BEAST+2, opValue.getSnapshot().getInvocationCount());
        assertEquals(OPPOSITE_THE_BEAST+2, opValue.getSnapshot().getMillisecondsTaken());
        assertEquals(OPPOSITE_THE_BEAST+2, opValue.getSnapshot().getValue());
        assertEquals(THE_BEAST+2, opValue.getSnapshot().getResultSetSize());
    }

    private class CountingCachedExternalOpValue extends CachedExternalOpValue
    {
        AtomicLong computeValueCount = new AtomicLong(NEIGHBOUR_OF_THE_BEAST);

        private CountingCachedExternalOpValue()
        {
        }

        private CountingCachedExternalOpValue(final long cacheTimeout, final TimeUnit cacheTimeoutUnits)
        {
            super(cacheTimeout, cacheTimeoutUnits);
        }

        protected OpSnapshot computeValue()
        {
            long value = computeValueCount.getAndIncrement();
            return new OpSnapshot("", value, value + 1, value + 2, value + 3);
        }
    }


}
