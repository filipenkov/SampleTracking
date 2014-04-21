package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0.1
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestNavigatorViewsOrdering extends AbstractJqlFuncTest
{

    // JRA-19531 - order by's were dropped
    public void testPrintableViewFilterOrderByRespected() throws Exception
    {
        administration.restoreData("TestNavigatorViewsSorting.xml");

        // load filter 'order asc'
        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.clickLinkWithText("Printable");
        // Verify the ordering is correct
        List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-3", "HSP-2", "HSP-1"));
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);

        // load filter 'order desc'
        navigation.issueNavigator().loadFilter(10001, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.clickLinkWithText("Printable");
        // Verify the ordering is correct
        conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), "HSP-1", "HSP-2", "HSP-3"));
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
    }
}
