package com.atlassian.jira.webtest.framework.core.mock;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;

/**
 * Mock {@link com.atlassian.jira.webtest.framework.core.PageObject}.
 *
 * @since v4.3
 */
public class MockPageObject implements PageObject
{
    private TimedCondition mockCondition = new MockCondition(50, false);

    @Override
    public TimedCondition isReady()
    {
        return mockCondition;
    }

    @Override
    public WebTestContext context()
    {
        throw new UnsupportedOperationException("Implement me");
    }

    public MockPageObject condition(TimedCondition condition)
    {
        mockCondition = condition;
        return this;
    }

    
}
