package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT, Category.COMPONENTS_AND_VERSIONS })
public class TestBrowseVersionsAndComponents extends JIRAWebTest
{
    private static final String VIEW_RESOLVED_ISSUES_LINK = "Resolved";
    private static final String VIEW_UNRESOLVED_ISSUES_LINK = "Unresolved";
    private static final String POPULAR_ISSUES_LINK = "Popular Issues";

    public TestBrowseVersionsAndComponents(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestProjectVersionsAndComponentsTabPanel.xml");
    }

    public void tearDown()
    {
        super.tearDown();
    }

    public void testBrowseComponentsPopularIssuesPanel()
    {
        //check popular issues panel with NO issues
        gotoComponentBrowse(PROJECT_NEO_KEY, COMPONENT_NAME_ONE);
        clickLinkWithText(POPULAR_ISSUES_LINK);
        clickToScope(VIEW_UNRESOLVED_ISSUES_LINK);
        assertNoPopularIssues();
        clickToScope(VIEW_RESOLVED_ISSUES_LINK);
        assertNoPopularIssues();

        //check popular issues panel with issues
        gotoComponentBrowse(PROJECT_HOMOSAP_KEY, "full component");
        clickLinkWithText(POPULAR_ISSUES_LINK);
        clickToScope(VIEW_UNRESOLVED_ISSUES_LINK);
        assertExpectedPopularIssues(false);
        clickToScope(VIEW_RESOLVED_ISSUES_LINK);
        assertExpectedPopularIssuesResolved(false);
    }

    public void testBrowseVersionPopularIssuesPanel()
    {
        //check popular issues panel with NO issues
        gotoVersionBrowse(PROJECT_NEO_KEY, VERSION_NAME_ONE);
        clickLinkWithText(POPULAR_ISSUES_LINK);
        clickToScope(VIEW_UNRESOLVED_ISSUES_LINK);
        assertNoPopularIssues();
        clickToScope(VIEW_RESOLVED_ISSUES_LINK);
        assertNoPopularIssues();

        //check popular issues panel with issues
        gotoVersionBrowse(PROJECT_HOMOSAP_KEY, "full version");
        clickLinkWithText(POPULAR_ISSUES_LINK);
        clickToScope(VIEW_UNRESOLVED_ISSUES_LINK);
        assertExpectedPopularIssues(true);
        clickToScope(VIEW_RESOLVED_ISSUES_LINK);
        assertExpectedPopularIssuesResolved(true);
    }

    private void assertNoPopularIssues()
    {
        try
        {
            WebTable componentsTable = getDialog().getResponse().getTableWithID("popular_issues_table");
            assertEquals(1, componentsTable.getRowCount());
            assertTableHasMatchingRow(componentsTable, new Object[] { "No issues" });
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertExpectedPopularIssues(boolean isVersion)
    {
        try
        {
            WebTable componentsTable = getDialog().getResponse().getTableWithID("popular_issues_table");
            assertEquals(3, componentsTable.getRowCount());
            if (isVersion)
            {
                assertTableHasMatchingRow(componentsTable, new Object[] {"3", new ImageCell(ISSUE_IMAGE_BUG), "HSP-5", new LinkCell("/browse/HSP-5", "hsp bug 1"), "full version", new ImageCell(PRIORITY_IMAGE_BLOCKER), new ImageCell(STATUS_IMAGE_OPEN) });
            }
            else
            {
                assertTableHasMatchingRow(componentsTable, new Object[] { "3", new ImageCell(ISSUE_IMAGE_BUG), "HSP-5", new LinkCell("/browse/HSP-5", "hsp bug 1"), new LinkCell("/browse/HSP/fixforversion/10000", "full version"), new ImageCell(PRIORITY_IMAGE_BLOCKER), new ImageCell(STATUS_IMAGE_OPEN) });
            }
            assertTableHasMatchingRow(componentsTable, new Object[] { "2", new ImageCell(ISSUE_IMAGE_IMPROVEMENT), "HSP-4", new LinkCell("/browse/HSP-4", "hsp improvement 2"), new LinkCell("/browse/HSP/fixforversion/10001", "desc version"), new ImageCell(PRIORITY_IMAGE_CRITICAL), new ImageCell(STATUS_IMAGE_IN_PROGRESS) });
            assertTableHasMatchingRow(componentsTable, new Object[] {"1", new ImageCell(ISSUE_IMAGE_TASK), "HSP-3", new LinkCell("/browse/HSP-3", "hsp task 3"), new LinkCell("/browse/HSP/fixforversion/10002", "date version"), new ImageCell(PRIORITY_IMAGE_MAJOR), new ImageCell(STATUS_IMAGE_OPEN) });
            assertTableHasNoMatchingRow(componentsTable, new Object[] { null, new ImageCell(ISSUE_IMAGE_NEWFEATURE), "HSP-2", new LinkCell("/browse/HSP-2", "hsp feature 4"), null, new ImageCell(PRIORITY_IMAGE_MINOR), new ImageCell(STATUS_IMAGE_IN_PROGRESS) });
            assertTableHasNoMatchingRow(componentsTable, new Object[] { null, new ImageCell(ISSUE_IMAGE_BUG), "HSP-1", new LinkCell("/browse/HSP-1", "hsp bug 5"), null, new ImageCell(PRIORITY_IMAGE_TRIVIAL), new ImageCell(STATUS_IMAGE_OPEN) });
            assertTableHasNoMatchingRow(componentsTable, new Object[] { null, new ImageCell(ISSUE_IMAGE_TASK), "HSP-6", new LinkCell("/browse/HSP-6", "hsp resolved bug 1"), null, new ImageCell(PRIORITY_IMAGE_MAJOR), new ImageCell(STATUS_IMAGE_RESOLVED) });
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertExpectedPopularIssuesResolved(boolean isVersion)
    {
        try
        {
            WebTable componentsTable = getDialog().getResponse().getTableWithID("popular_issues_table");
            assertEquals(1, componentsTable.getRowCount());
            if (isVersion)
            {
                assertTableHasMatchingRow(componentsTable, new Object[] {"1", new ImageCell(ISSUE_IMAGE_TASK), "HSP-6", new LinkCell("/browse/HSP-6", "hsp resolved bug 1"), "full version", new ImageCell(PRIORITY_IMAGE_MAJOR), new ImageCell(STATUS_IMAGE_RESOLVED) });
            }
            else
            {
                assertTableHasMatchingRow(componentsTable, new Object[] { "1", new ImageCell(ISSUE_IMAGE_TASK), "HSP-6", new LinkCell("/browse/HSP-6", "hsp resolved bug 1"), new LinkCell("/browse/HSP/fixforversion/10000", "full version"), new ImageCell(PRIORITY_IMAGE_MAJOR), new ImageCell(STATUS_IMAGE_RESOLVED) });
            }
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void clickToScope(String scope)
    {
        try
        {
            assertLinkPresentWithText(scope);
            clickLinkWithText(scope);
        }
        catch (Throwable t)
        {
            //ignore since we must already be on the right scope.
        }
    }
}
