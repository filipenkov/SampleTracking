package com.atlassian.instrumentation.operations.registry;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpSnapshot;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.instrumentation.operations.SimpleOpTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a glorified Map/Set of {@link com.atlassian.instrumentation.operations.OpCounter}'s that are in some way related.
 * <p/>
 * It can create new OpCounter objects directly via is pull() methods.  This makes it easier to manage the creation of
 * performance statistics.
 * <p/>
 * Its just a fraction easier to use than a raw map plus is has thread safety.
 *
 * @since v4.0
 */
public class OpRegistry
{
    // deliberately has package level protection for OpFinder
    final ConcurrentHashMap<String, OpCounter> mapOfOpCounters = new ConcurrentHashMap<String, OpCounter>();

    /**
     * Can be called to PUT a counter into the registry.  This has normal map-like behaviour.  The key used is the name
     * of the OpCounter
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param opCounter the counter in question - It must NOT be null
     * @return the previous OpCounter value
     */
    public OpCounter put(OpCounter opCounter)
    {
        notNull("opCounter", opCounter);
        return mapOfOpCounters.put(opCounter.getName(), opCounter);
    }

    /**
     * Returns the OpCounter under the specified name or null if its not present.  This has normal map behaviour.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param opName the name of the counter to retrieve.  It must not be null.
     * @return an OpCounter associated with the key or null
     */
    public OpCounter get(String opName)
    {
        notNull("opName", opName);
        return mapOfOpCounters.get(opName);
    }

    /**
     * Call this method to start an OpTimer.  When the end() method is called the OpSnapshot will be pulled into
     * OpRegistry.
     * <p/>
     * <pre>
     * OpTimer timer = registry.pullTimer("opName");
     * ...
     * ... // do some work
     * ...
     * timer.end();
     * //
     * // the timer values will now be pulled into the
     * // registry under the name "opName"
     * </pre>
     *
     * @param name the name of the operation
     * @return an running OpTimer.
     */
    public OpTimer pullTimer(final String name)
    {
        final OpRegistry that = this;
        //
        // a specific implementation of OpTimer that pulls the value into this registry
        return new SimpleOpTimer(name)
        {
            public void onEndCalled(final OpSnapshot opSnapshot)
            {
                that.add(opSnapshot);
            }
        };
    }

    /**
     * This is a pull through operation that guarantees to ATOMICALLy create an OpCounter under the name if its not
     * present OR return the previously created one if its there.
     * <p/>
     * THREAD-SAFE - Uses ConcurrentHashMap under the covers
     *
     * @param opName the name of the operation
     * @return a NON NULL OpCounter guaranteed
     */
    public OpCounter pull(String opName)
    {
        notNull("opName", opName);
        OpCounter possiblyNeeded = new OpCounter(opName);
        OpCounter prevValue = mapOfOpCounters.putIfAbsent(opName, possiblyNeeded);
        if (prevValue == null)
        {
            /// it must have been null before. so we need the newly created counter
            prevValue = possiblyNeeded;
        }
        return prevValue;
    }

    /**
     * This will "pull" a counter into the registry and add the value of the OpCounter to it.
     *
     * @param srcOpCounter the source OpCounter to get values from to
     * @return the OpCounter that was pulled from the OpRegistry with the values added to it
     */
    public OpCounter add(OpCounter srcOpCounter)
    {
        notNull("srcOpCounter", srcOpCounter);
        return add(srcOpCounter.snapshot());
    }

    /**
     * This will "pull" a counter into the registry and add the value of the OpCounter to it.
     *
     * @param srcOpSnapshot the source OpSnapshot to get values from to
     * @return the OpCounter that was pulled from the OpRegistry with the values added to it
     */
    public OpCounter add(OpSnapshot srcOpSnapshot)
    {
        notNull("srcOpSnapshot", srcOpSnapshot);
        OpCounter targetOpCounter = pull(srcOpSnapshot.getName());
        targetOpCounter.add(srcOpSnapshot);
        return targetOpCounter;
    }

    /**
     * This will "pull" all the counters out of the passed in OpRegistry and add the values to counters under the same
     * names in the current registry.  It uses "pull" to do this, and hence they will be created as necessary.
     * <p/>
     * THREAD-SAFETY - Since the srcOpRegistry is mutable, it can only get a snapshot of the other registry at a point
     * in time.
     *
     * @param srcOpRegistry the registry to copy values from
     */
    public void add(OpRegistry srcOpRegistry)
    {
        List<OpSnapshot> copy = srcOpRegistry.snapshot();
        for (final OpSnapshot opSnapshot : copy)
        {
            this.add(opSnapshot);
        }
    }

    /**
     * This will "pull" a List of OpSnapshot objects into the current registry
     *
     * @param opSnapshotList a List of OpSnapshot objects
     */
    public void add(List<OpSnapshot> opSnapshotList)
    {
        notNull("opSnapshotList", opSnapshotList);
        for (final OpSnapshot opSnapshot : opSnapshotList)
        {
            add(opSnapshot);
        }
    }

    /**
     * This returns a List of OpSnapshot objects that represent the values of the all the OpCounter's in the OpRegistry
     * at the time the method was called.
     * <p/>
     * The list is MUTABLE and a copy of the OpCounter values.  So feel free to mutate it by sorting it and filtering
     * it.
     *
     * @return a mutable List of OpSnapshot objects
     */
    public List<OpSnapshot> snapshot()
    {
        List<OpSnapshot> snapshotList = new ArrayList<OpSnapshot>(mapOfOpCounters.size());
        for (OpCounter opCounter : mapOfOpCounters.values())
        {
            snapshotList.add(opCounter.snapshot());
        }
        return snapshotList;
    }

    /**
     * This returns a List of OpSnapshot objects that represent the values of the all the OpCounter's in the OpRegistry
     * at the time the method was called and then clears the internal list of counters.
     * <p/>
     * The list is MUTABLE and a copy of the OpCounter values.  So feel free to mutate it by sorting it and filtering
     * it.
     *
     * @return a mutable List of OpSnapshot objects
     */
    public List<OpSnapshot> snapshotAndClear() {
        List<OpSnapshot> snapshot = snapshot();
        mapOfOpCounters.clear();
        return snapshot;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString())
                .append(" ")
                .append(mapOfOpCounters.toString());
        return sb.toString();
    }
}
