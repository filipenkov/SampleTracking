package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.ApplicationLink;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;

/**
 * Selenium-backed implementation of the AppLinksAdminPage.
 *
 * @since v4.3
 */
public class SeleniumAppLinksAdminPage extends AbstractSeleniumPage implements AppLinksAdminPage
{
    public static final String NTH_ROW_SELECTOR = "tr.ual-row:nth-child(%d)";

    private final SeleniumLocator detector;
    private final Locator adminLinkDetector;
    private final Locator addAppLinkLocator;
    private final NewAppLinkWizard wizardDialog;

    public SeleniumAppLinksAdminPage(SeleniumContext ctx)
    {
        super(ctx);
        detector = css("div.applinks-header");
        adminLinkDetector = id("ual");
        addAppLinkLocator = id("add-application-link");
        wizardDialog = new SeleniumNewAppLinksWizard(ctx, this);
    }

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }

    @Override
    public Locator adminLinkLocator()
    {
        return adminLinkDetector;
    }

    @Override
    public NewAppLinkWizard clickAddApplicationLink()
    {
        addAppLinkLocator.element().click();
        return wizardDialog;
    }

    @Override
    public ApplicationLink applicationLink(String applicationBaseURL)
    {
        Element visibleTable = jQuery("#application-links-table:visible").element();
        if (!visibleTable.isPresent().by(10000))
        {
            throw new IllegalStateException("Not present: " + visibleTable);
        }

//        final SeleniumLocator rowLocator = jQuery(String.format("#ual-row-%s", escapeJQuery(applicationBaseURL)));
//        return new SeleniumApplicationLinkRow(rowLocator, context());

        // iterate through all the rows, checking the URL
        for (int i = 1; ; i++)
        {
            SeleniumLocator baseUrlCell = jQuery(String.format(NTH_ROW_SELECTOR, i) + " td.application-url");
            String txt = baseUrlCell.element().text().now();
            if (txt == null)
            {
                return null;
            }

            if (applicationBaseURL.equals(txt))
            {
                return new SeleniumApplicationLink(jQuery(String.format(NTH_ROW_SELECTOR, i)), context(), this);
            }
        }
    }
}
