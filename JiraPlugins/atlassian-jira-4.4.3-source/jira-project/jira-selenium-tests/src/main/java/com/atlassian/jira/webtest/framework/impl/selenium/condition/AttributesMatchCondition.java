package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition} that is <code>true</code>, if a given
 * set of attributes matches predefined values.
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 * @since v4.3
 */
public class AttributesMatchCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{
    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    public static final class Builder extends AbstractLocatorBasedTimedConditionBuilder<Builder,AttributesMatchCondition>
    {
        private final Map<String,String> expectedAttributes = new HashMap<String,String>();

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        /**
         * Add an expected attribute name-value pair. The target element will be verified to contain each pair as a
         * query underlying the target timed condition
         *
         * @param name attribute name
         * @param expectedValue expected attribute value
         * @return this builder instance
         */
        public final Builder expected(String name, String expectedValue)
        {
            expectedAttributes.put(name, expectedValue);
            return this;
        }

        @Override
        public AttributesMatchCondition build()
        {
            return new AttributesMatchCondition(this);
        }
    }

    private final Map<String,String> expectedAttributes;

    private AttributesMatchCondition(Builder builder)
    {
        super(builder);
        this.expectedAttributes = builder.expectedAttributes;
    }


    @Override
    public boolean now()
    {
        for (Map.Entry<String,String> expectedAttr : expectedAttributes.entrySet())
        {
            if (!hasAttribute(expectedAttr.getKey(), expectedAttr.getValue()))
            {
                return false;
            }
        }
        return true;
    }

    private boolean hasAttribute(String name, String value)
    {
        String attr = client.getAttribute(locator + "@" + name);
        return attr != null && attr.equals(value);
    }
}
