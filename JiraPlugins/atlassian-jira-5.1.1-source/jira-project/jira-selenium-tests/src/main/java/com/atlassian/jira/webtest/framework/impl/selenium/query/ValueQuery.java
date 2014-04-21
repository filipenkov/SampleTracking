package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * A query for an elements value.
 *
 * @since v4.3
 */
public class ValueQuery extends AbstractLocatorConditionQuery<String> implements TimedQuery<String>
{
    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    public static class Builder extends AbstractLocatorBasedConditionQueryBuilder<Builder, ValueQuery, String>
    {
        Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        @Override
        public ValueQuery build()
        {
            if (condition() == null)
            {
                condition(locatorPresentCondition());
            }
            return new ValueQuery(this);
        }

        private IsPresentCondition locatorPresentCondition()
        {
            return IsPresentCondition.forContext(context).defaultTimeout(defaultTimeout()).locator(locator()).build();
        }
    }

    private ValueQuery(Builder builder)
    {
        super(builder);
    }

    @Override
    protected String evaluateNow()
    {
        return client.getValue(locator);
    }
}
