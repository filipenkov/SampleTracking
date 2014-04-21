package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.compare.InstrumentComparator;
import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

/**
 * This is an snapshot instance of a <b>named</b> operation's timing, invocation count and its result set size.
 * <p/>
 * Its IMMUTABLE and hence THREAD safe.
 *
 * @since v4.0
 */
public final class OpSnapshot implements OpInstrument
{
    private final String name;
    private final long invocationCount;
    private final long millisecondsTaken;
    private final long resultSetSize;
    private final long cpuTime;

    /**
     * Constructs a OpSnapshot with the specified values.
     *
     * @param name The name of the operation.  Must not be null.
     * @param invocationCount how may times the operations was invoked
     * @param millisecondsTaken the total number of milliseconds taken for all invocations of the operation
     * @param resultSetSize the size of the "data" returned by the operation.
     * @param cpuTime the CPU time in nanoseconds
     */
    public OpSnapshot(final String name, final long invocationCount, final long millisecondsTaken, final long resultSetSize, final long cpuTime)
    {
        notNull("name", name);
        this.name = name;
        this.invocationCount = invocationCount;
        this.millisecondsTaken = millisecondsTaken;
        this.resultSetSize = resultSetSize;
        this.cpuTime = cpuTime;
    }

    /**
     * Constructs a OpSnapshot with the specified values and a resultSetSize of -1;
     *
     * @param name The name of the operation.  Must not be null.
     * @param invocationCount how may times the operations was invoked
     * @param millisecondsTaken the total number of milliseconds taken for all invocations of the operation
     */
    public OpSnapshot(final String name, final long invocationCount, final long millisecondsTaken)
    {
        this(name, invocationCount, millisecondsTaken, 0, 0);
    }

    public String getName()
    {
        return name;
    }

    /**
     * @return the number of times this operation has been invoked
     */
    public long getInvocationCount()
    {
        return invocationCount;
    }

    /**
     * @return the total time taken by this operation
     */
    public long getMillisecondsTaken()
    {
        return millisecondsTaken;
    }

    /**
     * @return the size of the result set
     */
    public long getResultSetSize()
    {
        return resultSetSize;
    }

    /**
     * @return the CPU time used in nanoseconds
     */
    public long getCpuTime()
    {
        return cpuTime;
    }

    /**
     * This is a synonym for {@link #getMillisecondsTaken()}
     */
    public long getValue()
    {
        return getMillisecondsTaken();
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final OpSnapshot that = (OpSnapshot) o;

        if (invocationCount != that.invocationCount)
        {
            return false;
        }
        if (millisecondsTaken != that.millisecondsTaken)
        {
            return false;
        }
        if (cpuTime != that.cpuTime)
        {
            return false;
        }
        if (resultSetSize != that.resultSetSize)
        {
            return false;
        }
        //noinspection RedundantIfStatement
        if (!name.equals(that.name))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = name.hashCode();
        result = 31 * result + (int) (invocationCount ^ (invocationCount >>> 32));
        result = 31 * result + (int) (millisecondsTaken ^ (millisecondsTaken >>> 32));
        result = 31 * result + (int) (cpuTime ^ (cpuTime >>> 32));
        result = 31 * result + (int) (resultSetSize ^ (resultSetSize >>> 32));
        return result;
    }

    ///CLOVER:OFF
    public String toString()
    {
        StringBuilder sb = new StringBuilder("name=")
                .append(name)
                .append("; inv=")
                .append(invocationCount)
                .append("; ms=")
                .append(millisecondsTaken)
                .append("; cpu=")
                .append(cpuTime)
                .append("; rss=")
                .append(resultSetSize)
                .append(";");

        return sb.toString();
    }
    ///CLOVER:ON

    /**
     * By default it sorts in name / highest time taken / highest invocation count / highest result set size order
     *
     * @param that the object tom compare
     * @return -1, 0 or 1 as per Comparable
     */
    public int compareTo(final Instrument that)
    {
        if (that == null)
        {
            return -1;
        }
        if (that instanceof OpSnapshot)
        {
            OpSnapshot thatSnapshot = (OpSnapshot) that;
            long rc = (this.millisecondsTaken - thatSnapshot.millisecondsTaken);
            if (rc == 0)
            {
                rc = (this.invocationCount - thatSnapshot.invocationCount);
                if (rc == 0)
                {
                    rc = (this.resultSetSize - thatSnapshot.resultSetSize);
                }
            }
            return rc > 0 ? 1 : (rc < 0 ? -1 : 0);
        }
        else
        {
            return new InstrumentComparator().compare(this, that);
        }
    }

    /**
     * This can be used to find out the difference between this snapshot and a previous one
     * <p/>
     *
     * @param previousSnapshot a previous snapshot
     * @return the difference expressed as a new OpSnapshot
     */
    public OpSnapshot substract(OpSnapshot previousSnapshot)
    {
        notNull("previousSnapshot", previousSnapshot);
        return new OpSnapshot(this.name,
                this.invocationCount - previousSnapshot.invocationCount,
                this.millisecondsTaken - previousSnapshot.millisecondsTaken,
                this.resultSetSize - previousSnapshot.resultSetSize,
                this.cpuTime - previousSnapshot.cpuTime
        );
    }

    /**
     * Adds the values in the otherOpSnapshot to the current one and returns a new object repsentation of itself.  The
     * name of the returned object will be the name of this OpSnapshot.
     *
     * @param otherOpSnapshot the one to add to this snapshot
     * @return a new OpSnapshot object with the added values
     */
    public OpSnapshot add(OpSnapshot otherOpSnapshot)
    {
        return new OpSnapshot(this.name,
                this.invocationCount + otherOpSnapshot.invocationCount,
                this.millisecondsTaken + otherOpSnapshot.millisecondsTaken,
                this.resultSetSize + otherOpSnapshot.resultSetSize,
                this.cpuTime + otherOpSnapshot.cpuTime
        );
    }

}
