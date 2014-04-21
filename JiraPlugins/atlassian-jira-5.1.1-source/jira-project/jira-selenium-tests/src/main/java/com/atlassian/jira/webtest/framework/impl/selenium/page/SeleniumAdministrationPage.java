package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.SeleniumGeneralConfiguration;
import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks.SeleniumAppLinksAdminPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins.SeleniumPlugins;
import com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard.DashboardImpl;
import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.admin.AdminPage;
import com.atlassian.jira.webtest.framework.page.admin.ViewGeneralConfiguration;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

import java.util.Map;

/**
 * Selenium implementation of the {@link SeleniumAdministrationPage}.
 *
 * @since v4.3
 */
public class SeleniumAdministrationPage extends SeleniumAbstractGlobalPage<AdministrationPage>
        implements AdministrationPage
{
    private static final String DETECTOR = "a#leave_admin";
    private static final String GLOBAL_LINK = "admin_link";

    private static final Map<Class<?>, Class<?>> ADMIN_PAGE_MAPPINGS = MapBuilder.<Class<?>,Class<?>>newBuilder()
            .add(ViewGeneralConfiguration.class, SeleniumGeneralConfiguration.class)
            .add(Plugins.class, SeleniumPlugins.class)
            .add(AppLinksAdminPage.class, SeleniumAppLinksAdminPage.class)
            .toMap();

    private final SeleniumLocator pageLocator;
    private final SeleniumLocator globalLinkLocator;

    public SeleniumAdministrationPage(SeleniumContext context)
    {
        super(AdministrationPage.class, context);
        pageLocator = css(DETECTOR);
        globalLinkLocator = id(GLOBAL_LINK);
    }


    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator linkLocator()
    {
        return globalLinkLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return pageLocator;
    }

    /* --------------------------------------------- TRANSITIONS ---------------------------------------------------- */

    public <T extends AdminPage> T goToPage(Class<T> pageType)
    {
        //need to click on all dropdowns to make sure the links beneath work
        clickLinkIfPresent("admin_project_menu");
        clickLinkIfPresent("admin_plugins_menu");
        clickLinkIfPresent("admin_users_menu");
        clickLinkIfPresent("admin_issues_menu");
        clickLinkIfPresent("admin_system_menu");
        clickLinkIfPresent("system.admin");

        T instance = instantiate(pageType);
        instance.adminLinkLocator().element().click();
        client.waitForPageToLoad();
        return instance;
    }

    private void clickLinkIfPresent(final String linkId)
    {
        if(client.isElementPresent(linkId))
        {
            client.click(linkId);
        }
    }

    @Override
    public Dashboard backToJira()
    {
        client.click("leave_admin");
        waitFor().pageLoad();
        return new DashboardImpl(context);
    }

    private <T extends AdminPage> T instantiate(final Class<T> pageType)
    {
        // TODO this is quite fragile, some factory would be nice as we add more admin pages
        try
        {
            return getSeleniumImplClass(pageType).getConstructor(SeleniumContext.class).newInstance(context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error instantiating AdminPage of type <" + pageType.getName() + ">", e);
        }
    }


    @SuppressWarnings ({ "unchecked" })
    private <T extends AdminPage> Class<T> getSeleniumImplClass(Class<T> pageType) throws ClassNotFoundException
    {
        if (ADMIN_PAGE_MAPPINGS.containsKey(pageType))
        {
            return (Class<T>) ADMIN_PAGE_MAPPINGS.get(pageType);
        }
        return (Class<T>) Class.forName(getClass().getPackage().getName() + ".admin.Selenium" + pageType.getSimpleName());
    }
}
