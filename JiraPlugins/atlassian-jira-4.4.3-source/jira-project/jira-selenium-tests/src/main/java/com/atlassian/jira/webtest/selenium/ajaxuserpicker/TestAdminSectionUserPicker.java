package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser(browsers={Browser.IE}) //Time out issue - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestAdminSectionUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String ITEM_KEY = "_i_";
    private static final String USER_NAMES = "userNames";
    private static final String USER_NAMES_KEY = USER_NAMES + ITEM_KEY;

    public static Test suite()
    {
        return suiteFor(TestAdminSectionUserPicker.class);
    }

    public void onSetUp()
    {
        log("setting up TestAdminSectionUserPicker");
        super.onSetUp();
        log("setting up TestAdminSectionUserPicker complete");
    }

    public void testEditDefaultRoleMembership() throws InterruptedException
    {
        getNavigator().gotoAdmin();
        client.click("project_role_browser", true);
        client.click("manage_Users", true);
        client.click("edit_Users_atlassian-user-role-actor", true);

        //add single
        client.typeWithFullKeyEvents(USER_NAMES, "wa");
        waitFor(800);
        client.typeWithFullKeyEvents(USER_NAMES, "ter", false);
        
        assertThat.visibleByTimeout(USER_NAMES_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(USER_NAMES_KEY, new String[]{"waterm", "rastusw"});
        client.click(USER_NAMES_KEY + "waterm");

        client.click("add", true);
        assertTrue(client.isTextPresent("Water Seed"));

        //add multiple
        client.typeWithFullKeyEvents(USER_NAMES, "john");
        assertThat.visibleByTimeout(USER_NAMES_KEY + "johnr", DROP_DOWN_WAIT);
        assertUsersShown(USER_NAMES_KEY, new String[]{"johnr", "johnw"});
        client.click(USER_NAMES_KEY + "johnr");
        client.typeWithFullKeyEvents(USER_NAMES, "fre", false);
        assertThat.visibleByTimeout(USER_NAMES_KEY + "fred", DROP_DOWN_WAIT);
        client.click(USER_NAMES_KEY + "fred");

        client.click("add", true);

        assertTrue(client.isTextPresent("Water Seed"));
        assertTrue(client.isTextPresent("Fred Normal"));
        assertTrue(client.isTextPresent("John Rotten"));

        //test no result
        client.typeWithFullKeyEvents(USER_NAMES, "blargh");
        assertFalse(client.isVisible("class=suggestions"));

        client.click("add", true);
    }

    //quick test to ensure that no dropdown is shown in the edit group screen.
    public void testEditDefaultGroupRoleMembership() throws InterruptedException
    {
        getNavigator().gotoAdmin();
        client.click("project_role_browser", true);
        client.click("manage_Users", true);
        client.click("edit_Users_atlassian-group-role-actor", true);

        //add single
        client.typeWithFullKeyEvents("groupNames", "water");
        waitFor(DROP_DOWN_WAIT);
        assertFalse(client.isElementPresent("class=suggestions"));
        client.click("add", true);

    }

    public void testEditWatchers() throws InterruptedException
    {
        getNavigator().gotoPage("secure/ManageWatchers!default.jspa?id=10000", true);
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        //add single
        client.typeWithFullKeyEvents(USER_NAMES, "wa");
        waitFor(800);
        client.typeWithFullKeyEvents(USER_NAMES, "ter", false);
        assertThat.visibleByTimeout(USER_NAMES_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(USER_NAMES_KEY, new String[]{"waterm", "rastusw"});
        client.click(USER_NAMES_KEY + "waterm");

        client.click("add", true);
        assertTrue(client.isTextPresent("Water Seed"));

        //add multiple
        client.typeWithFullKeyEvents(USER_NAMES, "john");
        assertThat.visibleByTimeout(USER_NAMES_KEY + "johnr", DROP_DOWN_WAIT);
        assertUsersShown(USER_NAMES_KEY, new String[]{"johnr", "johnw"});
        client.click(USER_NAMES_KEY + "johnr");
        client.typeWithFullKeyEvents(USER_NAMES, "fre", false);
        assertThat.visibleByTimeout(USER_NAMES_KEY + "fred", DROP_DOWN_WAIT);
        client.click(USER_NAMES_KEY + "fred");

        client.click("add", true);

        assertTrue(client.isTextPresent("Water Seed"));
        assertTrue(client.isTextPresent("Fred Normal"));
        assertTrue(client.isTextPresent("John Rotten"));

        //test no result
        client.typeWithFullKeyEvents(USER_NAMES, "blargh");
        assertFalse(client.isVisible("class=suggestions"));
        client.click("add", true);
    }

    public void testEditWatchersNoPermission() throws InterruptedException
    {
        //add the no userpicker user to the administrators group.
        getAdministration().addUserToGroup(NOUSERPICKERUSER, "jira-administrators");

        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);

        getNavigator().gotoPage("secure/ManageWatchers!default.jspa?id=10000", true);

        //add single
        client.typeWithFullKeyEvents(USER_NAMES, "water");
        assertThat.elementNotPresentByTimeout("class=suggestions", DROP_DOWN_WAIT);
        client.click("add", true);

    }

    public void testEditDefaultRoleMembershipWithNoUserPickerPermission() throws InterruptedException
    {
        log("trying add user to group");
        //add the no userpicker user to the administrators group.
        getAdministration().addUserToGroup(NOUSERPICKERUSER, "jira-administrators");
        log("added user to group");

        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);

        getNavigator().gotoAdmin();
        client.click("project_role_browser", true);
        client.click("manage_Users", true);
        client.click("edit_Users_atlassian-user-role-actor", true);
        client.typeWithFullKeyEvents(USER_NAMES, "water");
        waitFor(DROP_DOWN_WAIT);
        assertFalse(client.isElementPresent("class=suggestions"));
        client.click("add", true);

    }
}
