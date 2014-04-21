package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.ClockAware;
import com.atlassian.jira.util.RealClock;
import com.atlassian.jira.webtest.framework.core.AbstractPollingQuery;
import com.atlassian.jira.webtest.framework.core.PollingQuery;
import com.atlassian.jira.webtest.framework.util.Timeout;
import net.jcip.annotations.NotThreadSafe;

import static com.atlassian.jira.util.Clocks.getClock;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;


/**
 * Abstract condition that implements {@link #by(long)} in terms of
 * {@link #now()} (to implement by derived classes) and a configured interval between
 * attempts to evaluate {@link #now()}.
 *
 * @since v4.3
 */
@NotThreadSafe
public abstract class AbstractTimedCondition extends AbstractPollingQuery implements TimedCondition, ClockAware
{
    private final Clock clock;
    private boolean lastRun = false;

    protected AbstractTimedCondition(Clock clock, long defTimeout, long interval)
    {
        super(interval, defTimeout);
        this.clock = notNull("clock", clock);
    }

    protected AbstractTimedCondition(long defTimeout, long interval)
    {
        this(RealClock.getInstance(), defTimeout, interval);
    }

    protected AbstractTimedCondition(PollingQuery other)
    {
        this(getClock(other), other.defaultTimeout(), other.interval());
    }

    public final boolean by(long timeout)
    {
        resetLastRun();
        final long start = currentTime();
        final long deadline = start + greaterThan("timeout", timeout, 0);
        while (withinTimeout(deadline) || isLastRun())
        {
            if (isLastRun())
            {
                return now();
            }
            else if (now())
            {
                return true;
            }
            else
            {
                Timeout.waitFor(sleepTime(deadline)).milliseconds();
            }
        }
        return false;
    }

    public final boolean byDefaultTimeout()
    {
        return by(defaultTimeout);
    }

    @Override
    public Clock clock()
    {
        return clock;
    }

    private long currentTime()
    {
        return clock.getCurrentDate().getTime();
    }


    private boolean withinTimeout(final long deadline)
    {
        return currentTime() <= deadline;
    }

    private long sleepTime(final long deadline)
    {
        final long now = currentTime();
        if (now + interval < deadline)
        {
            return interval;
        }
        else
        {
            // yes this is quite arbitrary, the goal is to run the last evaluation as close to the deadline as possible
            long toEvalOneMoreTime = deadline - now - 2;
            setLastRun();
            return toEvalOneMoreTime > 0 ? toEvalOneMoreTime : 1;
        }
    }

    private void setLastRun()
    {
        lastRun = true;
    }

    private void resetLastRun()
    {
        lastRun = false;
    }

    private boolean isLastRun()
    {
        return lastRun;
    }
}
