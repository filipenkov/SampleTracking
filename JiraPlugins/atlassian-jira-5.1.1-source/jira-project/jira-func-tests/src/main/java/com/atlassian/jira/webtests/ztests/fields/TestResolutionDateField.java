package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestResolutionDateField extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestResolutionDateField.xml");
    }

    public void testViewIssuePage()
    {
        //check HSP-1 doesn't show the resolution date
        navigation.issue().viewIssue("HSP-1");
        Locator locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        //check HSP-2 shows the resolution date
        navigation.issue().viewIssue("HSP-2");
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "Second test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Resolved");
    }

    public void testResolveIssueUpdatesDate()
    {
        //check HSP-1 doesn't show the resolution date
        navigation.issue().viewIssue("HSP-1");
        Locator locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        // resolve the issue
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //now check the issue shows the resolved date.
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Resolved");

        //also check the navigator columns:
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        //can't really assert today's date exactly here because of timezone issues with the different builds etc...
        final String resolvedCell = tester.getDialog().getWebTableBySummaryOrId("issuetable").getCellAsText(2, 11).trim();
        assertTrue(resolvedCell.length() > 0);

        //now re-open the issue to ensure that clears the resolution date
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("action_id_3");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //check resolved is no longer shown
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        //also check the issue navigator no longer shows it
        //also check the navigator columns:
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        assertTableCellEmpty("issuetable", 2, 11);
    }

    public void testIssueNavigatorColumns()
    {
        //show all issues
        navigation.issueNavigator().displayAllIssues();

        //check that HSP-2 displays a Resolved date, and HSP-1 doesn't.
        tester.assertTextPresent("Issue Navigator");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Resolved");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 1), "HSP-2");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 11), "14/Oct/08");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        assertTableCellEmpty("issuetable", 2, 11);
    }

    /**
     * tests here!
     */
    public void testSearcher()
    {
        navigation.issueNavigator().gotoNavigator();

        tester.setFormElement("resolutiondate:after", "13/Oct/08");
        tester.setFormElement("resolutiondate:before", "16/Oct/08");
        tester.setFormElement("resolutiondate:next", "");
        tester.submit("show");

        //should have gotten HSP-2
        tester.assertTextPresent("Issue Navigator");
        assertEquals(2, tester.getDialog().getWebTableBySummaryOrId("issuetable").getRowCount());
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Resolved");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 1), "HSP-2");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 11), "14/Oct/08");

        //Resolved date of HSP-2 is not in this range, so we shouldn't get anything.
        tester.setFormElement("resolutiondate:after", "15/Oct/08");
        tester.setFormElement("resolutiondate:before", "16/Oct/08");
        tester.setFormElement("resolutiondate:next", "");
        tester.submit("show");

        tester.assertTextPresent("No matching issues found.");
        assertNull(tester.getDialog().getWebTableBySummaryOrId("issuetable"));
    }

    public void testIssueViews()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Printable");
        tester.assertTextNotPresent("Resolved:");

        navigation.issue().viewIssue("HSP-2");
        tester.clickLinkWithText("Printable");
        tester.assertTextPresent("Resolved:");

        //word view uses the same template as the printable view so no need to test it.

        //Xml view is tested in TestXmlIssueView
    }

    private void assertTableCellEmpty(String tableId, int row, int col)
    {
        final String resolvedCell = tester.getDialog().getWebTableBySummaryOrId(tableId).getCellAsText(row, col).trim();
        assertTrue(resolvedCell.length() == 0);
    }
}
