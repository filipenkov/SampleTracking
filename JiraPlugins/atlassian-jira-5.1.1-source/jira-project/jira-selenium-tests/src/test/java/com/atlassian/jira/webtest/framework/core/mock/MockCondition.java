package com.atlassian.jira.webtest.framework.core.mock;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.webtest.framework.core.condition.AbstractTimedCondition;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation of {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition}.
 *
 * @since v4.3
 */
public class MockCondition extends AbstractTimedCondition implements TimedCondition
{
    public static TimedCondition TRUE = new MockCondition(true);
    public static TimedCondition FALSE = new MockCondition(false);

    public static MockCondition successAfter(int falseCount)
    {
        boolean[] results = new boolean[falseCount + 1];
        Arrays.fill(results, false);
        results[results.length-1] = true;
        return new MockCondition(results);
    }

    public static final int DEFAULT_INTERVAL = 50;
    public static final long DEFAULT_TIMEOUT = 500;

    private final boolean[] results;
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger callCount = new AtomicInteger();
    private volatile boolean limitReached = false;
    private final ConcurrentLinkedQueue<Long> times = new ConcurrentLinkedQueue<Long>();

    public MockCondition(Clock clock, long interval, boolean... results)
    {
        super(clock, DEFAULT_TIMEOUT, interval);
        this.results = results;
    }

    public MockCondition(long interval, boolean... results)
    {
        super(DEFAULT_TIMEOUT, interval);
        this.results = results;
    }

    public MockCondition(boolean... results)
    {
        super(500L, DEFAULT_INTERVAL);
        this.results = results;
    }

    public MockCondition withClock(Clock clock)
    {
        return new MockCondition(clock, interval, results);
    }

    public boolean now()
    {
        callCount.incrementAndGet();
        if (limitReached)
        {
            return last();
        }
        else
        {
            return next();
        }
    }

    private boolean last()
    {
        boolean answer = results[results.length-1];
        times.add(System.currentTimeMillis());
        return answer;
    }

    private boolean next()
    {
        int current = count.getAndIncrement();
        if (current >= results.length -1)
        {
            limitReached = true;
        }
        if (current > results.length -1)
        {
            return last();
        }
        boolean answer = results[current];
        times.add(System.currentTimeMillis());
        return answer;
    }

    public int callCount()
    {
        return callCount.get();
    }

    public List<Long> times()
    {
        return new ArrayList<Long>(times);
    }
}
