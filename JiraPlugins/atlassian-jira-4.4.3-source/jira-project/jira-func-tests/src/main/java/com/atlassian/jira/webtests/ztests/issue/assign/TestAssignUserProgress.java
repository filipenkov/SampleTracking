package com.atlassian.jira.webtests.ztests.issue.assign;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestAssignUserProgress extends FuncTestCase
{
    public static final String TEST_SUMMARY = "testing progress inconsistency";
    public static final String IN_PROGRESS = "In Progress";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testChangeProgressWithAssign()
    {
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        administration.usersAndGroups().addUserToGroup(BOB_USERNAME, "jira-developers");

        administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        final String key = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_SUMMARY);

        navigation.issue().viewIssue(key);
        tester.clickLinkWithText("Start Progress");
        navigation.issue().assignIssue(key, TEST_SUMMARY, BOB_FULLNAME);

        text.assertTextPresent(locator.page(), IN_PROGRESS);
    }

    public void testChangeProgressWithEdit()
    {
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        administration.usersAndGroups().addUserToGroup(BOB_USERNAME, "jira-developers");

        administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        final String key = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_SUMMARY);

        navigation.issue().viewIssue(key);
        tester.clickLinkWithText("Start Progress");

        tester.clickLink("editIssue");
        tester.selectOption("assignee", BOB_FULLNAME);
        tester.submit("Update");

        text.assertTextPresent(locator.page(), IN_PROGRESS);
    }
}