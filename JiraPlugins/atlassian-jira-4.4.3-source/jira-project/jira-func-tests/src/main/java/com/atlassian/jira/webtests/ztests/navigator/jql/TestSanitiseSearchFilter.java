package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Jql func test to verify issue keys and project names get sanitized.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSanitiseSearchFilter extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSanitiseSearchFilter.xml");
    }

    public void testSanitiseIssueClause() throws Exception
    {
        runFilterWithIdAndVerifyJQL(10030, "Issue = \"ABC-4\" order by key ASC", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10031, "Issue != \"ABC-4\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10032, "Issue in (\"ABC-4\",\"ABC-3\")", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10033, "Issue not in (\"ABC-4\",\"ABC-3\")", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10034, "Issue <= \"ABC-4\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10035, "Issue < \"ABC-4\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10036, "Issue < \"ABC-3\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10037, "Issue >= \"ABC-1\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10038, "Issue > \"ABC-1\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10039, "Issue > \"ABC-4\"", "fieldJqlQuery");

        navigation.login(FRED_USERNAME);
        //Fred Doesn't have the Browse Project Permission for ABC-4
        runFilterWithIdAndVerifySanitisedJQL(10030, "Issue = 10015", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10031, "Issue != 10015", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10032, "Issue in (10015, ABC-3)", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10033, "Issue not in (10015, ABC-3)", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10034, "Issue <= 10015", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10035, "Issue < 10015", "A value with ID '10015' does not exist for the field 'Issue'");
        runFilterWithIdAndVerifySanitisedJQL(10039, "Issue > 10015", "A value with ID '10015' does not exist for the field 'Issue'");

        // valid
        runFilterWithIdAndVerifyJQL(10036, "Issue < \"ABC-3\"", "fieldJqlQuery");
        assertIssues("ABC-1", "ABC-2");
        runFilterWithIdAndVerifyJQL(10037, "Issue >= \"ABC-1\"", "fieldJqlQuery");
        assertIssues("ABC-1", "ABC-2", "ABC-3");
        runFilterWithIdAndVerifyJQL(10038, "Issue > \"ABC-1\"", "fieldJqlQuery");
        assertIssues("ABC-2", "ABC-3");
    }

    public void testSanitiseParentClause() throws Exception
    {
        runFilterWithIdAndVerifyJQL(10040, "Parent = \"MKY-1\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10041, "Parent != \"MKY-1\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10042, "Parent in (\"MKY-1\",\"HSP-1\")", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10043, "Parent not in (\"MKY-1\",\"HSP-1\")", "fieldJqlQuery");

        navigation.login(FRED_USERNAME);
        //Fred Doesn't have the Browse Project Permission for ABC-4 and Project Homosapien
        runFilterWithIdAndVerifyJQL(10040, "Parent = \"MKY-1\"", "fieldJqlQuery");
        assertIssues("MKY-2");
        runFilterWithIdAndVerifyJQL(10041, "Parent != \"MKY-1\"", "fieldJqlQuery");
        assertIssues("ABC-1", "ABC-2", "ABC-3", "MKY-1");

        runFilterWithIdAndVerifySanitisedJQL(10042, "Parent in (MKY-1, 10000)", "A value with ID '10000' does not exist for the field 'Parent'");
        runFilterWithIdAndVerifySanitisedJQL(10043, "Parent not in (MKY-1, 10000)", "A value with ID '10000' does not exist for the field 'Parent'");
    }

    public void testSanitiseProjectClause() throws Exception
    {
        runFilterWithIdAndVerifyJQL(10044, "homosapien", "fieldpid");
        runFilterWithIdAndVerifyJQL(10045, "Project != \"Homosapien\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10046, "homosapien, monkey", "fieldpid");
        runFilterWithIdAndVerifyJQL(10047, "Project not in (\"Homosapien\",\"Monkey\")", "fieldJqlQuery");

        navigation.login(FRED_USERNAME);

        runFilterWithIdAndVerifySanitisedJQL(10044, "Project = 10000", "A value with ID '10000' does not exist for the field 'Project'.");
        runFilterWithIdAndVerifySanitisedJQL(10045, "Project != 10000", "A value with ID '10000' does not exist for the field 'Project'.");
        runFilterWithIdAndVerifySanitisedJQL(10046, "Project in (10000, Monkey) ORDER BY type ASC", "A value with ID '10000' does not exist for the field 'Project'.");
        runFilterWithIdAndVerifySanitisedJQL(10047, "Project not in (10000, Monkey)", "A value with ID '10000' does not exist for the field 'Project'.");
    }

    public void testSanitiseProjectPickerClause() throws Exception
    {
        runFilterWithIdAndVerifyJQL(10050, "homosapien", "fieldcustomfield_10000");
        runFilterWithIdAndVerifyJQL(10051, "ProjectPicker != \"Homosapien\"", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10052, "ProjectPicker in (\"Homosapien\", \"Monkey\")", "fieldJqlQuery");
        runFilterWithIdAndVerifyJQL(10053, "ProjectPicker not in (\"Homosapien\", \"Monkey\")", "fieldJqlQuery");

        navigation.login(FRED_USERNAME);

        runFilterWithIdAndVerifySanitisedJQL(10050, "ProjectPicker = 10000", "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        runFilterWithIdAndVerifySanitisedJQL(10051, "ProjectPicker != 10000", "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        runFilterWithIdAndVerifySanitisedJQL(10052, "ProjectPicker in (10000, Monkey)", "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
        runFilterWithIdAndVerifySanitisedJQL(10053, "ProjectPicker not in (10000, Monkey)", "A value with ID '10000' does not exist for the field 'ProjectPicker'.");
    }

    private void runFilterWithIdAndVerifyJQL(int filterId, String jqlString, final String id)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        text.assertTextPresent(new IdLocator(tester, id), jqlString);
    }

    private void runFilterWithIdAndVerifySanitisedJQL(int filterId, String jqlString, final String... errors)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        text.assertTextPresent(new IdLocator(tester, "fieldJqlQuery"), jqlString);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        text.assertTextPresent(new IdLocator(tester, "jqltext"), jqlString);
        assertions.getIssueNavigatorAssertions().assertJqlErrors(errors);
    }

}
