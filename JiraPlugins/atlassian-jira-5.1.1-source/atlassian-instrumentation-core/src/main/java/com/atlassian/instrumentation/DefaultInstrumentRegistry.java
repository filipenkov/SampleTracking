package com.atlassian.instrumentation;

import com.atlassian.instrumentation.caches.CacheCounter;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpSnapshot;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.SimpleOpTimerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

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
 * THREAD SAFETY : ConcurrentHashMap is used under the covers to ensure that @{link Instrument}s are created once and
 * only once.
 */
public class DefaultInstrumentRegistry implements InstrumentRegistry
{
    private static final SimpleOpTimerFactory SIMPLE_OPTIMER_FACTORY = new SimpleOpTimerFactory();
    private final ConcurrentHashMap<String, Instrument> mapOfInstruments = new ConcurrentHashMap<String, Instrument>();
    private final OpTimerFactory opTimerFactory;
    private final RegistryConfiguration registryConfiguration;

    /**
     * Creates a default implementation that uses a {@link SimpleOpTimerFactory}
     */
    public DefaultInstrumentRegistry()
    {
        this(SIMPLE_OPTIMER_FACTORY, new DefaultRegistryConfiguration());
    }

    /**
     * Creates an implementation that used the passed in OpTimerFactory
     *
     * @param opTimerFactory        the factory to use
     * @param registryConfiguration the configuration for the registry
     */
    public DefaultInstrumentRegistry(final OpTimerFactory opTimerFactory, final RegistryConfiguration registryConfiguration)
    {
        this.registryConfiguration = notNull("registryConfiguration", registryConfiguration);
        this.opTimerFactory = notNull("opTimerFactory", opTimerFactory);
    }

    @Override
    public RegistryConfiguration getRegistryConfiguration()
    {
        return registryConfiguration;
    }

    /**
     * This will put an {@link Instrument} into the registry, ATOMICALLY overwriting any previous {@link Instrument}
     * value.
     *
     * @param instrument the NON NULL instrument to add
     * @return the previous Instrument value
     */
    public Instrument putInstrument(Instrument instrument)
    {
        notNull("instrument", instrument);
        return mapOfInstruments.put(instrument.getName(), instrument);
    }

    /**
     * Returns an {@link Instrument} from the registry with the specified name
     *
     * @param name the name of the Instrument to return
     * @return an Instrument or null if there isn't one with that name
     */
    public Instrument getInstrument(String name)
    {
        notNull("name", name);
        return mapOfInstruments.get(name);
    }

    /*
     Checks that instruments that have been found in the map are in fact uniquely named and of the right type
     */
    private void checkInstrumentType(Instrument instrument, Class instrumentClass)
    {
        notNull("instrumentClass", instrumentClass);
        if (instrument != null && !(instrumentClass.isAssignableFrom(instrument.getClass())))
        {
            throw new NamedInstrumentException(instrument.getName());
        }
    }

    /**
     * This will be called if a {@link com.atlassian.instrumentation.Instrument} may be put into the map of known
     * instruments.  This follows the {@link java.util.concurrent.ConcurrentMap#putIfAbsent(Object, Object)} semantics
     *
     * @param name           the name of the instrument
     * @param possiblyNeeded the possible needed Instrument
     * @return the old value in the internal map
     */
    protected Instrument putIfAbsent(String name, Instrument possiblyNeeded)
    {
        return mapOfInstruments.putIfAbsent(name, possiblyNeeded);
    }

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
    public AbsoluteCounter pullAbsoluteCounter(String name)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            AbsoluteCounter possiblyNeeded = new AbsoluteAtomicCounter(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, AbsoluteCounter.class);
        return (AbsoluteCounter) instrument;
    }

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
    public Counter pullCounter(String name)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            Counter possiblyNeeded = new AtomicCounter(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, Counter.class);
        return (Counter) instrument;
    }

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
    public DerivedCounter pullDerivedCounter(String name)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            DerivedCounter possiblyNeeded = new DerivedAtomicCounter(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, DerivedCounter.class);
        return (DerivedCounter) instrument;
    }

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
    public Gauge pullGauge(String name)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            Gauge possiblyNeeded = new AtomicGauge(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, Gauge.class);
        return (Gauge) instrument;
    }

    @Override
    public CacheCounter pullCacheCounter(String name, CacheCounter.Sizer sizer)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            CacheCounter possiblyNeeded = new CacheCounter(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, CacheCounter.class);
        return (CacheCounter) instrument;
    }

    @Override
    public CacheCounter pullCacheCounter(String name)
    {
        return pullCacheCounter(name, CacheCounter.NOOP_SIZER);
    }


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
    public OpCounter pullOpCounter(String name)
    {
        Instrument instrument = getInstrument(name);
        if (instrument == null)
        {
            OpCounter possiblyNeeded = new OpCounter(name);
            instrument = putIfAbsent(name, possiblyNeeded);
            instrument = (instrument != null ? instrument : possiblyNeeded);
        }
        checkInstrumentType(instrument, OpCounter.class);
        return (OpCounter) instrument;
    }

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
    public OpTimer pullTimer(final String name)
    {
        final OpCounter opCounter = pullOpCounter(name);

        // now create a timer that puts its value into the created OpCounter
        return opTimerFactory.createOpTimer(name, registryConfiguration.isCPUCostCollected(), new AddToOpCounterCallback(opCounter));
    }

    /**
     * This returns a snapshot list of {@link Instrument} objects in the InstrumentRegistry at the time the method was
     * called.
     * <p/>
     * The list is MUTABLE and a copy of the underlying values.  So feel free to mutate it by sorting it and filtering
     * it.
     *
     * @return a mutable List of Instrument objects
     */
    public List<Instrument> snapshotInstruments()
    {
        return new ArrayList<Instrument>(mapOfInstruments.values());
    }

    private static class NamedInstrumentException extends IllegalArgumentException
    {
        private NamedInstrumentException(final String name)
        {
            super("An instrument of a different type with the name : '" + name + "already exists");
        }
    }

    private static class AddToOpCounterCallback implements OpTimer.OnEndCallback
    {
        private final OpCounter opCounter;

        public AddToOpCounterCallback(OpCounter opCounter) {this.opCounter = opCounter;}

        public void onEndCalled(OpSnapshot opSnapshot)
        {
            opCounter.add(opSnapshot);
        }
    }
}