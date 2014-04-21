package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.WebSudoBanner;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.*;

/*
 * @since 4.3
 */
public class SeleniumWebSudoBanner extends AbstractLocatorBasedPageObject implements WebSudoBanner
{
    final private Locator bannerLocator;
    final private Locator protectedLinkLocator;
    final private Locator normalLinkLocator;

    public SeleniumWebSudoBanner (SeleniumContext seleniumContext)
    {
        super(seleniumContext);
        bannerLocator = id("websudo-banner").withDefaultTimeout(Timeouts.COMPONENT_LOAD);
        protectedLinkLocator = id("websudo-drop-from-protected-page");
        normalLinkLocator = id("websudo-drop-from-normal-page");
    }

    public Locator locator()
    {
        return bannerLocator;
    }

    public Locator detector()
    {
        return bannerLocator;
    }

    public WebSudoBanner dropWebSudo()
    {
        if (protectedLinkLocator.element().isPresent().byDefaultTimeout())
        {
            protectedLinkLocator.element().click();
        }
        else
        {
            normalLinkLocator.element().click();
        }

        return this;
    }

    public TimedCondition protectedLinkIsPresent()
    {
        return protectedLinkLocator.element().isPresent();
    }

    public TimedCondition normalLinkIsPresent()
    {
        return normalLinkLocator.element().isPresent();
    }

    public TimedCondition isPresent()
    {
        return bannerLocator.element().isPresent();
    }

    public TimedCondition isVisible()
    {
        // casting is bad
        // but apparently this will be fixed in a month when we move to WebDriver
        return and(isPresent(),
                   not(conditions().hasClass((SeleniumLocator) bannerLocator, "dropped")));
    }
}
