package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.page.admin.ViewGeneralConfiguration;

/**
 * Represents the 'Global configuration' administration page.
 *
 * @since v4.2
 */
public class SeleniumGeneralConfiguration extends AbstractSeleniumPage implements ViewGeneralConfiguration
{
    // luckily somebody honoured one row on this poor unidentified page with an ID
    private static final String DETECTOR = "tr#maximumAuthenticationAttemptsAllowed";
    private static final String ADMIN_LINK = "general_configuration";

    private final SeleniumLocator main;
    private final SeleniumLocator adminLink;

    public SeleniumGeneralConfiguration(SeleniumContext ctx)
    {
        super(ctx);
        this.main = css(DETECTOR); 
        this.adminLink = id(ADMIN_LINK);
    }

    /* ------------------------------------------------ LOCATORS ---------------------------------------------------- */

    @Override
    protected SeleniumLocator detector()
    {
        return main;
    }

    public SeleniumLocator adminLinkLocator()
    {
        return adminLink;
    }

    private SeleniumLocator editLinkLocator()
    {
        return id("edit-app-properties");
    }

    /* ------------------------------------------------ TRANSITIONS ------------------------------------------------- */

    @Override
    public SeleniumEditGeneralConfiguration edit()
    {
        editLinkLocator().element().click();
        waitFor().pageLoad();
        return new SeleniumEditGeneralConfiguration(context, this);
    }
}
