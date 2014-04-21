package com.atlassian.instrumentation;

import com.atlassian.instrumentation.caches.CacheCounter;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpSnapshot;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.SimpleOpTimer;
import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A test case for InstrumentRegistry
 *
 * @since v4.0
 */
public class TestDefaultInstrumentRegistry extends TestCase
{
    public void testConstructor()
    {
        InstrumentRegistry registry = new DefaultInstrumentRegistry();
        List<Instrument> instruments = registry.snapshotInstruments();
        assertNotNull(instruments);
        assertEquals(0, instruments.size());
    }

    public void testGetAndPut()
    {
        Counter counter = new AtomicCounter("name");

        InstrumentRegistry registry = new DefaultInstrumentRegistry();
        assertNull(registry.putInstrument(counter));
        assertSame(counter, registry.getInstrument("name"));
    }

    public void testPull()
    {
        InstrumentRegistry registry = new DefaultInstrumentRegistry();

        AbsoluteCounter absoluteCounter = registry.pullAbsoluteCounter("absolute.counter");
        assertNotNull(absoluteCounter);
        assertSame(absoluteCounter, registry.pullAbsoluteCounter("absolute.counter"));

        Counter counter = registry.pullCounter("counter");
        assertNotNull(counter);
        assertSame(counter, registry.pullCounter("counter"));

        DerivedCounter derivedCounter = registry.pullDerivedCounter("derived.counter");
        assertNotNull(derivedCounter);
        assertSame(derivedCounter, registry.pullDerivedCounter("derived.counter"));

        Gauge gauge = registry.pullGauge("gauge");
        assertNotNull(gauge);
        assertSame(gauge, registry.pullGauge("gauge"));

        CacheCounter cacheCounter = registry.pullCacheCounter("cache.counter");
        assertNotNull(cacheCounter);
        assertSame(cacheCounter, registry.pullCacheCounter("cache.counter"));

        OpCounter opCounter = registry.pullOpCounter("op.counter");
        assertNotNull(opCounter);
        assertSame(opCounter, registry.pullOpCounter("op.counter"));

        // slightly different behaviour
        OpTimer opTimer = registry.pullTimer("timer");
        assertNotNull(opTimer);
        assertNotNull("timer", opTimer.getName());
        assertNotSame(opTimer, registry.pullTimer("timer"));
        // we should now have a OpCounter in the registry for that named timer
        Instrument underlyingOpCounter = registry.getInstrument("timer");
        assertNotNull(underlyingOpCounter);
        assertTrue(underlyingOpCounter instanceof OpCounter);

        // test that a snapshot of the registry has all our stuff in it
        List<Instrument> instruments = registry.snapshotInstruments();
        assertNotNull(instruments);
        assertEquals(7, instruments.size());
        assertTrue(instruments.contains(absoluteCounter));
        assertTrue(instruments.contains(counter));
        assertTrue(instruments.contains(derivedCounter));
        assertTrue(instruments.contains(gauge));
        assertTrue(instruments.contains(cacheCounter));
        assertTrue(instruments.contains(opCounter));
        assertTrue(instruments.contains(underlyingOpCounter));

    }


    public void testTimerWorks() throws InterruptedException
    {
        InstrumentRegistry registry = new DefaultInstrumentRegistry();
        OpTimer timer = registry.pullTimer("timer");

        // wait  a bit
        Thread.sleep(1500);
        timer.end(666);

        OpCounter opCounter = (OpCounter) registry.getInstrument("timer");
        assertNotNull(opCounter);
        assertEquals(1, opCounter.getInvocationCount());
        assertEquals(666, opCounter.getResultSetSize());
        assertTrue(opCounter.getMillisecondsTaken() > 0);

    }

    public void testTimerFactoryWorks() throws InterruptedException
    {
        final AtomicBoolean factoryCalled = new AtomicBoolean(false);
        final InstrumentRegistry registry = new DefaultInstrumentRegistry(new OpTimerFactory()
        {
            public OpTimer createOpTimer(final String name, final boolean captureCPUCost, final OpTimer.OnEndCallback endCallback)
            {
                factoryCalled.set(true);
                return new SimpleOpTimer(name, captureCPUCost, endCallback);
            }
        },new DefaultRegistryConfiguration());
        OpTimer timer = registry.pullTimer("timer");

        // wait  a bit
        Thread.sleep(1500);
        timer.end(666);

        OpCounter opCounter = (OpCounter) registry.getInstrument("timer");
        assertNotNull(opCounter);
        assertEquals(1, opCounter.getInvocationCount());
        assertEquals(666, opCounter.getResultSetSize());
        assertTrue(opCounter.getMillisecondsTaken() > 0);

        assertTrue(factoryCalled.get());
    }

    public void testTimerAndOpCounterBackdoor() throws InterruptedException
    {
        // you can get an OpCounter directly by name or via a Timer
        // but they are the same thing
        InstrumentRegistry registry = new DefaultInstrumentRegistry();
        OpCounter opCounter = registry.pullOpCounter("some.name");
        assertEquals(0, opCounter.getInvocationCount());
        assertEquals(0, opCounter.getMillisecondsTaken());
        assertEquals(0, opCounter.getResultSetSize());

        OpTimer timer = registry.pullTimer("some.name");
        Thread.sleep(250);
        OpSnapshot opSnapshot = timer.end(666);

        OpCounter opCounterAgain = registry.pullOpCounter("some.name");
        assertSame(opCounter, opCounterAgain);

        assertEquals(opSnapshot.getInvocationCount(), opCounter.getInvocationCount());
        assertEquals(opSnapshot.getMillisecondsTaken(), opCounter.getMillisecondsTaken());
        assertEquals(opSnapshot.getResultSetSize(), opCounter.getResultSetSize());


    }

    public void testUniqueNames()
    {
        InstrumentRegistry registry = new DefaultInstrumentRegistry();
        Instrument fakeInstrument = new Instrument()
        {
            public String getName()
            {
                return "name.clash";
            }

            public long getValue()
            {
                return 0;
            }

            public int compareTo(final Instrument o)
            {
                return 0;
            }
        };
        registry.putInstrument(fakeInstrument);

        try
        {
            registry.pullAbsoluteCounter("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullCounter("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullDerivedCounter("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullGauge("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullOpCounter("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullTimer("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            registry.pullCacheCounter("name.clash");
            fail("Should have barfed on name clash");
        }
        catch (IllegalArgumentException ignored) {}

    }
}
