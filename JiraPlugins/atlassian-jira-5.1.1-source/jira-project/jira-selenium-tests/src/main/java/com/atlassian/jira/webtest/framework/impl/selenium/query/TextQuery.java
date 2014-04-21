package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * A query for an element's text.
 *
 * @since v4.3
 */
public class TextQuery extends AbstractLocatorConditionQuery<String> implements TimedQuery<String>
{
    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    public static class Builder extends AbstractLocatorBasedConditionQueryBuilder<Builder, TextQuery, String>
    {
        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        @Override
        public TextQuery build()
        {
            if (condition() == null)
            {
                condition(locatorPresentCondition());
            }
            return new TextQuery(this);
        }

        private IsPresentCondition locatorPresentCondition()
        {
            return IsPresentCondition.forContext(context).defaultTimeout(defaultTimeout()).locator(locator()).build();
        }
    }

    private TextQuery(Builder builder)
    {
        super(builder);
    }

    @Override
    protected String evaluateNow()
    {
        return client.getText(locator);
    }
}
