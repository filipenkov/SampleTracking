package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.StaticQuery;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.query.TextQuery;

/**
 * Selenium {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery} factory.
 *
 * @since v4.3
 */
public class SeleniumQueries extends SeleniumContextAware
{
    private final long evaluationInterval;
    private final long uiAction;

    protected SeleniumQueries(SeleniumContext context)
    {
        super(context);
        this.evaluationInterval = timeouts.timeoutFor(Timeouts.EVALUATION_INTERVAL);
        this.uiAction = timeouts.timeoutFor(Timeouts.UI_ACTION);
    }

    /**
     * New query with static value.
     *
     * @param value value to return
     * @param defTimeout default timeout
     * @param <T> type of the query result
     * @return new static timed query
     * @see com.atlassian.jira.webtest.framework.core.query.StaticQuery
     */
    public <T> TimedQuery<T> forStaticValue(T value, long defTimeout)
    {
        return new StaticQuery<T>(value, defTimeout, evaluationInterval);
    }

    /**
     * New query with static value and default timeout set to {@link Timeouts#UI_ACTION}.
     *
     * @param value value to return
     * @param <T> type of the query result
     * @return new static timed query
     * @see com.atlassian.jira.webtest.framework.core.query.StaticQuery
     */
    public <T> TimedQuery<T> forStaticValue(T value)
    {
        return new StaticQuery<T>(value, uiAction, evaluationInterval);
    }

    /**
     * Builder for a query retrieving text of a given element.
     *
     * @param locator locator of the element
     * @return text query builder
     */
    public TextQuery.Builder forTextBuilder(Locator locator)
    {
        return TextQuery.forContext(context).locator((SeleniumLocator)locator);
    }

    /**
     * Query for element's text. It will have default timeout {@link Timeouts#UI_ACTION} and default expiration
     * handler {@link com.atlassian.jira.webtest.framework.core.query.ExpirationHandler#RETURN_NULL}.
     *
     * @param locator locator of the target element
     * @return timed query for element's text
     */
    public TimedQuery<String> forText(Locator locator)
    {
        return forTextBuilder(locator).expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }
}
