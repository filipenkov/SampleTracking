package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A query for an elements attribute.
 *
 * @since v4.3
 */
public class AttributeQuery extends AbstractLocatorConditionQuery<String> implements TimedQuery<String>
{
    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    public static class Builder extends AbstractLocatorBasedConditionQueryBuilder<Builder, AttributeQuery, String>
    {
        private String attributeName;

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        public Builder attributeName(String name)
        {
            this.attributeName = notNull("attributeName",name);
            return this;
        }

        @Override
        public AttributeQuery build()
        {
            return new AttributeQuery(this);
        }
    }

    private final String attributeName;

    private AttributeQuery(Builder builder)
    {
        super(builder);
        this.attributeName = notNull("attributeName", builder.attributeName);
    }

    @Override
    protected String evaluateNow()
    {
        return client.getAttribute(locator + "@" + attributeName);
    }
}
