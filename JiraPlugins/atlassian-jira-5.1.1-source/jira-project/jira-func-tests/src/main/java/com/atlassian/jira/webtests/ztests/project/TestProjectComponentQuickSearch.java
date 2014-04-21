package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.assertions.LinkAssertions;
import com.atlassian.jira.functest.framework.assertions.LinkAssertionsImpl;
import com.atlassian.jira.functest.framework.parser.SystemInfoParser;
import com.atlassian.jira.functest.framework.parser.SystemInfoParserImpl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.AndCell;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.atlassian.jira.webtests.table.TextCell;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.PROJECTS })
public class TestProjectComponentQuickSearch extends JIRAWebTest
{

    String appServer;
    private LinkAssertions linkAssertions;

    public TestProjectComponentQuickSearch(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestProjectComponentQuickSearch.xml");

        // what app server are we running on
        SystemInfoParser systemInfoParser = new SystemInfoParserImpl(getTester(), navigation);
        appServer = systemInfoParser.getSystemInfo().getAppServer();
        linkAssertions = new LinkAssertionsImpl(tester, getEnvironmentData());
    }

    public void tearDown()
    {
        super.tearDown();
    }

    public void testProjectComponentQuickSearchMultipleProjects() throws Exception
    {
        //see all issues
        WebTable issueTable = assertComponentQuickSearch("", 5);
        assertLinkPresentWithoutSubString("permlink", "pid");
        assertLinkPresentWithoutSubString("permlink", "component");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);
        assertIssueTableHasMKY_35(issueTable);
        assertIssueTableHasMKY_45(issueTable);
        assertIssueTableHasHSP_45(issueTable);

