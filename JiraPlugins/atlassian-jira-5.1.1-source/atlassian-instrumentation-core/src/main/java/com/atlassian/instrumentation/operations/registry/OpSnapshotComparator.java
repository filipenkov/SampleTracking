package com.atlassian.instrumentation.operations.registry;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.operations.OpSnapshot;

import java.util.Comparator;

/**
 * A comparator helper for comparing OpSnapshot objects
 */
public abstract class OpSnapshotComparator implements Comparator<OpSnapshot>
{
    /**
     * This will sort in lowest name / highest time taken / highest invocation count / highest result set size order
     */
    public static final OpSnapshotComparator BY_NAME = new OpSnapshotComparator()
    {
        public int compareThese(final OpSnapshot opSnapshot1, final OpSnapshot opSnapshot2)
        {
            return sortByName(opSnapshot1, opSnapshot2);
        }
    };

    /**
     * This will sort in highest time taken / highest invocation count / highest result set size order / lowest name
     * order
     */
    public static final OpSnapshotComparator BY_TIME_TAKEN = new OpSnapshotComparator()
    {
        public int compareThese(final OpSnapshot opSnapshot1, final OpSnapshot opSnapshot2)
        {
            return sortByTimeTaken(opSnapshot1, opSnapshot2);
        }
    };


    /**
     * This will sort in highest invocation count / highest time taken / highest result set size order / lowest name
     * order
     */
    public static final OpSnapshotComparator BY_INVOCATION_COUNT = new OpSnapshotComparator()
    {
        public int compareThese(final OpSnapshot opSnapshot1, final OpSnapshot opSnapshot2)
        {
            return sortByInvocation(opSnapshot1, opSnapshot2);
        }
    };

    /**
     * This will sort in highest result set size / highest invocation count / highest time taken / lowest name order
     */
    public static final OpSnapshotComparator BY_RESULT_SET_SIZE = new OpSnapshotComparator()
    {
        public int compareThese(final OpSnapshot opSnapshot1, final OpSnapshot opSnapshot2)
        {
            return sortByResultSetSize(opSnapshot1, opSnapshot2);
        }
    };


    /**
     * The default is BY_TIME_TAKEN.
     * <p/>
     * This will sort in highest time taken / highest invocation count / highest result set size order / lowest name
     * order
     */
    public static final OpSnapshotComparator BY_DEFAULT = BY_TIME_TAKEN;


    /**
     * Compares 2 OpSnapshot values to each other.
     * <p/>
     * This WILL throw  InvalidArgumentException any of the objects are null
     * <p/>
     * This WILL throw  ClassCastException of the two objects are not OpSnapshot objects
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    public int compare(final OpSnapshot opSnapshot1, final OpSnapshot opSnapshot2)
    {
        notNull("opSnapshot1", opSnapshot1);
        notNull("opSnapshot2", opSnapshot2);
        return compareThese(opSnapshot1, opSnapshot2);
    }

    /**
     * When this is called the null checks have been made and the objects have been cast.
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    protected abstract int compareThese(OpSnapshot opSnapshot1, OpSnapshot opSnapshot2);

    /**
     * This will sort in lowest name / highest time taken / highest invocation count / highest result set size order
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    protected int sortByName(OpSnapshot opSnapshot1, OpSnapshot opSnapshot2)
    {
        double rc = opSnapshot1.getName().compareTo(opSnapshot2.getName());
        if (rc == 0)
        {
            rc = opSnapshot1.compareTo(opSnapshot2);
        }
        return rc > 0 ? 1 : (rc < 0 ? -1 : 0);
    }

    /**
     * This will sort in highest time taken / highest invocation count / highest result set size order / lowest name
     * order
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    protected int sortByTimeTaken(OpSnapshot opSnapshot1, OpSnapshot opSnapshot2)
    {
        double rc = (opSnapshot2.getMillisecondsTaken() - opSnapshot1.getMillisecondsTaken());
        if (rc == 0)
        {
            rc = (opSnapshot2.getInvocationCount() - opSnapshot1.getInvocationCount());
            if (rc == 0)
            {
                rc = (opSnapshot2.getResultSetSize() - opSnapshot1.getResultSetSize());
                if (rc == 0)
                {
                    rc = opSnapshot1.getName().compareTo(opSnapshot2.getName());
                }
            }
        }
        return rc > 0 ? 1 : (rc < 0 ? -1 : 0);
    }


    /**
     * This will sort in highest invocation count / highest time taken / highest result set size order / lowest name
     * order
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    protected int sortByInvocation(OpSnapshot opSnapshot1, OpSnapshot opSnapshot2)
    {
        double rc = (opSnapshot2.getInvocationCount() - opSnapshot1.getInvocationCount());
        if (rc == 0)
        {
            rc = (opSnapshot2.getMillisecondsTaken() - opSnapshot1.getMillisecondsTaken());
            if (rc == 0)
            {
                rc = (opSnapshot2.getResultSetSize() - opSnapshot1.getResultSetSize());
                if (rc == 0)
                {
                    rc = opSnapshot1.getName().compareTo(opSnapshot2.getName());
                }
            }
        }
        return rc > 0 ? 1 : (rc < 0 ? -1 : 0);
    }

    /**
     * This will sort in highest result set size / highest invocation count / highest time taken / lowest name order
     *
     * @param opSnapshot1 - MUST be a non null OpSnapshot object
     * @param opSnapshot2 - MUST be a non null OpSnapshot object
     * @return -1, 0 or 1 as per the Comparator interface
     */
    protected int sortByResultSetSize(OpSnapshot opSnapshot1, OpSnapshot opSnapshot2)
    {
        double rc = (opSnapshot2.getResultSetSize() - opSnapshot1.getResultSetSize());
        if (rc == 0)
        {
            rc = (opSnapshot2.getInvocationCount() - opSnapshot1.getInvocationCount());
            if (rc == 0)
            {
                rc = (opSnapshot2.getMillisecondsTaken() - opSnapshot1.getMillisecondsTaken());
                if (rc == 0)
                {
                    rc = opSnapshot1.getName().compareTo(opSnapshot2.getName());
                }
            }
        }
        return rc > 0 ? 1 : (rc < 0 ? -1 : 0);
    }

}
