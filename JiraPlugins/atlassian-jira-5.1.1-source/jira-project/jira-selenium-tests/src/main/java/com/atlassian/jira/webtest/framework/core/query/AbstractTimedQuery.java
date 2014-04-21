package com.atlassian.jira.webtest.framework.core.query;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.ClockAware;
import com.atlassian.jira.util.RealClock;
import com.atlassian.jira.webtest.framework.core.AbstractPollingQuery;
import com.atlassian.jira.webtest.framework.core.PollingQuery;
import com.atlassian.jira.webtest.framework.util.Timeout;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.Clocks.getClock;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;


/**
 * <p>
 * Abstract query that implements {@link #byDefaultTimeout()} in terms of {@link #by(long)}, and {@link #by(long)} as a
 * template method calling the following hooks (to be implemented by subclasses):
 * <ul>
 * <li>{@link #currentValue()} - to determine current evaluation of the query
 * <li>{@link #shouldReturn(Object)} - which indicates, if current value of the query should be returned 
 * </ul>
 *
 * <p>
 * In addition, an {@link ExpirationHandler} must be provided to handle the case of expired query.
 *
 * @see com.atlassian.jira.webtest.framework.core.query.ExpirationHandler
 * 
 * @since v4.3
 */
@NotThreadSafe
public abstract class AbstractTimedQuery<T> extends AbstractPollingQuery implements TimedQuery<T>, ClockAware
{
    private final Clock clock;
    private final ExpirationHandler expirationHandler;

    private boolean lastRun = false;

    protected AbstractTimedQuery(Clock clock, long defTimeout, long interval, ExpirationHandler expirationHandler)
        {
            super(interval, defTimeout);
            this.clock = notNull("clock", clock);
            this.expirationHandler = notNull("expirationHandler", expirationHandler);
        }


    protected AbstractTimedQuery(long defTimeout, long interval, ExpirationHandler expirationHandler)
    {
        this(RealClock.getInstance(), defTimeout, interval, expirationHandler);
    }

    protected AbstractTimedQuery(PollingQuery other, ExpirationHandler expirationHandler)
    {
        this(getClock(other), other.defaultTimeout(), notNull("other", other).interval(), expirationHandler);
    }

    public final T by(long timeout)
    {
        resetLastRun();
        final long start = currentTime();
        final long deadline = start + greaterThan("timeout", timeout, 0);
        while (withinTimeout(deadline))
        {
            T current = currentValue();
            if (shouldReturn(current))
            {
                return current;
            }
            else if (isLastRun())
            {
                return expired(current, timeout);
            }
            else
            {
                Timeout.waitFor(sleepTime(deadline)).milliseconds();
            }
        }
        return expired(currentValue(), timeout);
    }

    public final T by(long timeout, TimeUnit unit)
    {
        return by(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    public T byDefaultTimeout()
    {
        return by(defaultTimeout);
    }

    @Override
    public final T now()
    {
        T val = currentValue();
        if (shouldReturn(val))
        {
            return val;
        }
        else
        {
            return expired(val, 0);       
        }
    }

    /**
     * Expiration handler of this query
     *
     * @return expiration handler of this query
     */
    public final ExpirationHandler expirationHandler()
    {
        return expirationHandler;
    }

    /**
     * If the current evaluated query value should be returned.
     *
     * @param currentEval current query evaluation
     * expires
     * @return <code>true</code>, if the current query evaluation should be returned as a result of this timed query
     */
    protected abstract boolean shouldReturn(T currentEval);

    /**
     * Current evaluation of the query.
     *
     * @return current evaluation of the query
     */
    protected abstract T currentValue();

    /**
     * Value to returned on the timeout expiry (last query evaluation).
     *
     * @param currentVal current query value
     * @param timeout timeout
     * @return query evaluation on the timeout expiration
     */
    private T expired(T currentVal, long timeout)
    {
        return expirationHandler.expired(this, currentVal, timeout);
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
            // yes this is quite arbitrary, the goal is to run the last evaluation as close to the deadline as possible,
            // but not let it pass
            long toEvalOneMoreTime = deadline - now - 2;
            setLastRun();
            return toEvalOneMoreTime > 0 ? toEvalOneMoreTime : 1;
        }
    }

    private void setLastRun()
    {
        lastRun = true;
    }

    protected void resetLastRun()
    {
        lastRun = false;
    }

    private boolean isLastRun()
    {
        return lastRun;
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(), "[interval=", interval, ",defaultTimeout=", defaultTimeout, "]");
    }
}
