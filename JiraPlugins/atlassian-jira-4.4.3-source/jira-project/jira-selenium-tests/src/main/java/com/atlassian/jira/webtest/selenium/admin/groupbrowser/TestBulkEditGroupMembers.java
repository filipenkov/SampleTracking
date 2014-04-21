package com.atlassian.jira.webtest.selenium.admin.groupbrowser;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestBulkEditGroupMembers extends JiraSeleniumTest
{
    private static final String PLEASE_REFRESH_MEMBERS_LIST = "Newly selected group(s) may have different members.";
    private static final String ASSIGN = "assign";
    private static final String FIELD_USERS_TO_ASSIGN = "usersToAssignStr";
    private static final String FIELD_SELECTED_GROUPS = "selectedGroupsStr";

    private static final String ERROR_CANNOT_ADD_USER_INVALID = "Cannot add user. 'invalid' does not exist";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_ALL = "Cannot add user 'admin', user is already a member of all the selected group(s)";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN = "Cannot add user 'admin', user is already a member of 'jira-administrators'";

    public static Test suite()
    {
         return suiteFor(TestBulkEditGroupMembers.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestBulkEditGroupMembers.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testBulkEditGroupMembersPruning()
    {
        gotoBulkEditGroupMembers();

        //select groups and add invalid user
        selectUsersAndDevelopersGroup();
        client.type(FIELD_USERS_TO_ASSIGN, "invalid");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_CANNOT_ADD_USER_INVALID);
        assertAndPruneErroneousNames("invalid", "");

        //add a existing member to a group
        selectAdminGroupOnly();
        client.type(FIELD_USERS_TO_ASSIGN, "admin");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);
        assertAndPruneErroneousNames("admin", "");

        //add a existing member to multiple groups
        selectUsersAndDevelopersGroup();
        client.type(FIELD_USERS_TO_ASSIGN, "admin");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL);
        assertAndPruneErroneousNames("admin", "");

        //add a existing member and non existing member to a group
        selectAdminGroupOnly();
        client.type(FIELD_USERS_TO_ASSIGN, "admin, dev");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);
        assertAndPruneErroneousNames("admin, dev", "dev");

        //add a existing member and non existing member to all groups
        selectAllGroups();
        client.type(FIELD_USERS_TO_ASSIGN, "admin, dev");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL);
        assertAndPruneErroneousNames("admin, dev", "dev");

        //attempt to add various user members and test pruning a large set of usernames
        selectAdminGroupOnly();
        client.type(FIELD_USERS_TO_ASSIGN, "user, admin, duplicate, invalid, dev, duplicate, duplicate, error, user");
        getNavigator().clickAndWaitForPageLoad(ASSIGN);
        assertThat.textPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);
        assertThat.textPresent("Cannot add user. 'duplicate' does not exist");
        assertThat.textPresent("Cannot add user. 'invalid' does not exist");
        assertThat.textPresent("Cannot add user. 'duplicate' does not exist");
        assertThat.textPresent("Cannot add user. 'duplicate' does not exist");
        assertThat.textPresent("Cannot add user. 'error' does not exist");
        assertAndPruneErroneousNames("user, admin, duplicate, invalid, dev, duplicate, duplicate, error, user", "user, dev, user");
    }

    private void selectAdminGroupOnly()
    {
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-administrators");
        client.removeSelection(FIELD_SELECTED_GROUPS, "label=jira-developers");
        client.removeSelection(FIELD_SELECTED_GROUPS, "label=jira-users");
        assertThat.textPresent(PLEASE_REFRESH_MEMBERS_LIST);
        clickRefresh();
        assertThat.textPresent("Selected 1 of 5 Groups");
    }

    private void selectUsersAndDevelopersGroup()
    {
        client.removeSelection(FIELD_SELECTED_GROUPS, "label=jira-administrators");
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-developers");
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-users");
        assertThat.textPresent(PLEASE_REFRESH_MEMBERS_LIST);
        clickRefresh();
        assertThat.textPresent("Selected 2 of 5 Groups");
    }

    private void selectAllGroups()
    {
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-administrators");
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-developers");
        client.addSelection(FIELD_SELECTED_GROUPS, "label=jira-users");
        assertThat.textPresent(PLEASE_REFRESH_MEMBERS_LIST);
        clickRefresh();
        assertThat.textPresent("Selected 3 of 5 Groups");
    }

    private void assertAndPruneErroneousNames(String originalInput, String expectedInput)
    {
        assertThat.textPresent("remove the erroneous names");
        assertThat.elementPresent("prune");
        assertThat.formElementEquals(FIELD_USERS_TO_ASSIGN,originalInput);
        //only click on the prune link as the page does not need to be reloaded
        client.click("prune");
        //check that the text area value has changed
        assertThat.formElementEquals(FIELD_USERS_TO_ASSIGN, expectedInput);
        //and check that the actual prune panel and links are hidden from the user
        //This could not be tested in the basic func-test as it is always there in the html source
        assertThat.elementNotVisible("prune");
        assertThat.elementNotVisible("prunePanel");
    }

    private void gotoBulkEditGroupMembers()
    {
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("group_browser");
        getNavigator().clickAndWaitForPageLoad("bulk_edit_groups");
        assertThat.textPresent("This page allows you to edit the user memberships for each group.");
        assertThat.textPresent("Selected 0 of 5 Groups");
        assertThat.textPresent("No users in selected group(s)");
    }

    private void clickRefresh()
    {
        getNavigator().clickAndWaitForPageLoad("refresh-dependant-fields");
    }
}
