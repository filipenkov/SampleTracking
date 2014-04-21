package com.atlassian.instrumentation.caches;

import com.atlassian.instrumentation.Instrument;

/**
 * An instrument that counts cache hits and misses
 */
public interface CacheInstrument extends Instrument
{
    /**
     * @return the total number of misses that have been made on the underlying instrumented cache
     */
    long getMisses();

    /**
     * @return the total time it has take to create objects because of cache misses on the underlying instrumented cache
     */
    long getMissTime();

    /**
     * @return the total number of hits that have been made on the underlying instrumented cache
     */
    long getHits();

    /**
     * @return the size of the underlying instrumented cache
     */
    long getCacheSize();

    /**
     * @return the ration of hits divided by the misses on the underlying instrumented cache
     */
    double getHitMissRatio();
}
