package com.atlassian.instrumentation.caches;

import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.compare.InstrumentComparator;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

/**
 * CacheCounter can track cache hits and misses and give out statistics about how the cache
 * is performing
 */
public class CacheCounter implements CacheInstrument
{
    /**
     * In order to find out how large a cache is, this callback interface is invoked
     * to give a "guesstimate" about how large the cache is at present.
     * <p/>
     * This does not need to be "perfect" especially if its very expensive to work out how big the
     * cache is.
     */
    public interface Sizer
    {
        /**
         * @return a "guesstimate" about how large the cache is at present
         */
        long getCacheSize();
    }

    /**
     * A sizer that always returns -1
     */
    public static final Sizer NOOP_SIZER = new Sizer()
    {
        @Override
        public long getCacheSize()
        {
            return -1;
        }
    };

    protected final String name;
    protected final AtomicLong hits;
    protected final AtomicLong misses;
    protected final AtomicLong missTime;

    protected final Sizer sizer;


    public CacheCounter(final String name)
    {
        this(name, NOOP_SIZER);
    }

    public CacheCounter(final String name, final Sizer sizer)
    {
        this.name = notNull("name", name);
        this.sizer = notNull("sizer", sizer);
        this.hits = new AtomicLong(0);
        this.misses = new AtomicLong(0);
        this.missTime = new AtomicLong(0);
    }

    /**
     * Call this to record a hit on the instrumented cache
     *
     * @return the number of hits for this instrumented cache
     */
    public long hit()
    {
        return hits.incrementAndGet();
    }

    /**
     * Call this to record a miss on the instrumented cache
     *
     * @return the number of misses for this instrumented cache
     */
    public long miss()
    {
        return misses.incrementAndGet();
    }

    /**
     * Call this to record a miss on the instrumented cache and how long it took
     * to create the object that was missed.  This allows you to know the true cost in
     * time for cache misses.
     *
     * @param millisecondsTaken the time in ms it took to create the missed cache object
     * @return the number of misses for this instrumented cache
     */
    public long miss(final long millisecondsTaken)
    {
        misses.incrementAndGet();
        return missTime.getAndAdd(millisecondsTaken);
    }

    /**
     * Call this to record a miss on the instrumented cache and how long it took
     * to create the object that was missed.  This allows you to know the true cost in
     * time for cache misses.
     * <p/>
     * This method takes a Callable that allows you to "create" the missed object and auto record the time it took to create the missed object.
     *
     * @param missObjectCreation the callable that creates the missed object
     * @return the number of misses for this instrumented cache
     */
    public <T> T miss(Callable<T> missObjectCreation)
    {
        long then = System.currentTimeMillis();
        try
        {
            return missObjectCreation.call();
        }
        catch (Exception e)
        {
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        finally
        {
            long delta = System.currentTimeMillis() - then;
            misses.incrementAndGet();
            missTime.getAndAdd(delta);

        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getValue()
    {
        return getMisses();
    }

    @Override
    public int compareTo(Instrument that)
    {
        return new InstrumentComparator().compare(this, that);
    }

    @Override
    public long getHits()
    {
        return hits.longValue();
    }

    @Override
    public long getMisses()
    {
        return misses.longValue();
    }

    @Override
    public long getMissTime()
    {
        return missTime.longValue();
    }

    @Override
    public double getHitMissRatio()
    {
        double hits = getHits();
        double misses = getMisses();
        if (hits + misses == 0)
        {
            return 0;
        }
        return hits / (hits + misses);
    }

    @Override
    public long getCacheSize()
    {
        return sizer.getCacheSize();
    }
}
