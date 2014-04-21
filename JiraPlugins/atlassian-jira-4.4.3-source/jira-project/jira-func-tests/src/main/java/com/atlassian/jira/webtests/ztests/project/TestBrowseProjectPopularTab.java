package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectPopularTab extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestBrowseProjectPopularTab.xml");
    }


    public void testVotingTurnedOff()
    {
        navigation.browseProject("HSP");
        assertions.assertNodeExists("//a[@id='popularissues-panel-panel']");

        administration.generalConfiguration().disableVoting();
        navigation.browseProject("HSP");
        assertions.assertNodeDoesNotExist("//a[@id='popularissues-panel-panel']");

        navigation.browseVersionTabPanel("HSP", "New Version 4");
        assertions.assertNodeDoesNotExist("//a[@id='version-popularissues-panel-panel']");

        navigation.browseComponentTabPanel("HSP", "New Component 2");
        assertions.assertNodeDoesNotExist("//a[@id='component-popularissues-panel-panel']");

        administration.generalConfiguration().enableVoting();
        navigation.browseProject("HSP");
        assertions.assertNodeExists("//a[@id='popularissues-panel-panel']");
    }

    public void testNoPopularIssues()
    {
        navigation.browseProjectTabPanel("EMPTY", "popularissues");

        TableLocator tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        navigation.browseVersionTabPanel("EMPTY", "Empty Version", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");

        navigation.browseComponentTabPanel("EMPTY", "Empty Component", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");



        navigation.browseProjectTabPanel("UNRESOLVED", "popularissues");

        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(3, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");
        text.assertTextPresent(tableLocator, "UNRESOLVED-1");
        text.assertTextPresent(tableLocator, "UNRESOLVED-2");
        text.assertTextPresent(tableLocator, "UNRESOLVED-3");

        navigation.browseVersionTabPanel("UNRESOLVED", "Unresolved Version", "popularissues");
        tester.clickLinkWithText("unresolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(3, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");
        text.assertTextPresent(tableLocator, "UNRESOLVED-1");
        text.assertTextPresent(tableLocator, "UNRESOLVED-2");
        text.assertTextPresent(tableLocator, "UNRESOLVED-3");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");

        navigation.browseComponentTabPanel("UNRESOLVED", "Unresolved Component", "popularissues");
        tester.clickLinkWithText("unresolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(3, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");
        text.assertTextPresent(tableLocator, "UNRESOLVED-1");
        text.assertTextPresent(tableLocator, "UNRESOLVED-2");
        text.assertTextPresent(tableLocator, "UNRESOLVED-3");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");


        navigation.browseProjectTabPanel("RESOLVED", "popularissues");

        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        navigation.browseVersionTabPanel("RESOLVED", "Resolved Version", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(3, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");
        text.assertTextPresent(tableLocator, "RESOLVED-1");
        text.assertTextPresent(tableLocator, "RESOLVED-2");
        text.assertTextPresent(tableLocator, "RESOLVED-3");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");

        tester.clickLinkWithText("unresolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

        navigation.browseComponentTabPanel("RESOLVED", "Resolved Component", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(3, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");
        text.assertTextPresent(tableLocator, "RESOLVED-1");
        text.assertTextPresent(tableLocator, "RESOLVED-2");
        text.assertTextPresent(tableLocator, "RESOLVED-3");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]/a", "unresolved issues");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]", "resolved issues");

        tester.clickLinkWithText("unresolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
        assertions.assertNodeDoesNotExist("//ul[@id='resolution-picker']/li[1]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[1]", "unresolved issues");
        assertions.assertNodeExists("//ul[@id='resolution-picker']/li[2]/a");
        assertions.assertNodeHasText("//ul[@id='resolution-picker']/li[2]/a", "resolved issues");

    }

    public void testNoVotes()
    {
        navigation.browseProjectTabPanel("NOVOTE", "popularissues");

        TableLocator tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        navigation.browseVersionTabPanel("NOVOTE", "No Vote Version", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");
        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");
        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        navigation.browseComponentTabPanel("NOVOTE", "No Vote Component", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");
        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

        tester.clickLinkWithText("resolved issues");
        tableLocator = new TableLocator(tester, "popular_issues_table");
        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");

    }

    public void testOrdering()
    {
        navigation.browseProjectTabPanel("HSP", "popularissues");

        TableLocator tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(14, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        int i = 1;
        assertRow(i++, 2, "HSP-9");
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 2, "HSP-14");
        assertRow(i++, 2, "HSP-11");
        assertRow(i++, 1, "HSP-2");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-6");
        assertRow(i++, 1, "HSP-8");
        assertRow(i++, 1, "HSP-1");
        assertRow(i++, 1, "HSP-3");
        assertRow(i++, 1, "HSP-7");
        assertRow(i++, 1, "HSP-12");
        assertRow(i++, 1, "HSP-4");
        assertRow(i++, 1, "HSP-5");


        navigation.browseVersionTabPanel("HSP", "New Version 1", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(6, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        i = 1;
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 2, "HSP-14");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-1");
        assertRow(i++, 1, "HSP-3");
        assertRow(i++, 1, "HSP-12");

        navigation.browseVersionTabPanel("HSP", "New Version 4", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(6, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        i = 1;
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-8");
        assertRow(i++, 1, "HSP-1");
        assertRow(i++, 1, "HSP-7");
        assertRow(i++, 1, "HSP-4");


        navigation.browseVersionTabPanel("HSP", "New Version 5", "popularissues");
        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(5, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        i = 1;
        assertRow(i++, 2, "HSP-9");
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 2, "HSP-11");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-3");
        
        navigation.browseComponentTabPanel("HSP", "New Component 1", "popularissues");

        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(5, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        i = 1;
        assertRow(i++, 2, "HSP-9");
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 2, "HSP-11");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-1");
        
        navigation.browseComponentTabPanel("HSP", "New Component 2", "popularissues");

        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(8, tableLocator.getTable().getRowCount());
        text.assertTextNotPresent(tableLocator, "No issues.");

        i = 1;
        assertRow(i++, 2, "HSP-10");
        assertRow(i++, 2, "HSP-14");
        assertRow(i++, 1, "HSP-13");
        assertRow(i++, 1, "HSP-8");
        assertRow(i++, 1, "HSP-3");
        assertRow(i++, 1, "HSP-7");
        assertRow(i++, 1, "HSP-4");
        assertRow(i++, 1, "HSP-5");

        
        navigation.browseComponentTabPanel("HSP", "New Component 3", "popularissues");

        tableLocator = new TableLocator(tester, "popular_issues_table");

        assertEquals(1, tableLocator.getTable().getRowCount());
        text.assertTextPresent(tableLocator, "No issues.");
    }

    private void assertRow(int row, int votes, String key)
    {
        assertCell(row, 1, "" + votes);
        assertCell(row, 3, key);
    }

    private void assertCell(int row, int col, String val)
    {
        assertions.assertNodeHasText("//table[@id='popular_issues_table']/tbody/tr[" + row + "]/td[" + col + "]", val);
    }


}