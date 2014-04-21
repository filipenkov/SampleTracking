package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.w3c.dom.Node;

import java.util.Collections;

/**
 * Test related to the Jql query history in the Issue Navigator advanced screen.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlQueryTextBox extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlQueryTextBox.xml");
    }

    public void testQueryHistoryTrimQuery() throws Exception
    {
        String query = "project = homosapien";

        assertSearchWithResults(query,"HSP-3","HSP-2","HSP-1");

        assertQueryHistory(query);

        navigation.issueNavigator().createSearch("   "+query + ORDER_BY_CLAUSE+"      ");
        assertIssues("HSP-3","HSP-2","HSP-1");
        assertQueryHistory(query);
    }

    public void testQueryHistoryKeepWhitespaceInsideQuery() throws Exception
    {
        String query1 = "project =  homosapien";
        String query2 = "project    = homosapien";

        assertSearchWithResults(query1,"HSP-3","HSP-2","HSP-1");
        assertSearchWithResults(query2,"HSP-3","HSP-2","HSP-1");

        assertQueryHistory(query2, query1);
    }

    public void testQueryHistoryBoxNoDuplicateQueries() throws Exception
    {
        String queryHSP1 = "Issue = \"HSP-1\"";
        String queryHSP2 = "Issue = \"HSP-2\"";
        String longQueryHSP1 = "Issue = \"HSP-1\" AND affectedVersion is empty AND summary ~ sample AND assignee = \"fred\" AND reporter = \"" + ADMIN_USERNAME + "\" AND priority = \"Major\"";
        String longQueryHSP2 = "Issue = \"HSP-2\" AND affectedVersion is empty AND summary ~ sample AND assignee = \"fred\" AND reporter = \"" + ADMIN_USERNAME + "\"";

        assertSearchWithResults(queryHSP1, "HSP-1");
        assertQueryHistory(queryHSP1);

        assertSearchWithResults(queryHSP2, "HSP-2");
        assertQueryHistory(queryHSP2, queryHSP1);

        assertSearchWithResults(queryHSP1, "HSP-1");
        assertQueryHistory(queryHSP1, queryHSP2);

        assertSearchWithResults(longQueryHSP1,"HSP-1");
        assertQueryHistory(longQueryHSP1, queryHSP1, queryHSP2);

        assertSearchWithResults(longQueryHSP2);
        assertQueryHistory(longQueryHSP2, longQueryHSP1, queryHSP1, queryHSP2);
    }

    public void testClickingQueryHistoryClearsCurrentFilter() throws Exception
    {
        String queryHSP1 = "Issue = \"HSP-1\"";
        String queryHSP2 = "Issue = \"HSP-2\"";

        assertSearchWithResults(queryHSP1, "HSP-1");
        assertQueryHistory(queryHSP1);

        assertSearchWithResults(queryHSP2, "HSP-2");
        assertQueryHistory(queryHSP2, queryHSP1);

        final long filterId = navigation.issueNavigator().saveCurrentAsNewFilter("My New Filter", "", true, Collections.<TestSharingPermission>emptySet());
        navigation.issueNavigator().loadFilter(filterId, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        // click a history item
        tester.clickLink("historyItem1");

        // assert that filter is gone
        Locator locator = new IdLocator(tester, "filter-description");
        assertions.getTextAssertions().assertTextNotPresent(locator, "Filter modified since loading");

        assertQueryHistory(queryHSP1, queryHSP2);
    }

    private void assertQueryHistory(String... items)
    {
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        XPathLocator locator = new XPathLocator(tester, "//a[@class='jqlQuickLink']");
        final Node[] nodes = locator.getNodes();
        assertEquals("Size of query history items doesn't match expected ", items.length, nodes.length);

        for (int i = 0; i < items.length; i++)
        {
            assertEquals(items[i] + ORDER_BY_CLAUSE, locator.getText(nodes[i]));
        }
    }

}
