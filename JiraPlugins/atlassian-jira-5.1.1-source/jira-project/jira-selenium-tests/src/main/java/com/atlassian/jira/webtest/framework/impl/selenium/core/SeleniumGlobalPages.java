package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.PageObjectFactory;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issue.SeleniumViewIssue;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.GlobalPages;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;
import com.atlassian.jira.webtest.selenium.harness.util.NavigatorImpl;

/**
 * Implementation of {@link com.atlassian.jira.webtest.framework.page.GlobalPages}.
 *
 * @since v4.3
 */
public class SeleniumGlobalPages extends SeleniumContextAware implements GlobalPages
{
    private static final String LEAVE_ADMIN_LINK_ID = "leave_admin";
    private final SeleniumLocator leaveAdminLinkLocator;
    private final PageObjectFactory factory;

    private final Navigator navigator;

    private final AdministrationPage admin;
    private final IssueNavigator issueNav;
    private final Dashboard dashboard;

    public SeleniumGlobalPages(SeleniumContext context)
    {
        super(context);
        this.navigator = new NavigatorImpl(context);
        this.factory = new SeleniumPageObjectFactory(context);
        this.admin = factory.createGlobalPage(AdministrationPage.class);
        this.issueNav = factory.createGlobalPage(IssueNavigator.class);
        this.dashboard = factory.createGlobalPage(Dashboard.class);
        this.leaveAdminLinkLocator = SeleniumLocators.id(LEAVE_ADMIN_LINK_ID, context);
    }

    @Override
    public IssueNavigator goToIssueNavigator()
    {
        if (leaveAdminLinkLocator.element().isPresent().byDefaultTimeout())
        {
            leaveAdminLinkLocator.element().click();
            client.waitForPageToLoad(context.timeoutFor(Timeouts.PAGE_LOAD));
        }
        return issueNav.goTo();
    }

    @Override
    public IssueNavigator issueNavigator()
    {
        return issueNav;
    }

    @Override
    public ViewIssue goToViewIssueFor(IssueData issueData)
    {
        navigator.gotoIssue(issueData.key());
        return new SeleniumViewIssue(context);
    }

    @Override
    public AdministrationPage goToAdministration()
    {
        if (leaveAdminLinkLocator.element().isPresent().byDefaultTimeout())
        {
            return admin;
        }
        return admin.goTo();
    }

    @Override
    public AdministrationPage administration()
    {
        return admin;
    }

    @Override
    public Dashboard goToDashboard()
    {
        return dashboard.goTo();
    }

    @Override
    public Dashboard dashboard()
    {
        return dashboard;
    }
}
