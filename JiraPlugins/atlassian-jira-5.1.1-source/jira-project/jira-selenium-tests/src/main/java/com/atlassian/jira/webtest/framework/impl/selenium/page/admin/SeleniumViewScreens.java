package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.model.admin.Screen;
import com.atlassian.jira.webtest.framework.page.admin.ConfigureScreen;
import com.atlassian.jira.webtest.framework.page.admin.ViewScreens;

/**
 * Represents the Screens' administration page.
 *
 * @since v4.2
 */
public class SeleniumViewScreens extends AbstractSeleniumPage implements ViewScreens
{
    private static final String DETECTOR = "#td.jiraformheader h3.formtitle:contains(View Screens)";
    private static final String ADMIN_LINK_LOCATOR = "field_screens";

    private final SeleniumLocator main;
    private final SeleniumLocator adminLink;


    public SeleniumViewScreens(SeleniumContext ctx)
    {
        super(ctx);
        this.main = jQuery(DETECTOR);
        this.adminLink = id(ADMIN_LINK_LOCATOR);
    }

    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    @Override
    protected SeleniumLocator detector()
    {
        return main;
    }

    @Override
    public Locator adminLinkLocator()
    {
        return adminLink;
    }

    @Override
    public Locator screenTableLocator()
    {
        return css("ul.operations-list");
    }

    @Override
    public Locator configureScreenLinkLocatorFor(Screen screen)
    {
        return screenTableLocator().combine(cssFor(screen));
    }

    private Locator cssFor(Screen screen)
    {
        return css(String.format("a.configure-fieldscreen[rel='%d']", screen.id()));
    }

    /* ------------------------------------------------ TRANSITIONS ------------------------------------------------- */

    @Override
    public ConfigureScreen goToConfigureScreen(Screen screen)
    {
        configureScreenLinkLocatorFor(screen).element().click();
        waitFor().pageLoad();
        return new SeleniumConfigureScreen(this, screen, context);
    }

}
