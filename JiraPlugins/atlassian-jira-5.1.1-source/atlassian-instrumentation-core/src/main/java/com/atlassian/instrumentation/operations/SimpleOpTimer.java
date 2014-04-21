package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.utils.dbc.Assertions;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A new instance of this class will begin a profiling operation, one that can count how long an operation takes
 * and how much CPU it used.
 * <p/>
 * These are ONE TIME USE objects.
 * <p/>
 * Once end() has been called they cannot be called again and WILL throw IllegalStateException if you try.
 * <p/>
 * You can however ask for a snapshot() at any time if you want but that is a secondary use case.
 * <p/>
 * The cpu time is based on the Java JMX {@link java.lang.management.ThreadMXBean#getCurrentThreadCpuTime()} and hence this object should not
 * be shared amongst multiple threads otherwise the value is meaningless.
 *
 * @since v4.0
 */
public class SimpleOpTimer implements OpTimer
{
    private final static OnEndCallback NOOP = new OnEndCallback()
    {
        public void onEndCalled(OpSnapshot opSnapshot)
        {
        }
    };

    private final String name;
    private final long then;
    private final long cpuThen;
    private final boolean captureCPUCost;
    private final AtomicReference<OpSnapshot> timerValue = new AtomicReference<OpSnapshot>();
    private final OnEndCallback onEndCallback;

    /**
     * Once you construct a SimpleOpTimer, the timer is started. GO!
     *
     * @param name the name of the operation being timed
     */
    public SimpleOpTimer(final String name)
    {
        this(name, true, NOOP);
    }

    public SimpleOpTimer(final String name, boolean captureCPUCost, OnEndCallback onEndCallback)
    {
        Assertions.notNull("name", name);
        Assertions.notNull("onEndCallback", onEndCallback);
        this.name = name;
        this.onEndCallback = onEndCallback;
        this.captureCPUCost = captureCPUCost;

        then = System.currentTimeMillis();
        cpuThen = getCurrentThreadCpuTime();
    }

    public String getName()
    {
        return name;
    }

    /**
     * This will return a snap shot of how long the timer has been running for.   If end() has been called, then this
     * will return the same OpSnapshot object that end() returned.
     *
     * @return a new OpSnapshot object will the invocation count = 1, the resultSetSize of NAN and the milliseconds set
     *         to how long this timer has been running for.
     */
    public OpSnapshot snapshot()
    {
        //
        // this is a THREAD guard against end being endCalled more than once.  If its true then end must be called
        if (timerValue.get() != null)
        {
            return timerValue.get();
        }
        else
        {
            long millisTaken = Math.max(0, System.currentTimeMillis() - then);
            long cpuTimeTaken = Math.max(0, getCurrentThreadCpuTime() - cpuThen);
            return new OpSnapshot(name, 1, millisTaken, 0, cpuTimeTaken);
        }
    }

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     *
     * @param resultSetSize the size of the operations result.
     * @return a new OpSnapshot representing this operation timing
     */
    public OpSnapshot end(final long resultSetSize)
    {
        return end(new HeisenburgResultSetCalculator()
        {
            public long calculate()
            {
                return resultSetSize;
            }
        });
    }

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     * <p/>
     * The result set size will be set to 0.
     *
     * @return a new OpSnapshot representing this operation timing
     */
    public OpSnapshot end()
    {
        return end(0);
    }

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     *
     * @param heisenburgResultSetCalculator a class to calculate time taken
     * @return a new OpSnapshot representing this operation timing
     */
    public OpSnapshot end(HeisenburgResultSetCalculator heisenburgResultSetCalculator)
    {
        long millisTaken = Math.max(0, System.currentTimeMillis() - then);
        long cpuTimeTaken = Math.max(0, getCurrentThreadCpuTime() - cpuThen);

        // work out the size of the result after the timer has been stopped
        long resultSetSize = 0;
        if (heisenburgResultSetCalculator != null)
        {
            resultSetSize = heisenburgResultSetCalculator.calculate();
        }
        final OpSnapshot opSnapshot = new OpSnapshot(name, 1, millisTaken, resultSetSize, cpuTimeTaken);
        //
        // this is a THREAD guard against end being called more than once.
        if (!timerValue.compareAndSet(null, opSnapshot))
        {
            throw new IllegalStateException("The OpTimer has been re-used.  end() can only be called once!");
        }
        onEndCallback.onEndCalled(opSnapshot);
        return opSnapshot;
    }

    @Override
    public OpSnapshot endWithTime(final long timeInMillis)
    {
        final OpSnapshot opSnapshot = new OpSnapshot(name, 1, timeInMillis);
        // this is a THREAD guard against end being called more than once.
        if (!timerValue.compareAndSet(null, opSnapshot))
        {
            throw new IllegalStateException("The OpTimer has been re-used.  end() can only be called once!");
        }
        onEndCallback.onEndCalled(opSnapshot);
        return opSnapshot;
    }

    /**
     * This uses the JMX Thread beans to capture the current threads CPU time.  It can return -1
     * if not enabled and throw  UnsupportedOperationException is not available.
     *
     * @return the cpu time in nanoseconds used or 0
     */
    private long getCurrentThreadCpuTime()
    {
        long cpuUsed = 0;
        if (captureCPUCost)
        {
            try
            {
                final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                if (threadMXBean.isCurrentThreadCpuTimeSupported())
                {
                    cpuUsed = threadMXBean.getCurrentThreadCpuTime();
                }
            }
            catch (UnsupportedOperationException e)
            {
                // ok we will record it as 0 in this case
            }
        }
        // the CPU time returned can be -1 if timing is not enabled.  We normalise this to 0 in this case
        return cpuUsed == -1 ? 0 : cpuUsed;
    }


}
