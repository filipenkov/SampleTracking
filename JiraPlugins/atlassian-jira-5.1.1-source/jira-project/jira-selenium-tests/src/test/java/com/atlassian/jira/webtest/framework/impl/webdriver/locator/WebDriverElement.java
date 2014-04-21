package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.webdriver.condition.ContainsTextCondition;
import com.atlassian.jira.webtest.framework.impl.webdriver.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.webdriver.condition.IsVisibleCondition;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContextAware;
import com.atlassian.webtest.ui.keys.KeySequence;
import org.openqa.selenium.By;

/**
 * WebDriver implementation of the {@link com.atlassian.jira.webtest.framework.core.locator.Element} interface.
 *
 * @since v4.2
 */
class WebDriverElement extends WebDriverContextAware implements Element
{
    private final By by;
    private final ElementFinder elementFinder;

    WebDriverElement(final By by, WebDriverContext ctx)
    {
        super(ctx);
        this.by = by;
        this.elementFinder = new ElementFinder(ctx, by);
    }

    public TimedCondition isPresent()
    {
        return new IsPresentCondition(by, context);
    }

    public TimedCondition isNotPresent()
    {
        return Conditions.not(isPresent());
    }

    public TimedCondition isVisible()
    {
        return new IsVisibleCondition(by, context);
    }

    public TimedCondition isNotVisible()
    {
        return Conditions.not(isVisible());
    }

    public TimedCondition containsText(String text)
    {
        return new ContainsTextCondition(by, context, text);
    }

    public TimedCondition doesNotContainText(String text)
    {
        return Conditions.not(containsText(text));
    }

    // TODO
    
    @Override
    public TimedQuery<String> value()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimedQuery<String> text()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimedQuery<String> attribute(String attrName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element type(KeySequence keys)
    {
        checkPresent();
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Element clear()
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Element click()
    {
        checkPresent();
        elementFinder.getFirst().click();
        return this;
    }

    private void checkPresent()
    {
        if (!isPresent().now())
        {
            throw new IllegalStateException("Not present");
        }
    }
}
