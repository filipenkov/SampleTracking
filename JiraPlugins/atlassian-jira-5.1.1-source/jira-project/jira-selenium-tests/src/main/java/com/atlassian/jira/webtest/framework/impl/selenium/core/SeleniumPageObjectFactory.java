package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.PageObjectFactory;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.impl.selenium.component.SeleniumWebSudoBanner;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.impl.selenium.page.SeleniumAdministrationPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.SeleniumWebSudoLoginPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.CreateNewDashboardImpl;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.DashboardImpl;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator.SeleniumIssueNav;
import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.GlobalPage;
import com.atlassian.jira.webtest.framework.page.WebSudoBanner;
import com.atlassian.jira.webtest.framework.page.WebSudoLoginPage;
import com.atlassian.jira.webtest.framework.page.dashboard.CreateNewDashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;

import java.util.Map;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.core.PageObjectFactory}.
 *
 * @since v4.3
 */
public class SeleniumPageObjectFactory extends SeleniumContextAware implements PageObjectFactory
{
    private final Map<Class<?>, Class<?>> pageMappings = MapBuilder.<Class<?>, Class<?>>newBuilder()
            .add(AdministrationPage.class, SeleniumAdministrationPage.class)
            .add(IssueNavigator.class, SeleniumIssueNav.class)
            .add(Dashboard.class, DashboardImpl.class)
            .add(WebSudoBanner.class, SeleniumWebSudoBanner.class)
            .add(WebSudoLoginPage.class, SeleniumWebSudoLoginPage.class)
            .toMap();

    protected SeleniumPageObjectFactory(SeleniumContext context)
    {
        super(context);
    }

    public <T extends GlobalPage> T createGlobalPage(Class<T> pageType)
    {
        return createPageObject(pageType);
    }

    @Override
    public <P extends PageObject> P createPageObject(Class<P> pageType)
    {
        Class<?> targetClass = pageMappings.get(pageType);
        if (targetClass == null)
        {
            return hardcoded(pageType);
        }
        return pageType.cast(instantiate(targetClass));
    }

    private <P extends PageObject> P hardcoded(Class<P> pageType)
    {
        // TODO yes it's very funny, but we need _real_ DI and there is already solution in atlassian-selenium2 so no need
        // TODO do any fancy stuff here, just migrate it to use AS2 PageBinder
        if (pageType.equals(CreateNewDashboard.class))
        {
            return (P) new CreateNewDashboardImpl(context.getPageObject(Dashboard.class), context);
        }
        throw new IllegalStateException("No mapping for <" + pageType + ">");
    }

    private Object instantiate(Class<?> targetClass)
    {
        try
        {
            return targetClass.getConstructor(SeleniumContext.class).newInstance(context);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public Locator createLocator(LocatorType type, String value)
    {
        return SeleniumLocators.create(type, value, context);
    }


}
