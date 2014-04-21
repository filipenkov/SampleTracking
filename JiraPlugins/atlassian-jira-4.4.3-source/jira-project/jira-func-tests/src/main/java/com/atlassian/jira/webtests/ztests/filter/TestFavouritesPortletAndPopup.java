package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS, Category.PORTLETS })
public class TestFavouritesPortletAndPopup extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestUpgradeFavourites.xml");
    }

    public void testFilterPopUp()
    {
        navigation.login(ADMIN_USERNAME);
        gotoFilterPopUp();

        TableLocator tableLocator = new TableLocator(tester, "filter_list");
        assertEquals(4, tableLocator.getTable().getRowCount());
        TableCellLocator tableCellLocator = new TableCellLocator(tester, "filter_list", 1, 0);
        text.assertTextPresent(tableCellLocator, "All");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10000");
        tableCellLocator = new TableCellLocator(tester, "filter_list", 2, 0);
        text.assertTextPresent(tableCellLocator, "New Features");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10020");
        tableCellLocator = new TableCellLocator(tester, "filter_list", 3, 0);
        text.assertTextPresent(tableCellLocator, "Nick");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10010");

        navigation.manageFilters().removeFavourite(10000);

        gotoFilterPopUp();
        tableLocator = new TableLocator(tester, "filter_list");
        assertEquals(3, tableLocator.getTable().getRowCount());
        tableCellLocator = new TableCellLocator(tester, "filter_list", 1, 0);
        text.assertTextPresent(tableCellLocator, "New Features");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10020");
        tableCellLocator = new TableCellLocator(tester, "filter_list", 2, 0);
        text.assertTextPresent(tableCellLocator, "Nick");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10010");

        navigation.manageFilters().removeFavourite(10020);

        gotoFilterPopUp();
        tableLocator = new TableLocator(tester, "filter_list");
        assertEquals(2, tableLocator.getTable().getRowCount());
        tableCellLocator = new TableCellLocator(tester, "filter_list", 1, 0);
        text.assertTextPresent(tableCellLocator, "Nick");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10010");

        navigation.manageFilters().removeFavourite(10010);

        gotoFilterPopUp();
        tableLocator = new TableLocator(tester, "filter_list");
        assertNull(tableLocator.getTable());
        tester.assertTextPresent("You have no favourite filters at the moment.");
        tester.assertTextPresent("Manage Filters");

        navigation.manageFilters().addFavourite(10001);
        gotoFilterPopUp();
        tableLocator = new TableLocator(tester, "filter_list");
        assertEquals(2, tableLocator.getTable().getRowCount());
        tableCellLocator = new TableCellLocator(tester, "filter_list", 1, 0);
        text.assertTextPresent(tableCellLocator, "All My");
        text.assertTextPresent(tableCellLocator.getHTML(), "requestId=10001");
    }

    public void gotoFilterPopUp()
    {
        tester.gotoPage("secure/FavouriteFilters.jspa");
    }
}
