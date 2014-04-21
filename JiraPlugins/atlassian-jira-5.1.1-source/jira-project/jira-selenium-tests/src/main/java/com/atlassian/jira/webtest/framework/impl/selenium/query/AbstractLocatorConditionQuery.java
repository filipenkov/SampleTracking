package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * Condition based query that uses locator. Got it?;P
 *
 * @since v4.3
 */
public abstract class AbstractLocatorConditionQuery<T> extends AbstractSeleniumConditionBasedQuery<T> implements TimedQuery<T>
{
    protected final String locator;

    protected AbstractLocatorConditionQuery(AbstractLocatorBasedConditionQueryBuilder<?, ?, T> builder)
    {
        super(builder);
        this.locator = Assertions.notNull("locator", builder.locator());
    }

}
