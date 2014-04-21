package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ContainsTextCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.ContainsValueCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.HasClassCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsInWindowCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsVisibleCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * For simpler instantiation of Selenium conditions.
 *
 * @since v4.3
 */
public final class SeleniumConditions extends SeleniumContextAware
{

    protected SeleniumConditions(SeleniumContext context)
    {
        super(context);
    }

    public IsPresentCondition.Builder isPresentBuilder(SeleniumLocator locator)
    {
        return IsPresentCondition.forContext(context).locator(locator).defaultTimeout(locator.defaultTimeout());
    }

    public IsPresentCondition isPresent(SeleniumLocator locator)
    {
        return isPresentBuilder(locator).build();
    }

    public IsVisibleCondition.Builder isVisibleBuilder(SeleniumLocator locator)
    {
        return IsVisibleCondition.forContext(context).locator(locator).defaultTimeout(locator.defaultTimeout());
    }

    public IsVisibleCondition isVisible(SeleniumLocator locator)
    {
        return isVisibleBuilder(locator).build();
    }

    public ContainsTextCondition.Builder containsTextBuilder(SeleniumLocator locator, String text)
    {
        return ContainsTextCondition.forContext(context).locator(locator).expectedValue(text)
                .defaultTimeout(locator.defaultTimeout());
    }

    public ContainsTextCondition containsText(SeleniumLocator locator, String text)
    {
        return containsTextBuilder(locator, text).build();
    }

    public ContainsValueCondition.Builder hasValueBuilder(SeleniumLocator locator, String value)
    {
        return ContainsValueCondition.forContext(context).locator(locator).expectedValue(value)
                .defaultTimeout(locator.defaultTimeout());
    }

    public ContainsValueCondition hasValue(SeleniumLocator locator, String value)
    {
        return hasValueBuilder(locator, value).build();
    }

    public HasClassCondition.Builder hasClassBuilder(SeleniumLocator locator, String className)
    {
        return HasClassCondition.forContext(context).locator(locator).cssClass(className).defaultTimeout(locator.defaultTimeout());
    }

    public HasClassCondition hasClass(SeleniumLocator locator, String className)
    {
        return hasClassBuilder(locator, className).build();
    }

    public IsInWindowCondition.Builder inWindowBuilder(String windowId)
    {
        return IsInWindowCondition.forContext(context).windowId(windowId).defaultTimeout(Timeouts.AJAX_ACTION);
    }

    public IsInWindowCondition inWindow(String windowId)
    {
        return inWindowBuilder(windowId).build();
    }
}
