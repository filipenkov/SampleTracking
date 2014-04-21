package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Condition that a value of given locator has changed. The value will be evaluated during
 * initialization of the condition and subsequent calls to {@link #now()} will
 * return true if the value has changed.
 *
 * @since v4.2
 */
public final class ValueChangedCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{

    public static final class Builder extends AbstractLocatorBasedTimedConditionBuilder<Builder,ValueChangedCondition>
    {
        private final ElementType elemType;

        public Builder(SeleniumContext context, ElementType type)
        {
            super(context, Builder.class);
            this.elemType = notNull("type", type);
        }

        @Override
        public ValueChangedCondition build()
        {
            return new ValueChangedCondition(this);
        }
    }

    /**
     * Builder for value changed in an input element
     *
     * @param context selenium context
     * @return new input changed condition
     * @see ElementType#INPUT
     */
    public static Builder newInputChanged(SeleniumContext context)
    {
        return new Builder(context, ElementType.INPUT);
    }

    /**
     * Builder for value changed in an input element
     *
     * @param context selenium context
     * @return new text changed condition builder
     * @see ElementType#INPUT
     */
    public static Builder newTextChanged(SeleniumContext context)
    {
        return new Builder(context, ElementType.TEXT);
    }

    private final String initialValue;
    private final ElementType elementType;

    private ValueChangedCondition(Builder builder)
    {
        super(builder);
        this.elementType = notNull("elementType", builder.elemType);
        this.initialValue = currentValue();
    }

    public ValueChangedCondition(SeleniumContext context, String locator, ElementType elementType)
    {
        super(context, locator);
        this.elementType = notNull("elementType", elementType);
        this.initialValue = currentValue();
    }

    public boolean now()
    {
        return !currentValue().equals(initialValue());
    }

    public String initialValue()
    {
        return initialValue;
    }

    private String currentValue()
    {
        return elementType.retrieve(locator,client);
    }

    public ElementType elementType()
    {
        return elementType;
    }

    @Override
    public String toString()
    {
        return asString("Initial value <",initialValue, "> of element [locator=", locator,",type=", elementType,
                " should change");
    }



}
