package com.atlassian.jira.web.filters.steps.instrumentation;

import com.atlassian.instrumentation.Gauge;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;

/**
 * Instruments at a top level the web request of JIRA
 *
 * @since v4.4
 */
public class InstrumentationStep implements FilterStep
{
    private final Gauge concurrentUsersGauge;
    private OpTimer requestTimer;

    public InstrumentationStep()
    {
        concurrentUsersGauge = Instrumentation.pullGauge(InstrumentationName.CONCURRENT_USERS);
    }

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        requestTimer = Instrumentation.pullTimer(InstrumentationName.WEB_REQUESTS);
        concurrentUsersGauge.incrementAndGet();

        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        requestTimer.end();
        concurrentUsersGauge.decrementAndGet();

        /*
         * Web request that generate markup end up calling this and hence get access to the thread local
         * version of the instrument values.  However other requests dont go through AccessLogImprinter and
         * hence we have to make double sure we clear that thread local stroage otherwise we double count
         * on the next request
         */
        Instrumentation.snapshotThreadLocalOperationsAndClear();
        return callContext;
    }

}
