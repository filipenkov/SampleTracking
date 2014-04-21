package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Selenium Test for the Quicklinks gadget
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestQuicklinksGadget extends GadgetTest
{

    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Quick Links");
    }

    public void testView() throws InterruptedException
    {
        _testAdmin();
        _testNonAdmin();
        _testAnonymousUser();
        _testUserWithNoPermissions();
    }

    private void _testAdmin()
    {
        waitForGadgetView("quicklinks-content");

        waitFor(3000);
        assertThat.linkPresentWithText("My Unresolved Reported Issues");
        assertThat.linkPresentWithText("Administration");
        assertThat.linkPresentWithText("Create Issue");
        assertThat.linkPresentWithText("Browse Projects");
        assertThat.linkPresentWithText("Search for Issues");

        verifyFindIssuesShowsInEditMode();
    }

    private void verifyFindIssuesShowsInEditMode()
    {
        getSeleniumClient().selectFrame("relative=top");
        // go to issue navigator in edit mode
        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=show", true);
        // force to simple searching
        getNavigator().gotoPage("secure/IssueNavigator!switchView.jspa?navType=simple", true);
        // select all projects
        getSeleniumClient().select("pid", "label=All projects");
        // go to the view mode
        getSeleniumClient().click("id=issue-filter-submit", true);
        assertThat.elementPresentByTimeout("viewfilter", DROP_DOWN_WAIT);
        getSeleniumClient().click("viewfilter", true);


        // Now go to dashboard and select the quicklinks gadget
        getNavigator().gotoHome();
        selectGadget("Quick Links");
        waitForGadgetView("quicklinks-content");
        getSeleniumClient().clickLinkWithText("Search for Issues", true);
        // assert in edit mode
        assertThat.linkPresentWithText("advanced");
    }

    private void _testNonAdmin()
    {
        getSeleniumClient().selectFrame("relative=top");
        getNavigator().logout(getXsrfToken());
        getNavigator().login("fred", "fred");
        assertThat.linkNotPresentWithText("Administration");
    }

    private void _testAnonymousUser()
    {
        selectDashboardFrame();
        loginAsAdmin();
        getNavigator().gotoPage("secure/admin/jira/EditDefaultDashboard!default.jspa", true);
        addGadget("Quick Links");
        selectDashboardFrame();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        assertThat.textNotPresent("Quick Links");
    }

    private void _testUserWithNoPermissions()
    {
        getWebUnitTest().addUser("noperms", "noperms", "foo", "foo@bar.com");
        getWebUnitTest().createGroup("nopermsgrp");
        getWebUnitTest().removeUserFromGroup("noperms", "jira-users");
        getWebUnitTest().addUserToGroup("noperms", "nopermsgrp");
        getWebUnitTest().grantGlobalPermission(1, "nopermsgrp");

        getSeleniumClient().selectFrame("relative=top");
        getNavigator().logout(getXsrfToken());
        getNavigator().login("noperms", "noperms");
        assertThat.textNotPresent("Quick Links");
        assertThat.linkNotPresentWithText("Create New Issue");
        assertThat.linkNotPresentWithText("Browse Projects");
        assertThat.linkNotPresentWithText("Search for Issues");
    }
}
