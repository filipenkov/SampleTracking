package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.util.Collections;
import java.util.List;

/**
 * The FilterNavigation representing the ManageFilters views, this implementation should be complete and
 * definitive of the full functionality of the view.
 */
public class ManageFiltersNavigation extends AbstractNavigationUtil implements FilterNavigation
{
    public ManageFiltersNavigation(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData);
    }

    public void addFavourite(final int id)
    {
        tester.gotoPage("secure/AddFavourite.jspa?entityId=" + id + "&entityType=SearchRequest");
    }

    public void removeFavourite(final int id)
    {
        tester.gotoPage("secure/RemoveFavourite.jspa?entityId=" + id + "&entityType=SearchRequest");
    }

    public void goToDefault()
    {
        favouriteFilters();
    }

    public void manageSubscriptions(final int filterId)
    {
        tester.gotoPage("secure/ViewSubscriptions.jspa?filterId=" + filterId);
    }

    public void favouriteFilters()
    {
        tester.gotoPage("secure/ManageFilters.jspa?filterView=favourites");
    }

    public void myFilters()
    {
        tester.gotoPage("secure/ManageFilters.jspa?filterView=my");
    }

    public void allFilters()
    {
        tester.gotoPage("secure/ManageFilters.jspa?filterView=search&pressedSearchButton=true&searchName=&searchOwner=&Search=");
    }

    public void popularFilters()
    {
        tester.gotoPage("secure/ManageFilters.jspa?filterView=popular");
    }

    public void searchFilters()
    {
        tester.gotoPage("secure/ManageFilters.jspa?filterView=search");
    }

    public long createFilter(final String filterName, final String filterDesc)
    {
        final SharedEntityInfo info = new SharedEntityInfo(filterName, filterDesc, true, null);
        final NavigatorSearch search = new NavigatorSearch(Collections.<NavigatorCondition>emptyList());
        return getNavigation().issueNavigator().createNewAndSaveAsFilter(info, search);
    }

    public void deleteFilter(final int id)
    {
        tester.gotoPage("secure/DeleteFilter.jspa?filterId=" + id + "&returnUrl=ManageFilters.jspa");
    }

    public void findFilters(final String filterName, final String authorUsername)
    {
        tester.getDialog().setFormParameter("searchName", filterName);
        tester.getDialog().setFormParameter("searchOwnerUserName", authorUsername);
        tester.submit("Search");
    }

    public List<FilterItem> sanitiseSearchFilterItems(final List<FilterItem> expectedItems)
    {
        return expectedItems;
    }

    public List<FilterItem> sanitiseFavouriteFilterItems(final List<FilterItem> expectedItems)
    {
        return expectedItems;
    }

    public String getActionBaseUrl()
    {
        return "ManageFilters.jspa";
    }

    public FilterNavigation projects()
    {
        throw new UnsupportedOperationException("The projects view is unique to the filterpickerpopup implementation");
    }
}