        //project component quicksearch across multiple projects
        issueTable = assertComponentQuickSearchOnAdvanced("c:one", 1);
        // Make sure we are on the advanced view since this does not fit the navigator
        assertLinkWithTextExists("Syntax Help");
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "component = \"one two homo three four five\"");
        assertIssueTableHasHSP_12345(issueTable);

        issueTable = assertComponentQuickSearchOnAdvanced(" c:two ", 2);
        // Make sure we are on the advanced view since this does not fit the navigator
        assertLinkWithTextExists("Syntax Help");
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "component in (\"one two homo three four five\", \"two three homo four five\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);

        issueTable = assertComponentQuickSearchOnAdvanced("three c:three", 3);
        // Make sure we are on the advanced view since this does not fit the navigator
        assertLinkWithTextExists("Syntax Help");
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "(summary ~ three OR description ~ three OR comment ~ three) AND component in (\"one two homo three four five\", \"two three homo four five\", \"three monk five\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);
        assertIssueTableHasMKY_35(issueTable);

        issueTable = assertComponentQuickSearchOnAdvanced("c:four four", 4);
        // Make sure we are on the advanced view since this does not fit the navigator
        assertLinkWithTextExists("Syntax Help");
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "(summary ~ four OR description ~ four OR comment ~ four) AND component in (\"homo four five\", \"one two homo three four five\", \"two three homo four five\", \"monk five four\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);
        assertIssueTableHasMKY_45(issueTable);
        assertIssueTableHasHSP_45(issueTable);

        // Wont pass on Websphere currently
        if (appServer.indexOf("WebSphere") == -1)
        {
            issueTable = assertComponentQuickSearchOnAdvanced("five c:five five", 5);
            // Make sure we are on the advanced view since this does not fit the navigator
            assertLinkWithTextExists("Syntax Help");
            linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "(summary ~ \"five  five\" OR description ~ \"five  five\" OR comment ~ \"five  five\") AND component in (\"homo four five\", \"one two homo three four five\", \"two three homo four five\", \"monk five four\", \"three monk five\")");
            assertIssueTableHasHSP_12345(issueTable);
            assertIssueTableHasHSP_2345(issueTable);
            assertIssueTableHasMKY_35(issueTable);
            assertIssueTableHasMKY_45(issueTable);
            assertIssueTableHasHSP_45(issueTable);
        }
    }

    public void testProjectComponentQuickSearchHomosapienProject
            () throws SAXException
    {
        //project component quick search for HSP
        WebTable issueTable = assertComponentQuickSearch("hsp c:one", 1);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = HSP AND component = \"one two homo three four five\"");
        assertIssueTableHasHSP_12345(issueTable);

        issueTable = assertComponentQuickSearch(" c:two homosapien ", 2);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = HSP AND component in (\"one two homo three four five\", \"two three homo four five\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);

        issueTable = assertComponentQuickSearch("three HSP c:three", 2);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = HSP AND (summary ~ three OR description ~ three OR comment ~ three) AND component in (\"one two homo three four five\", \"two three homo four five\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);

        issueTable = assertComponentQuickSearch("c:four four HOMOSAPIEN", 3);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = HSP AND (summary ~ four OR description ~ four OR comment ~ four) AND component in (\"homo four five\", \"one two homo three four five\", \"two three homo four five\")");
        assertIssueTableHasHSP_12345(issueTable);
        assertIssueTableHasHSP_2345(issueTable);
        assertIssueTableHasHSP_45(issueTable);

        // Wont pass on Websphere currently
        if (appServer.indexOf("WebSphere") == -1)
        {
            issueTable = assertComponentQuickSearch("hsp five c:five five", 3);
            linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = HSP AND (summary ~ \"five  five\" OR description ~ \"five  five\" OR comment ~ \"five  five\") AND component in (\"homo four five\", \"one two homo three four five\", \"two three homo four five\")");
            assertIssueTableHasHSP_12345(issueTable);
            assertIssueTableHasHSP_2345(issueTable);
            assertIssueTableHasHSP_45(issueTable);
        }
    }

    public void testProjectComponentQuickSearchMonkey
            () throws SAXException
    {
        //project component quick search for MKY
        WebTable issueTable = assertComponentQuickSearch("c:one MONKEY", 2);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = MKY");
        assertIssueTableHasMKY_35(issueTable);
        assertIssueTableHasMKY_45(issueTable);

        issueTable = assertComponentQuickSearch(" MKY c:two ", 2);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = MKY");
        assertIssueTableHasMKY_35(issueTable);
        assertIssueTableHasMKY_45(issueTable);

        issueTable = assertComponentQuickSearch("monkey three c:three", 1);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = MKY AND (summary ~ three OR description ~ three OR comment ~ three) AND component = \"three monk five\"");
        assertIssueTableHasMKY_35(issueTable);

        issueTable = assertComponentQuickSearch("c:four mky four", 1);
        linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = MKY AND (summary ~ four OR description ~ four OR comment ~ four) AND component = \"monk five four\"");
        assertIssueTableHasMKY_45(issueTable);

        // Wont pass on Websphere currently
        if (appServer.indexOf("WebSphere") == -1)
        {
            issueTable = assertComponentQuickSearch("five c:five five monkey", 2);
            linkAssertions.assertLinkIdQueryStringContainsJqlQuery("permlink", "project = MKY AND (summary ~ \"five  five\" OR description ~ \"five  five\" OR comment ~ \"five  five\") AND component in (\"monk five four\", \"three monk five\")");
            assertIssueTableHasMKY_35(issueTable);
            assertIssueTableHasMKY_45(issueTable);
        }
    }

    private WebTable assertComponentQuickSearch(String searchInput, int numOfResults) throws SAXException
    {
        runQuickSearch(searchInput);
        assertIssueNavigatorDisplaying("1", String.valueOf(numOfResults), String.valueOf(numOfResults));
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertEquals(numOfResults + 1, issueTable.getRowCount());
        assertTableHasMatchingRowFromTo(issueTable, 0, 1, new Object[] { "T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due" });
        return issueTable;
    }

    private WebTable assertComponentQuickSearchOnAdvanced(String searchInput, int numOfResults) throws SAXException
    {
        runQuickSearch(searchInput);
        assertAdvacnedIssueNavigatorDisplaying("1", String.valueOf(numOfResults), String.valueOf(numOfResults));
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertEquals(numOfResults + 1, issueTable.getRowCount());
        assertTableHasMatchingRowFromTo(issueTable, 0, 1, new Object[] { "T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due" });
        return issueTable;
    }

    private void assertAdvacnedIssueNavigatorDisplaying(String from, String to, String of)
    {
        assertTextPresent("Displaying issues <span class=\"results-count-start\">" + from + "</span> to " + to + " of <span class=\"results-count-link\"><strong class=\"results-count-total\">" + of + "</strong> matching issues");
    }

    private void assertIssueTableHasHSP_45(WebTable issueTable)
    {
        assertTableHasMatchingRow(issueTable, new Object[] { new AndCell(new LinkCell("/browse/HSP-1", ""), new ImageCell(ISSUE_IMAGE_BUG)), new LinkCell("/browse/HSP-1", "HSP-1"), "homo four five", ADMIN_FULLNAME, ADMIN_FULLNAME, new ImageCell(PRIORITY_IMAGE_MAJOR), new AndCell(new ImageCell(STATUS_IMAGE_OPEN), new TextCell(STATUS_OPEN)), "Unresolved", "23/Nov/07", "23/Nov/07", "" });
    }

    private void assertIssueTableHasHSP_12345(WebTable issueTable)
    {
        assertTableHasMatchingRow(issueTable, new Object[] { new AndCell(new LinkCell("/browse/HSP-2", ""), new ImageCell(ISSUE_IMAGE_TASK)), new LinkCell("/browse/HSP-2", "HSP-2"), "one two homo three four five", ADMIN_FULLNAME, ADMIN_FULLNAME, new ImageCell(PRIORITY_IMAGE_MAJOR), new AndCell(new ImageCell(STATUS_IMAGE_OPEN), new TextCell(STATUS_OPEN)), "Unresolved", "23/Nov/07", "23/Nov/07", "" });
    }

    private void assertIssueTableHasHSP_2345(WebTable issueTable)
    {
        assertTableHasMatchingRow(issueTable, new Object[] { new AndCell(new LinkCell("/browse/HSP-3", ""), new ImageCell(ISSUE_IMAGE_IMPROVEMENT)), new LinkCell("/browse/HSP-3", "HSP-3"), "two three homo four five", ADMIN_FULLNAME, ADMIN_FULLNAME, new ImageCell(PRIORITY_IMAGE_MAJOR), new AndCell(new ImageCell(STATUS_IMAGE_OPEN), new TextCell(STATUS_OPEN)), "Unresolved", "23/Nov/07", "23/Nov/07", "" });
    }

    private void assertIssueTableHasMKY_35(WebTable issueTable)
    {
        assertTableHasMatchingRow(issueTable, new Object[] { new AndCell(new LinkCell("/browse/MKY-2", ""), new ImageCell(ISSUE_IMAGE_NEWFEATURE)), new LinkCell("/browse/MKY-2", "MKY-2"), "three monk five", ADMIN_FULLNAME, ADMIN_FULLNAME, new ImageCell(PRIORITY_IMAGE_MAJOR), new AndCell(new ImageCell(STATUS_IMAGE_OPEN), new TextCell(STATUS_OPEN)), "Unresolved", "23/Nov/07", "23/Nov/07", "" });
    }

    private void assertIssueTableHasMKY_45(WebTable issueTable)
    {
        assertTableHasMatchingRow(issueTable, new Object[] { new AndCell(new LinkCell("/browse/MKY-1", ""), new ImageCell(ISSUE_IMAGE_BUG)), new LinkCell("/browse/MKY-1", "MKY-1"), "monk five four", ADMIN_FULLNAME, ADMIN_FULLNAME, new ImageCell(PRIORITY_IMAGE_MAJOR), new AndCell(new ImageCell(STATUS_IMAGE_OPEN), new TextCell(STATUS_OPEN)), "Unresolved", "23/Nov/07", "23/Nov/07", "" });
    }
}
