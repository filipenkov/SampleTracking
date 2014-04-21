package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.webtest.ui.keys.KeySequence;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.core.component.Input}.
 *
 * @since v4.3
 */
public class SeleniumInput extends AbstractLocatorBasedPageObject implements Input
{
    private final SeleniumLocator locator;

    public SeleniumInput(SeleniumLocator locator, SeleniumContext ctx)
    {
        super(ctx);
        this.locator = notNull("locator", locator);
    }

    @Override
    protected SeleniumLocator detector()
    {
        return locator;
    }

    @Override
    public Locator locator()
    {
        return locator;
    }

    @Override
    public Input type(KeySequence keys)
    {
        locator.element().type(keys);
        return this;
    }

    @Override
    public TimedQuery<String> value()
    {
        return locator.element().value();
    }
}
