package com.atlassian.instrumentation.operations.registry;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpFinder is as class that can search a OpRegistry and return OpSnapshot values in interesting and sorted ways
 *
 * @since v4.0
 */
public class OpFinder
{
    /**
     * This filter will always return true and hence includes ALL OpSnapshot values
     */
    public static final OpFinderFilter ALL = new OpFinderFilter()
    {
        public boolean filter(final OpSnapshot opSnapshot)
        {
            return true;
        }
    };

    private final OpRegistry opRegistry;

    public OpFinder(OpRegistry opRegistry)
    {
        notNull("opRegistry", opRegistry);
        this.opRegistry = opRegistry;
    }

    /**
     * This will return ALL values from the OpRegistry sorted into OpSnapshotComparator.BY_DEFAULT order.
     *
     * @return a sorted list of OpSnapshot objects.
     */
    public List /*<OpSnapshot>*/ find()
    {
        return find(ALL, OpSnapshotComparator.BY_DEFAULT);
    }

    /**
     * This will return ALL values in the OpRegistry sorted into the order distated by the comparator.
     *
     * @param comparator the comparator to use
     * @return a sorted list of OpSnapshot objects.
     */
    public List /*<OpSnapshot>*/ find(Comparator comparator)
    {
        notNull("comparator", comparator);
        return find(null, comparator);
    }

    /**
     * This will search an OpRegistry and find all OpSnapshot values that are suitable to a given filter and are sorted
     * by the comparator.
     *
     * @param filter a OpFinderFilter that will decide if a value is suitable for inclusion.  If it is null, then the
     * All fitler will be used
     * @param comparator a comparator used to sort the list.  If null, then the list will be sorted in
     * OpSnapshotComparator.BY_DEFAULT order
     * @return a sorted list of OpSnapshot objects.
     */
    public List<OpSnapshot> find(OpFinderFilter filter, Comparator comparator)
    {
        if (filter == null)
        {
            filter = ALL;
        }
        if (comparator == null)
        {
            comparator = OpSnapshotComparator.BY_DEFAULT;
        }
        final ConcurrentHashMap<String, OpCounter> map = opRegistry.mapOfOpCounters;
        List<OpSnapshot> snapshotList = new ArrayList<OpSnapshot>(map.size());
        for (final OpCounter opCounter : map.values())
        {
            OpSnapshot opSnapshot = opCounter.snapshot();
            boolean add = filter.filter(opSnapshot);
            if (add)
            {
                snapshotList.add(opSnapshot);
            }
        }
        Collections.sort(snapshotList, comparator);
        return snapshotList;
    }
}
