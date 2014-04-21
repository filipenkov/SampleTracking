package com.atlassian.instrumentation;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class TestCachedExternalValue extends TestCase
{

    private static final int NEIGHBOUR_OF_THE_BEAST = 664;

    public void testConstruction()
    {
        try
        {
            new CountingCachedExternalValue(-1, TimeUnit.MILLISECONDS);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
        try
        {
            new CountingCachedExternalValue(10, null);
            fail("This should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
        // and this should be cool
        new CountingCachedExternalValue(10, TimeUnit.MILLISECONDS);
    }

    /**
     * Tests that the value is lazily read and that it caches the value after that!
     */
    public void testLazyRead()
    {
        CountingCachedExternalValue value = new CountingCachedExternalValue();
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());
    }

    /**
     * Tests that the value returned is in fact cached for some time and the old value is given out
     * until it expires.
     *
     * @throws InterruptedException never
     */
    public void testTimeout() throws InterruptedException
    {
        CountingCachedExternalValue value = new CountingCachedExternalValue(1000, TimeUnit.MILLISECONDS);
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());

        Thread.sleep(50);

        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST,value.getValue());

        Thread.sleep(1000);

        assertEquals(NEIGHBOUR_OF_THE_BEAST+1,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST+1,value.getValue());


        Thread.sleep(1200);
        assertEquals(NEIGHBOUR_OF_THE_BEAST+2,value.getValue());
        assertEquals(NEIGHBOUR_OF_THE_BEAST+2,value.getValue());
    }

    private class CountingCachedExternalValue extends CachedExternalValue
    {
        AtomicLong computeValueCount = new AtomicLong(NEIGHBOUR_OF_THE_BEAST);

        private CountingCachedExternalValue()
        {
        }

        private CountingCachedExternalValue(final long cacheTimeout, final TimeUnit cacheTimeoutUnits)
        {
            super(cacheTimeout, cacheTimeoutUnits);
        }

        protected long computeValue()
        {
            return computeValueCount.getAndIncrement();
        }
    }

    
}
