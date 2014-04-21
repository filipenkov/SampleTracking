package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.ApplicationLink;
import com.atlassian.jira.webtest.framework.page.admin.applinks.DeleteApplicationLink;

/**
 * Selenium-backed implementation of ApplicationLink.
 *
 * @since v4.3
 */
public class SeleniumApplicationLink extends AbstractLocatorBasedPageObject implements ApplicationLink
{
    private final SeleniumLocator rowLocator;
    private final AppLinksAdminPage adminPage;
    private final DeleteApplicationLink deleteApplicationLinkDialog;
    private final SeleniumLocator deleteLocator;

    public SeleniumApplicationLink(SeleniumLocator rowLocator, SeleniumContext ctx, AppLinksAdminPage adminPage)
    {
        super(ctx);
        this.rowLocator = rowLocator;
        this.adminPage = adminPage;
        deleteApplicationLinkDialog = new SeleniumDeleteApplicationLink(ctx, this.adminPage);
        deleteLocator = rowLocator.combine(css(".app-delete-link"));
    }

    @Override
    public TimedQuery<String> name()
    {
        return rowLocator.combine(css("td.application-name")).element().text();
    }

    @Override
    public TimedCondition isNotPresent()
    {
        return Conditions.not(isReady());
    }

    @Override
    public TimedQuery<String> baseURL()
    {
        return rowLocator.combine(css("td.application-url a")).element().text();
    }

    @Override
    public boolean checkForConfiguredAuthenticationType(AuthType authType)
    {
        if (authType == AuthType.NONE)
        {
            return getConfiguredAuthLocator().combine(jQuery("div")).element().text().byDefaultTimeout().equals("none");
        }
        return getConfiguredAuthLocator().combine(jQuery("a[data-auth-type='" + authType.getAuthProviderClass() + "']")).element().isPresent().byDefaultTimeout();
    }

    /**
     * Clicks the "Delete" link on the
     *
     * @return a DeleteApplicationLink
     */
    @Override
    public DeleteApplicationLink clickDelete()
    {
        deleteLocator.element().click();
        return deleteApplicationLinkDialog;
    }

    private SeleniumLocator getConfiguredAuthLocator()
    {
        return rowLocator.combine(css("td.application-outgoing-authentication"));
    }

    @Override
    protected SeleniumLocator detector()
    {
        return rowLocator;
    }
}
