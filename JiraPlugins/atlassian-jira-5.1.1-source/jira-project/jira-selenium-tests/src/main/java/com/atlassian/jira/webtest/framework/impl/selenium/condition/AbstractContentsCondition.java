package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;


/**
 * Condition that an element specified by given locator must contain given contents.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 */
abstract class AbstractContentsCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{
    public static abstract class AbstractContentsConditionBuilder<B extends AbstractContentsConditionBuilder<B,T>, T extends AbstractContentsCondition>
            extends AbstractLocatorBasedTimedConditionBuilder<B,T>
    {
        private String text;

        public AbstractContentsConditionBuilder(SeleniumContext context, Class<B> builderClass)
        {
            super(context, builderClass);
        }

        public B expectedValue(String text)
        {
            this.text = text;
            return asTargetType();
        }

    }

    private final String expected;
    private final ElementType elementType;

    protected AbstractContentsCondition(AbstractContentsConditionBuilder<?,?> builder, ElementType elemType)
    {
        super(builder);
        this.expected = notNull("expected", builder.text);
        this.elementType = notNull("elementType", elemType);
    }

    public boolean now()
    {
        return elementType.retrieve(locator, client).contains(expected);
    }

    @Override
    public String toString()
    {
        return asString(super.toString(), "[expected=", expected, "]");
    }
}
