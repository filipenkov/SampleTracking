package com.atlassian.instrumentation.operations;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.Instrument;

/**
 * A OpCounter can keep track of how long a computing operation takes and how many times it has been invoked, as well as
 * optionally how bug the result set size was
 * <p/>
 * This is a MUTABLE form of {@link OpSnapshot} since it can increase in value.  However it is THREAD SAFE in the sense
 * that the values it exposes are IMMUTABLE snapshots in time.  You CANNOT get access to the underlying counter values
 * directly.
 * <p/>
 * A OpCounter can be thought of as 3 {@link com.atlassian.instrumentation.Counter}'s rolled into one object.
 * <p/>
 * <ul> <li>The snapshot().getInvocationCount() is like a {@link com.atlassian.instrumentation.Counter} that shows how
 * many times the operation has been invoked.</li>
 * <p/>
 * <li>The snapshot().getMillisecondsTaken() is like a {@link com.atlassian.instrumentation.Counter} that shows how long
 * the operation has taken in total.</li>
 * <p/>
 * <li>The snapshot().getResultSetSize() is like a {@link com.atlassian.instrumentation.Counter} that shows how big the
 * results have been.</li>
 * <p/>
 * </ul>
 * <p/>
 * In fact {@link OpCounter} and {@link OpSnapshot} is an {@link OpInstrument}/{@link
 * com.atlassian.instrumentation.Instrument} and hence can be recorded as one discrete object
 *
 * @since v4.0
 */
public final class OpCounter implements OpInstrument
{
    private long invocationCount;
    private long millisecondsTaken;
    private long resultSetSize;
    private long cpuTime;
    private final String name;

    public OpCounter(String name)
    {
        this(name, 0, 0, 0, 0);
    }

    public OpCounter(String name, long millisecondsTaken)
    {
        this(name, 1, millisecondsTaken, 0, 0);
    }

    public OpCounter(String name, long invocationCount, long millisecondsTaken)
    {
        this(name, invocationCount, millisecondsTaken, 0, 0);
    }

    public OpCounter(final OpSnapshot opSnapshot)
    {
        this(opSnapshot.getName(), opSnapshot.getInvocationCount(), opSnapshot.getMillisecondsTaken(), opSnapshot.getResultSetSize(), opSnapshot.getCpuTime());
    }

    public OpCounter(final String name, final long invocationCount, final long millisecondsTaken, final long resultSetSize, final long cpuTime)
    {
        notNull("name", name);
        this.name = name;
        this.invocationCount = invocationCount;
        this.millisecondsTaken = millisecondsTaken;
        this.resultSetSize = resultSetSize;
        this.cpuTime = cpuTime;
    }

    /**
     * Called to increment the values of the OpCounter by the values in another {@link OpCounter}.
     * <p/>
     *
     * @param opCounter a counter of an operation
     * @return this for a fluent style
     */
    public OpCounter add(OpCounter opCounter)
    {
        notNull("opCounter", opCounter);
        this.add(opCounter.snapshot());
        return this;
    }

    /**
     * Called to increment the values of the OpCounter by the values in the {@link OpSnapshot}.
     * <p/>
     *
     * @param opSnapshot a snapshot of an operation
     * @return this for a fluent style
     */
    public OpCounter add(OpSnapshot opSnapshot)
    {
        notNull("opSnapshot", opSnapshot);
        synchronized (this)
        {
            this.invocationCount += opSnapshot.getInvocationCount();
            this.millisecondsTaken += opSnapshot.getMillisecondsTaken();
            this.resultSetSize += opSnapshot.getResultSetSize();
            this.cpuTime += opSnapshot.getCpuTime();
        }
        return this;
    }

    /**
     * This returns the current snapshot value of the counter.
     *
     * @return the current value as a snapshot
     */
    public OpSnapshot snapshot()
    {
        synchronized (this)
        {
            return new OpSnapshot(name, invocationCount, millisecondsTaken, resultSetSize, cpuTime);
        }
    }

    /**
     * @see OpSnapshot#getInvocationCount()
     */
    public long getInvocationCount()
    {
        return snapshot().getInvocationCount();
    }

    /**
     * @see OpSnapshot#getMillisecondsTaken()
     */
    public long getMillisecondsTaken()
    {
        return snapshot().getMillisecondsTaken();
    }

    /**
     * @see OpSnapshot#getResultSetSize()
     */
    public long getResultSetSize()
    {
        return snapshot().getResultSetSize();

    }

    public long getCpuTime()
    {
        return snapshot().getCpuTime();
    }

    /**
     * @see OpSnapshot#getValue()
     */
    public long getValue()
    {
        return snapshot().getValue();
    }

    /**
     * A compareTo method that uses underlying OpSnapshot compareTo
     *
     * @param that the object to compare
     * @return -1, 0 or 1
     */
    public int compareTo(final Instrument that)
    {
        if (that == this)
        {
            return 0;
        }
        if (that == null)
        {
            return -1;
        }
        int rc = that.getName().compareTo(this.getName());
        if (rc == 0 && that instanceof OpCounter)
        {
            OpCounter thatOp = (OpCounter) that;
            rc = this.snapshot().compareTo(thatOp.snapshot());
        }
        return rc;
    }

    public String getName()
    {
        return name;
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

        final OpCounter that = (OpCounter) o;

        if (invocationCount != that.invocationCount)
        {
            return false;
        }
        if (millisecondsTaken != that.millisecondsTaken)
        {
            return false;
        }
        if (Double.compare(that.resultSetSize, resultSetSize) != 0)
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        result = (int) (invocationCount ^ (invocationCount >>> 32));
        result = 31 * result + (int) (millisecondsTaken ^ (millisecondsTaken >>> 32));
        ///CLOVER:OFF
        temp = resultSetSize != +0.0d ? Double.doubleToLongBits(resultSetSize) : 0L;
        ///CLOVER:ON
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + name.hashCode();
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
}
