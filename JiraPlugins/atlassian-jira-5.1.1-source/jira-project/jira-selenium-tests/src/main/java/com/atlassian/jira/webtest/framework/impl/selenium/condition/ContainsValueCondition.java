package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;


/**
 * Condition that an element specified by given locator must contain a particular value in the current test context.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 */
public class ContainsValueCondition extends AbstractContentsCondition implements TimedCondition
{
    public static final class Builder extends AbstractContentsConditionBuilder<Builder, ContainsValueCondition>
    {
        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        @Override
        public ContainsValueCondition build()
        {
            return new ContainsValueCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    private ContainsValueCondition(Builder builder)
    {
        super(builder, ElementType.INPUT);
    }
}
