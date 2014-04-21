package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.component.Checkbox;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.AbstractLocatorBasedTimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * Selenium object for checkboxes.
 *
 * @since v4.3
 */
public class SeleniumCheckbox extends AbstractLocatorBasedPageObject implements Checkbox
{
    private final SeleniumLocator locator;
    private final Timeouts defaultTimeout;

    public SeleniumCheckbox(SeleniumLocator locator, SeleniumContext ctx)
    {
        this(locator, ctx, Timeouts.COMPONENT_LOAD);
    }

    public SeleniumCheckbox(SeleniumLocator locator, SeleniumContext ctx, Timeouts defaultTimeout)
    {
        super(ctx);
        this.locator = locator.withDefaultTimeout(defaultTimeout);
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Main locator of this page object. Its presence means that this object is ready to be manipulated in the test.
     *
     * @return main locator of this page object
     */
    @Override
    protected SeleniumLocator detector()
    {
        return locator;
    }

    /**
     * Returns a boolean indicating whether this checkbox is checked.
     *
     * @return a boolean indicating whether this checkbox is checked
     */
    @Override
    public TimedCondition checked()
    {
        return Conditions.and(isReady(), new AbstractLocatorBasedTimedCondition(context, locator)
        {
            @Override
            public boolean now()
            {
                return client.isChecked(locator);
            }
        });
    }

    /**
     * Sets the checked status of this checkbox.
     *
     * @return this
     */
    @Override
    public Checkbox toggle()
    {
        locator().element().click();
        return this;
    }

    /**
     * Locator unambiguously locating this page object.
     *
     * @return locator
     */
    @Override
    public Locator locator()
    {
        return locator;
    }
}
