package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.List;

/**
 * Test the deleting of filters, and whether they disappear off the various lists
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterDelete extends FuncTestCase
{
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("sharedfilters/SharedFiltersBase.xml");
    }

    /**
     * Deletes a filter
     */
    public void testBasicNavigationPage()
    {
        final FilterNavigation filterNavigation = navigation.manageFilters();

        long filterId = filterNavigation.createFilter("Delete Me", "This will be deleted");

        filterNavigation.myFilters();
        assertFilterIsInList(parse.filter().parseFilterList("mf_owned"), "Delete Me");

        filterNavigation.goToDefault();
        assertFilterIsInList(parse.filter().parseFilterList("mf_favourites"), "Delete Me");

        // now delete it
        tester.gotoPage("secure/DeleteFilter!default.jspa?filterId=" + filterId + "&returnUrl=ManageFilters.jspa");
        tester.submit("Delete");
        assertOnManageFiltersPage();
        
        filterNavigation.myFilters();
        assertFilterIsNotInList(parse.filter().parseFilterList("mf_owned"), "Delete Me");

        filterNavigation.goToDefault();
        assertFilterIsNotInList(parse.filter().parseFilterList("mf_favourites"), "Delete Me");
    }

    private void assertFilterIsNotInList(FilterList filterList, String filterName)
    {
        final List<FilterItem> filterItems = filterList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            if (filterItem.getName().equals(filterName))
            {
                fail("This filter '" + filterName + "' should not exist in the filter list");
            }
        }
    }

    private void assertFilterIsInList(FilterList filterList, String filterName)
    {
        List<FilterItem> filterItems = filterList.getFilterItems();
        for (FilterItem filterItem : filterItems)
        {
            if (filterItem.getName().equals(filterName))
            {
                return;
            }
        }
        fail("Failed to find filter in list : " + filterName);
    }

    private void assertOnManageFiltersPage()
    {
        text.assertTextPresent(new CssLocator(tester, "#content > header h1"), "Manage Filters");
    }
}
