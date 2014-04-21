package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.CompositeClock;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.PeriodicClock;

import java.util.Date;

/**
 * {@link com.atlassian.core.util.Clock} implementations for testing conditions.
 *
 * @since v4.3
 */
public final class ConditionClocks
{
    private ConditionClocks()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Clock forInterval(long interval)
    {
        return new CompositeClock(new ConstantClock(new Date(0L))).addClock(2, new PeriodicClock(0L, interval, 2));
    }
}
