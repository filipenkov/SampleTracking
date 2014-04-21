package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test for saved filters that reference other saved filters.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestNestedSavedFilters extends AbstractJqlFuncTest
{
    private static final int FILTER_HOMOSAPIENS = 10000;
    private static final int FILTER_1 = 10002;
    private static final int FILTER_HOMOSAPIENS_AND_MONKEYS = 10004;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestNestedSavedFilters.xml");
    }
    
    public void testNestedSavedFilters() throws Exception
    {
        //Try to introduce a cyclical reference
        //ID 10002: Filter 1: status = "open"
        //ID 10003: Filter 2: filter = "project = homosapien AND filter = "Filter 1""

        navigation.issueNavigator().loadFilter(FILTER_1, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        // modify query with new JQL
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setFormElement("jqlQuery", "status = \"open\" and filter = \"Filter 2\"");
        tester.submit();

        assertions.getJiraFormAssertions().assertFormErrMsg("Field 'filter' with value 'Filter 2' matches filter "
                + "'Filter 2' and causes a cyclical reference, this query can not be executed and should be edited.");
    }

    public void testReferencedFilterInvalid() throws Exception
    {
        _loadFilter(FILTER_HOMOSAPIENS_AND_MONKEYS);
        text.assertTextSequence(new IdLocator(tester, "issue-filter"), "Edit", "the current filter to correct errors");
    }

    public void testReferencedFilterBecomesInvalid() throws Exception
    {
        _loadFilter(FILTER_HOMOSAPIENS);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        administration.permissionSchemes().scheme("Default Permission Scheme").removePermission(10, "jira-administrators");

        _loadFilter(FILTER_HOMOSAPIENS);

        text.assertTextSequence(new IdLocator(tester, "issue-filter"), "Edit", "the current filter to correct errors");
    }

    private void _loadFilter(final int filterId)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
    }

}
