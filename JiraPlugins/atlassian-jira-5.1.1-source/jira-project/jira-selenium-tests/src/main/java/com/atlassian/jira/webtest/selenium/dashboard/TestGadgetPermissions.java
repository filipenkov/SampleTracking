package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.Dashboard;

@WebTest({Category.SELENIUM_TEST })
public class TestGadgetPermissions extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestGadgetPermissions.xml");
    }

    public void testGadgetPermissions()
    {
        //first we're logged in as admin on the the 'Mine' dashboard.  Should see all gadgets
        Dashboard dashboard = getNavigator().dashboard("10020");
        dashboard.view();
        assertEquals(7, dashboard.getGadgetCount());

        //intro
        assertThat.elementPresent("//h3[@id='gadget-10030-title']");
        //projects
        assertThat.elementPresent("//h3[@id='gadget-10031-title']");
        //fav filters
        assertThat.elementPresent("//h3[@id='gadget-10032-title']");
        //assigned to me
        assertThat.elementPresent("//h3[@id='gadget-10033-title']");
        // in progress
        assertThat.elementPresent("//h3[@id='gadget-10034-title']");
        //quicklinks
        assertThat.elementPresent("//h3[@id='gadget-10035-title']");
        //admin
        assertThat.elementPresent("//h3[@id='gadget-10036-title']");

        getNavigator().logout(getXsrfToken());
        dashboard = getNavigator().dashboard("10000");
        dashboard.view();
        assertEquals(2, dashboard.getGadgetCount());
        //intro
        assertThat.elementPresent("//h3[@id='gadget-10000-title']");
        //login
        assertThat.elementPresent("//h3[@id='gadget-0-title']");

        getNavigator().login("fred", "fred");
        dashboard = getNavigator().dashboard("10020");
        dashboard.view();
        assertEquals(6, dashboard.getGadgetCount());
        //intro
        assertThat.elementPresent("//h3[@id='gadget-10030-title']");
        //projects
        assertThat.elementPresent("//h3[@id='gadget-10031-title']");
        //fav filters
        assertThat.elementPresent("//h3[@id='gadget-10032-title']");
        //assigned to me
        assertThat.elementPresent("//h3[@id='gadget-10033-title']");
        // in progress
        assertThat.elementPresent("//h3[@id='gadget-10034-title']");
        //quicklinks
        assertThat.elementPresent("//h3[@id='gadget-10035-title']");
        //admin gadget shouldn't be on this dashboard since it's read-only!
        assertThat.elementNotPresent("//h3[@id='gadget-10036-title']");

        dashboard = getNavigator().dashboard("10021");
        dashboard.view();
        assertEquals(2, dashboard.getGadgetCount());
        //intro
        assertThat.elementPresent("//h3[@id='gadget-10037-title']");
        //admin gadget should show error condition
        assertThat.elementPresent("//h3[@id='gadget-10038-title']");
        client.selectFrame("gadget-10038");
        assertThat.textPresent("This gadget was uninstalled by the administrator. Please delete it from your dashboard.");

        client.selectWindow(null);
        //now add browse permissions to everyone
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);

        client.click("link=Default Permission Scheme", true);
        client.click("add_perm_10", true);
        client.click("type_group");
        client.click("document.jiraform.elements[' Add ']", true);

        //we should now see some 'browse' type gadgets when logged out.
        getNavigator().logout(getXsrfToken());
        dashboard = getNavigator().dashboard("10000");
        dashboard.view();
        assertEquals(4, dashboard.getGadgetCount());
        //intro
        assertThat.elementPresent("//h3[@id='gadget-10000-title']");
        //projects
        assertThat.elementPresent("//h3[@id='gadget-10001-title']");
        //login
        assertThat.elementPresent("//h3[@id='gadget-0-title']");
        //quicklinks
        assertThat.elementPresent("//h3[@id='gadget-10005-title']");
    }
}
