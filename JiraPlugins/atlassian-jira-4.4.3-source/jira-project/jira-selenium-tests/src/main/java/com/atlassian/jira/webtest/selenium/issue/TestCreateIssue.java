package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * This is a test case to prove the functionality of the JavaScript that
 * selects the Issue Type based on the selected option in the Project
 * select box. It also verifies that the correct default issue type is
 * selected by default.
 *
 * @since 3.12.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestCreateIssue extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestCreateIssue.xml");
    }

    public void onTearDown()
    {
        restoreBlankInstance();
    }

    public void testDefaultIssueTypeSelected() throws Exception
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);

        // Test project
        client.select("project", "Test");
        client.select("issuetype", "Improvement");
        client.select("issuetype", "Task");
        assertTestProjectIssueTypes();

        // Holiday project
        client.select("project", "Holiday");
        client.select("issuetype", "New Feature");
        client.select("issuetype", "Task");
        assertHolidayProjectIssueTypes();

        // Test project
        client.select("project", "Test");
        client.select("issuetype", "Improvement");
        client.select("issuetype", "Task");
        assertTestProjectIssueTypes();
    }

    public void testCreateIssueInHolidayProject() throws Exception
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        client.select("project", "Holiday");
        createIssueAndAssertDefaultIssueType("Holiday", "New Feature", "Green Sky");
    }

    public void testCreateIssueInTestProject() throws Exception
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        client.select("project", "Test");
        createIssueAndAssertDefaultIssueType("Test", "Improvement", "Blue Sky");
    }

    //JRA-15994: Need to test that issuetypes show up correctly, if the fieldconfigscheme id is different than the
    // fieldconfiguration id
    public void testCreateIssueWithFieldConfigSchemeIdsDifferent()
    {
        restoreData("TestFieldConfigurationIdCreateIssue.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        client.select("project", "homosapien");
        assertThat.elementVisible("issuetype10011");
        assertThat.elementVisible("issuetype100111");
        assertThat.elementVisible("issuetype100112");
        assertThat.elementVisible("issuetype100113");
        assertThat.elementVisible("issuetype100114");
        assertThat.elementNotPresent("issuetype10010");
        assertThat.elementNotPresent("issuetype100105");
        assertThat.elementNotPresent("issuetype100106");

        client.select("project", "monkey");
        assertThat.elementVisible("issuetype10010");
        assertThat.elementVisible("issuetype100105");
        assertThat.elementVisible("issuetype100106");
        assertThat.elementNotPresent("issuetype10011");
        assertThat.elementNotPresent("issuetype100111");
        assertThat.elementNotPresent("issuetype100112");
        assertThat.elementNotPresent("issuetype100113");
        assertThat.elementNotPresent("issuetype100114");
    }

    private void assertTestProjectIssueTypes()
    {
        assertThat.elementNotPresent("issuetype100103"); // Holiday:Task
        assertThat.elementNotPresent("issuetype100102"); // Holiday:New Feature
        assertThat.elementVisible("issuetype100113"); // Test:Task
        assertThat.elementVisible("issuetype100114"); // Test:Improvement
    }

    private void assertHolidayProjectIssueTypes()
    {
        assertThat.elementVisible("issuetype100103"); // Holiday:Task
        assertThat.elementVisible("issuetype100102"); // Holiday:New Feature
        assertThat.elementNotPresent("issuetype100113"); // Test:Task
        assertThat.elementNotPresent("issuetype100114"); // Test:Improvement
    }

    private void createIssueAndAssertDefaultIssueType(String project, String defaultIssueType, String summary)
    {
        client.click("Next", true);
        assertThat.textPresent(project);
        assertThat.textPresent(defaultIssueType);

        client.type("summary", summary);
        client.click("Create", true);
        assertThat.textPresent("Details");
        assertThat.textPresent(project);
        assertThat.textPresent(defaultIssueType);
        assertThat.textPresent(summary);
    }

}
