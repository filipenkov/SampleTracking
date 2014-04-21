package com.atlassian.instrumentation;

import com.atlassian.instrumentation.caches.CacheCounter;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpTimer;

import java.util.List;

/**
 * This is a glorified Map/Set of {@link com.atlassian.instrumentation.Instrument}'s.
 * <p/>
 * It can create new {@link Instrument} objects directly via is pull() methods. This makes it easier to manage the
 * creation of performance statistics.
 * <p/>
 * Names of instruments MUST be unique.  For example you cannot have a {@link Counter} and a {@link Gauge} with the same
 * name.  If an Instrument exists with the a specified name and its not of the requested type, then an
 * IllegalStateException will be thrown.
 * <p/>
 * THREAD SAFETY : ConcurrentHashMap is sued under the covers to ensure that @{link Instrument}s are created once and
 * only once.
 *
 * @since v4.0
 */
public interface InstrumentRegistry
{

    /**
     * @return the underlying registry configuration
     */
    public RegistryConfiguration getRegistryConfiguration();

    /**
     * This will put an {@link Instrument} into the registry, atomically overwriting any previous {@link Instrument}
     * value.
     *
     * @param instrument the NON NULL instrument to add
     * @return the previous Instrument value
     */
    public Instrument putInstrument(Instrument instrument);

    /**
     * Returns an {@link Instrument} from the registry with the specified name
     *
     * @param name the name of the Instrument to return
     * @return an Instrument or null if there isn't one with that name
     */
    public Instrument getInstrument(String name);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a AbsoluteCounter under the name if its not
     * present OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the DerivedCounter to pull
     * @return a NON NULL DerivedCounter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a
     *                                  DerivedCounter
     */
    public AbsoluteCounter pullAbsoluteCounter(String name);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a Counter under the name if its not present
     * OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the Counter to pull
     * @return a NON NULL Counter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a Counter
     */
    public Counter pullCounter(String name);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a DerivedCounter under the name if its not
     * present OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the DerivedCounter to pull
     * @return a NON NULL DerivedCounter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a
     *                                  DerivedCounter
     */
    public DerivedCounter pullDerivedCounter(String name);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a Gauge under the name if its not present
     * OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the Gauge to pull
     * @return a NON NULL Gauge guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a Gauge
     */
    public Gauge pullGauge(String name);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a {@link CacheCounter} under the name if its not present
     * OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name  the name of the CacheCounter to pull
     * @param sizer the {@link com.atlassian.instrumentation.caches.CacheCounter.Sizer} to use that can work out the size of the cache
     * @return a NON NULL CacheCounter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a CacheCounter
     */
    public CacheCounter pullCacheCounter(final String name, final CacheCounter.Sizer sizer);

    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a {@link CacheCounter} under the name if its not present
     * OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the CacheCounter to pull
     * @return a NON NULL CacheCounter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a CacheCounter
     */
    public CacheCounter pullCacheCounter(final String name);


    /**
     * This is a pull through operation that guarantees to ATOMICALLY create an {@link OpCounter} under the name if its
     * not present OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the OpCounter to pull
     * @return a NON NULL OpCounter guaranteed
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a OpCounter
     */
    public OpCounter pullOpCounter(String name);


    /**
     * This is a pull through operation that guarantees to ATOMICALLY create a OpCounter under the name if its not
     * present OR use the previously created one if its there.  When {@link com.atlassian.instrumentation.operations.OpTimer#end()}
     * is called, the value of the timer will be placed inside the named OpCounter.
     * <p/>
     * You code can use it something like this
     * <pre>
     *  OpTimer timer = registry.pullOpTimer("some.operation");
     *  try {
     *      ..
     *      .. do some operation
     *      ..
     *  } finally {
     *    opTimer.end()
     *    // and OpCounter will now exist
     *    OpCounter opCounter = (OpCounter) registry.getInstrument("some.operation");
     *  }
     * </pre>
     * <p/>
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param name the name of the OpTimer to pull
     * @return a NON NULL OpTimer guaranteed that is ready and RUNNING.  Run Forrest Run!
     * @throws IllegalArgumentException if there is already a named Instrument in the registry but is not a {@link
     *                                  com.atlassian.instrumentation.operations.OpCounter}
     */
    public OpTimer pullTimer(String name);

    /**
     * This returns a snapshot list of {@link Instrument} objects in the InstrumentRegistry at the time the method was
     * called.
     * <p/>
     * The list is MUTABLE and a copy of the underlying values.  So feel free to mutate it by sorting it and filtering
     * it.
     *
     * @return a mutable List of Instrument objects
     */
    public List<Instrument> snapshotInstruments();
}