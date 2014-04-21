package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestIssueNavigatorAdvancedUrlCombinations extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestInvalidAdvancedQuery.xml");
    }

    public void testInvalidAdvancedQuery() throws Exception
    {
        navigation.issueNavigator().gotoNavigator();
        navigation.issueNavigator().createSearch("project = monkey");

        tester.assertTextPresent("No matching issues found.");
        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "project = blah");
        tester.submit();

        assertions.getIssueNavigatorAssertions().assertJqlErrors("The value 'blah' does not exist for the field 'project'.");

        assertJqlQuery("project = blah");
    }

    public void testUnparsableAdvancedQuery() throws Exception
    {
        navigation.issueNavigator().gotoNavigator();
        navigation.issueNavigator().createSearch("project = monkey");

        tester.assertTextPresent("No matching issues found.");
        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "ljksd;fljsd;lfj;s");
        tester.submit();

        assertions.getIssueNavigatorAssertions().assertJqlErrors("The character ';' is a reserved JQL character. You must enclose it in a string or use the escape '\\u003b' instead. (line 1, character 6)");

        assertJqlQuery("ljksd;fljsd;lfj;s");
    }

    public void testIssueNavigatorValidAdvancedQueryUrlNoActionParams() throws Exception
    {
        String jqlString = "jqlQuery=project+%3D+homosapien";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true");

        assertJqlQuery("project = homosapien");

        assertDisplayingIssuesText(1, 1, 1);
    }

    public void testIssueNavigatorValidAdvancedQueryUrlAndValidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+" + ADMIN_USERNAME;

        String actionParams = "&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true"+actionParams+"&reset=true");

        assertJqlQuery("assignee = " + ADMIN_USERNAME);

        assertDisplayingIssuesText(1, 1, 1);
    }

    public void testIssueNavigatorValidAdvancedQueryUrlAndInvalidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+" + ADMIN_USERNAME;

        String actionParams = "&pid=10234";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true"+actionParams+"&reset=true");

        assertJqlQuery("assignee = " + ADMIN_USERNAME);

        assertDisplayingIssuesText(1, 1, 1);
    }

    public void testIssueNavigatorInvalidAdvancedQueryUrlNoActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true");

        assertJqlQuery("assignee = Blah");

        assertions.getIssueNavigatorAssertions().assertJqlWarnings("The value 'Blah' does not exist for the field 'assignee'.");
    }

    public void testIssueNavigatorInvalidAdvancedQueryUrlAndValidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah";

        String actionParams = "&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+actionParams);

        assertJqlQuery("assignee = Blah");

        assertions.getIssueNavigatorAssertions().assertJqlWarnings("The value 'Blah' does not exist for the field 'assignee'.");
    }

    public void testIssueNavigatorInvalidAdvancedQueryUrlAndInvalidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah";

        String actionParams = "&pid=102344";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+actionParams);

        assertJqlQuery("assignee = Blah");

        assertions.getIssueNavigatorAssertions().assertJqlWarnings("The value 'Blah' does not exist for the field 'assignee'.");
    }

    public void testIssueNavigatorNotParseableAdvancedQueryUrlAndNoActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah+AND";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true");

        assertJqlQuery("assignee = Blah AND");

        assertions.getIssueNavigatorAssertions().assertJqlErrors("Expecting a field name at the end of the query.");
    }

    public void testIssueNavigatorNotParseableAdvancedQueryUrlAndValidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah+AND";

        String actionParams = "&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+actionParams);

        assertJqlQuery("assignee = Blah AND");

        assertions.getIssueNavigatorAssertions().assertJqlErrors("Expecting a field name at the end of the query.");
    }

    public void testIssueNavigatorNotParseableAdvancedQueryUrlAndInvalidActionParams() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blah+AND";

        String actionParams = "&pid=102344";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+actionParams);

        assertJqlQuery("assignee = Blah AND");

        assertions.getIssueNavigatorAssertions().assertJqlErrors("Expecting a field name at the end of the query.");
    }

    public void testIssueNavigatorDontReplaceStatement() throws Exception
    {
        navigation.issueNavigator().gotoNavigator();
        navigation.issueNavigator().createSearch("assignee is NULL");

        tester.assertTextPresent("No matching issues found.");

        assertJqlQuery("assignee is NULL");
    }

    public void testIssueNavigatorReplaceStatements() throws Exception
    {
        String jqlString = "jqlQuery=assignee+is+NULL";

        String actionParams = "&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+actionParams);

        assertJqlQuery("assignee is NULL");

        tester.assertTextPresent("No matching issues found.");
    }

    public void testIssueNavigatorValidAdvancedQueryAndSorting() throws Exception
    {
        String jqlString = "jqlQuery=assignee+is+NULL";

        String sorting ="&sorter/field=assignee&sorter/order=ASC";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+sorting);

        assertJqlQuery("assignee is NULL");
    }

    public void testIssueNavigatorInvalidAdvancedQueryAndSorting() throws Exception
    {
        String jqlString = "jqlQuery=assignee+%3D+Blub";

        String sorting ="&sorter/field=assignee&sorter/order=ASC";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+sorting);

        assertJqlQuery("assignee = Blub");

        assertions.getIssueNavigatorAssertions().assertJqlWarnings("The value 'Blub' does not exist for the field 'assignee'.");
    }

    public void testIssueNavigatorUnparsableAdvancedQueryAndSorting() throws Exception
    {
        String jqlString = "jqlQuery=assignee+bskldfsdkjhf+lskdfj209384";

        String sorting ="&sorter/field=assignee&sorter/order=ASC";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+sorting);

        assertJqlQuery("assignee bskldfsdkjhf lskdfj209384");

        assertions.getIssueNavigatorAssertions().assertJqlErrors("Expecting operator but got 'bskldfsdkjhf'. The valid operators are");
    }

    /**
     * We expect the error message to contain two ORDER BY statements, this behaviour is kind of weird, but
     * a compromise, because we can't seperate the ORDER BY Clause from with in the unparsable Jql query.
     * 
     */
    public void testIssueNavigatorUnparsableAdvancedQueryWithSorting() throws Exception
    {
        String jqlString = "jqlQuery=assignee+bskldfsdkjhf+lskdfj209384+ORDER+BY+assignee+ASC";

        String sorting ="&sorter/field=assignee&sorter/order=ASC&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+sorting);

        assertJqlQuery("assignee bskldfsdkjhf lskdfj209384 ORDER BY assignee ASC");
    }

    public void testIssueNavigatorNoAdvancedQueryWithParamFieldsAndSorting() throws Exception
    {
        String jqlString = "";

        String sorting ="&sorter/field=assignee&sorter/order=ASC&pid=10000";

        tester.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?" + jqlString + "&runQuery=true&reset=true"+sorting);

        assertJqlQuery("project = HSP ORDER BY assignee ASC");
    }

    private void assertJqlQuery(final String expectedJQL)
    {
        XPathLocator locator = new XPathLocator(tester, "//textarea[@id='jqltext']");

        text.assertTextPresent(locator, expectedJQL);
    }

    private void assertDisplayingIssuesText(int pageStart, int pageEnd, int total)
    {
        tester.assertTextPresent(String.format(
                "Displaying issues <span class=\"results-count-start\">%d</span> to %d of <span class=\"results-count-link\"><strong class=\"results-count-total\">%d</strong> matching issues.</span>",
                pageStart,
                pageEnd,
                total
        ));
    }
}
