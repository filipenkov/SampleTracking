package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES })
public class TestMultiIssueTypes extends JIRAWebTest
{
    private static final String AUTO_CREATED_SCHEME_PREFIX = "New issue type scheme for project ";
    private static final String CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR = "#issue-create h2";
    private static final String VIEW_ISSUE_PAGE_ISSUE_TYPE_LABEL_ID = "type-val";

    public TestMultiIssueTypes(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testMultiIssueTypes() throws Exception
    {
        _testCreateNewIssueTypeScheme();
        _testSameAsProject();
        _testChooseScheme();
        _testIssueNavigator(); // Depends on data changes introduced by the previous test cases :-(
    }

    private void _testIssueNavigator()
    {
        logSection("Testing that choosing a different scheme is reflected in the issue navigator (must run after other tests)");
        navigation.issueNavigator().gotoNavigator();
        tester.setWorkingForm("issue-filter");
        tester.assertOptionsEqual("type", new String[] { ISSUE_TYPE_ANY, ISSUE_TYPE_TASK });


        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");

        tester.checkCheckbox("createType", "chooseScheme");
        tester.selectOption("schemeId", AUTO_CREATED_SCHEME_PREFIX + PROJECT_HOMOSAP);
        tester.submit(" OK ");

        navigation.issueNavigator().gotoNavigator();
        tester.setWorkingForm("issue-filter");
        tester.assertOptionsEqual("type", new String[] { ISSUE_TYPE_ANY, ISSUE_TYPE_BUG, ISSUE_TYPE_TASK, ISSUE_TYPE_IMPROVEMENT });
    }

    private void _testChooseScheme()
    {
        logSection("Testing choosing scheme from a list & that all available projects share the same config");

        gotoPage("/plugins/servlet/project-config/MKY/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");
        tester.checkCheckbox("createType", "createScheme");
        tester.selectOption("selectedOptions", "Task");
        tester.submit();

        gotoPage("/plugins/servlet/project-config/MKY/issuetypes");
        text.assertTextPresent(locator.id("project-config-issuetype-scheme-name"), AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY);

        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");
        tester.selectOption("schemeId", AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY);
        tester.submit();
        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");
        text.assertTextPresent(locator.id("project-config-issuetype-scheme-name"), AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY);

        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Task");
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");
    }

    private void _testSameAsProject()
    {
        logSection("Choose issue type scheme same as another project");

        tester.checkCheckbox("createType", "chooseProject");
        tester.selectOption("sameAsProjectId", PROJECT_MONKEY);
        tester.submit();
        assertTextPresent("Default Issue Type Scheme");

        // Test issue creation should pass
        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Task");
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");
    }

    private void _testCreateNewIssueTypeScheme()
    {
        logSection("Create a new issue type scheme");

        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");

        // Select options for the new scheme
        tester.checkCheckbox("createType", "createScheme");
        selectMultiOption("selectedOptions", "Bug");
        selectMultiOption("selectedOptions", "Improvement");
        tester.submit();

        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        text.assertTextPresent(locator.id("project-config-issuetype-scheme-name"), AUTO_CREATED_SCHEME_PREFIX + PROJECT_HOMOSAP);

        // Test issue creation should fail
        tester.clickLink("leave_admin");
        tester.clickLink("create_link");
        tester.selectOption("pid", PROJECT_HOMOSAP);
        tester.selectOption("issuetype", "Task");
        tester.submit();
        assertTextPresent("The issue type selected is invalid");
        tester.selectOption("issuetype", "Bug");
        tester.submit();
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");

        // Make the Homo project be the same as monkey
        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");
    }

    // Ensure that BULK CHANGE global permission is not required for issue type scheme association
    public void testMultipleIssueTypeSchemesWithoutBulkChangePermission()
    {
        administration.restoreData("TestIssueTypesSchemes.xml");
        administration.removeGlobalPermission(BULK_CHANGE, Groups.USERS);

        navigation.gotoAdminSection("issue_types");
        tester.setFormElement("name", "");
        tester.clickLinkWithText("Issue Types Scheme");
        tester.setFormElement("name", "");
        tester.clickLink("associate_10011");
        tester.selectOption("projects", "homosapien");
        tester.submit("Associate");
        tester.assertTextPresent("homosapien");
        tester.assertTextPresent("Bug");
        tester.assertTextPresent("1");
        tester.submit("nextBtn");
        tester.submit("nextBtn");
        tester.submit("nextBtn");
        tester.assertTextPresent("homosapien");
        tester.assertTextPresent("New Feature");
        tester.submit("nextBtn");

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(locator.id(VIEW_ISSUE_PAGE_ISSUE_TYPE_LABEL_ID), "New Feature");
    }
}
