package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.AbstractSeleniumTimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ContainsTextCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsVisibleCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.query.AttributeQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.query.TextQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.query.ValueQuery;
import com.atlassian.selenium.keyboard.SeleniumTypeWriter;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.TypeMode;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.core.locator.Element} interface.
 *
 * @since v4.2
 */
class SeleniumElement extends SeleniumContextAware implements Element
{
    private final SeleniumLocator parent;
    private final Timeouts defaultTimeout;
    private SeleniumTypeWriter writer;

    SeleniumElement(SeleniumLocator parent, SeleniumContext context)
    {
        this(parent, context, AbstractSeleniumTimedCondition.DEFAULT_TIMEOUT);
    }

    SeleniumElement(SeleniumLocator parent, SeleniumContext context, Timeouts defaultTimeout)
    {
        super(context);
        this.parent = notNull("parent", parent);
        this.defaultTimeout = notNull("defaultTimeout", defaultTimeout);
    }


    // need to lazy init cause SeleniumTypeWriter uses full selenium locators
    private SeleniumTypeWriter writer()
    {
        if (writer == null)
        {
            writer = new SeleniumTypeWriter(context.client(), parent.fullLocator(), TypeMode.TYPE);
        }
        return writer;
    }

    /* ---------------------------------------------- CONDITIONS ---------------------------------------------------- */

    public TimedCondition isPresent()
    {
        return IsPresentCondition.forContext(context).locator(parent).defaultTimeout(defaultTimeout).build();
    }

    public TimedCondition isNotPresent()
    {
        return Conditions.not(isPresent());
    }

    public TimedCondition isVisible()
    {
        return IsVisibleCondition.forContext(context).locator(parent).defaultTimeout(defaultTimeout).build();
    }

    public TimedCondition isNotVisible()
    {
        return Conditions.not(isVisible());
    }

    public TimedCondition containsText(String text)
    {
        return ContainsTextCondition.forContext(context).locator(parent).expectedValue(text)
                .defaultTimeout(defaultTimeout).build();
    }

    public TimedCondition doesNotContainText(String text)
    {
        return Conditions.not(containsText(text));
    }


    /* ------------------------------------------------- QUERIES ---------------------------------------------------- */

    @Override
    public TimedQuery<String> value()
    {
        return ValueQuery.forContext(context).condition(isPresent()).locator(parent).defaultTimeout(defaultTimeout)
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    @Override
    public TimedQuery<String> text()
    {
        return TextQuery.forContext(context).condition(isPresent()).locator(parent).defaultTimeout(defaultTimeout)
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    @Override
    public TimedQuery<String> attribute(String attrName)
    {
        return AttributeQuery.forContext(context).condition(isPresent()).locator(parent).defaultTimeout(defaultTimeout)
                .expirationHandler(ExpirationHandler.RETURN_NULL).attributeName(attrName).build();
    }


    /* ------------------------------------------------- ACTIONS ---------------------------------------------------- */

    public Element type(KeySequence keys)
    {
        if (!isPresent().now())
        {
            throw new IllegalStateException("Not present");
        }
        writer().type(keys);
        return this;
    }

    @Override
    public Element clear()
    {
        writer().clear();
        return this;
    }

    public Element click()
    {
        if (!isPresent().now())
        {
            throw new IllegalStateException("Not present: " + parent);
        }
        context.client().click(parent.fullLocator());
        return this;
    }

    Timeouts defaultTimeout()
    {
        return defaultTimeout;
    }

}
