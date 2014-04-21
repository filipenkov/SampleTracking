package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;


/**
 * Condition that the current test context is in a particular browser window.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 */
public class IsInWindowCondition extends AbstractSeleniumTimedCondition implements TimedCondition
{
    public static final class Builder extends AbstractSeleniumTimedConditionBuilder<Builder, IsInWindowCondition>
    {
        private String windowId;

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        public Builder windowId(String text)
        {
            this.windowId = text;
            return this;
        }

        @Override
        public IsInWindowCondition build()
        {
            return new IsInWindowCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    private final String windowId;

    private IsInWindowCondition(Builder builder)
    {
        super(builder);
        this.windowId = notNull("text", builder.windowId);
    }

    public boolean now()
    {
        return client.getEval("selenium.browserbot.getUserWindow().name").trim().equals(windowId);
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(), "[windowId=", windowId, "]");
    }
}
