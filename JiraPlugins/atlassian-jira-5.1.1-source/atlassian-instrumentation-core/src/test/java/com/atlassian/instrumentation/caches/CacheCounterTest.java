package com.atlassian.instrumentation.caches;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 */
public class CacheCounterTest extends TestCase
{
    private CacheCounter cacheCounter;

    @Override
    protected void setUp() throws Exception
    {
        cacheCounter = new CacheCounter("name");
    }

    public void testHit() throws Exception
    {
        assertEquals(0.0, cacheCounter.getHitMissRatio());

        for (int i = 0; i < 10; i++)
        {
            cacheCounter.hit();
        }
        assertEquals(10, cacheCounter.getHits());
        assertEquals(0, cacheCounter.getMisses());
        assertEquals(0, cacheCounter.getValue());
        assertEquals(1.0, cacheCounter.getHitMissRatio());
    }

    public void testMiss() throws Exception
    {
        assertEquals(0.0, cacheCounter.getHitMissRatio());

        for (int i = 0; i < 20; i++)
        {
            cacheCounter.hit();
        }
        for (int i = 0; i < 5; i++)
        {
            cacheCounter.miss();
        }
        assertEquals(20, cacheCounter.getHits());
        assertEquals(5, cacheCounter.getMisses());
        assertEquals(5, cacheCounter.getValue());
        assertEquals(0.8, cacheCounter.getHitMissRatio());
    }

    public void testMissTimeCallable() throws Exception
    {
        assertEquals(0.0, cacheCounter.getHitMissRatio());

        String retVal = cacheCounter.miss(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "straightthru";
            }
        });

        assertEquals("straightthru", retVal);

        assertEquals(1, cacheCounter.getMisses());
        assertEquals(0.0, cacheCounter.getHitMissRatio());


        final RuntimeException runtimeException = new RuntimeException("Should be left as is");
        try
        {
            cacheCounter.miss(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    throw runtimeException;
                }
            });
            fail("Didn't throw exception??");
        }
        catch (RuntimeException rte)
        {
            assertSame(runtimeException, rte);
        }
        assertEquals(2, cacheCounter.getMisses());

        try
        {
            cacheCounter.miss(new Callable<Double>()
            {
                @Override
                public Double call() throws Exception
                {
                    throw new IOException("Gets wrapped");
                }
            });
            fail("Didn't throw exception??");
        }
        catch (RuntimeException rte)
        {
        }
        assertEquals(3, cacheCounter.getMisses());

    }

}
