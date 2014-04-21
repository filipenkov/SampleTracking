package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.ProjectCondition;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.json.TestJSONException;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Test to ensure that changes to the filter can be saved.
 *
 * @since v3.13.2
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestSaveFilter extends FuncTestCase
{
    private static final int ADMIN_ALLFILTER_ID = 10000;

    /**
     * Make sure there is an error if no filter exists.
     */
    public void testSaveNoFilter()
    {
        administration.restoreData("TestSaveFilter.xml");
        navigation.logout();
        navigation.login(FRED_USERNAME);

        tester.gotoPage("secure/SaveFilter!default.jspa");

        text.assertTextPresent(locator.page(), "There is no current search request");
    }

    /**
     * Make sure there is an error if user tries to save a filter they don't own.
     * Also testing that if they edit the filter, they don't get all the extra options that only owners get.
     */
    public void testSaveNotOwner()
    {
        administration.restoreData("TestSaveFilter.xml");
        navigation.login(FRED_USERNAME);

        navigation.issueNavigator().loadFilter(ADMIN_ALLFILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        tester.gotoPage("secure/SaveFilter!default.jspa");

        text.assertTextPresent(locator.page(), "You may only create, modify or delete filters that you own.");

        final ProjectCondition projectCondition = new ProjectCondition();
        projectCondition.addOption("monkey");
        final NavigatorSearch expectedSearch = new NavigatorSearch(projectCondition);

        navigation.issueNavigator().modifySearch(expectedSearch);

        text.assertTextPresent(locator.xpath("//ul[@id='filter-description']"), "Filter modified since loading");
        tester.assertLinkPresent("copyasnewfilter");
        tester.assertLinkNotPresent("filtereditshares");
        tester.assertLinkNotPresent("filtersave");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//div[@id='issue-filter']", "Subscriptions");
    }

    /**
     * JRA-15770: Should be able to save search parameter changes even when there are bad shares. Only changes the search parameters may be saved.
     */
    public void testSaveBadSharePermissions()
    {
        final SharedEntityInfo fredAllFilter = new SharedEntityInfo(10000L, "FredAll", "Fred All description", true, ImmutableSet.of(new GroupTestSharingPermission("jira-developers")));

        administration.restoreData("TestSaveFilterBadShares.xml");
        navigation.login(FRED_USERNAME);

        navigation.issueNavigator().loadFilter(fredAllFilter.getId(), IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        final ProjectCondition projectCondition = new ProjectCondition();
        projectCondition.addOption("monkey");
        final NavigatorSearch expectedSearch = new NavigatorSearch(projectCondition);

        navigation.issueNavigator().modifySearch(expectedSearch);
        navigation.issueNavigator().saveCurrentFilter();

        navigation.issueNavigator().loadFilter(fredAllFilter.getId(), IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        //make sure the filter has been saved.
        assertions.getIssueNavigatorAssertions().assertSimpleSearch(expectedSearch, tester);

        //make sure the filter's name, description, favourite and sharing remain the same.
        assertFilterDetails(fredAllFilter);
    }

    /**
     * Assert that the passed filter is stored correctly.
     *
     * @param expectedInfo the information that should currently be stored.
     */
    private void assertFilterDetails(final SharedEntityInfo expectedInfo)
    {
        tester.gotoPage("secure/EditFilter!default.jspa");

        tester.assertFormElementEquals("filterName", expectedInfo.getName());
        tester.assertFormElementEquals("filterDescription", expectedInfo.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(expectedInfo.isFavourite()));
        assertEquals(expectedInfo.getSharingPermissions(), parsePermissions());
    }

    /**
     * Return the current share permissions
     *
     * @return the permissions for the current filter.
     */
    private Set <TestSharingPermission> parsePermissions()
    {
        Locator xpath = new XPathLocator(tester, "//span[@id='shares_data']");
        String value = xpath.getText();
        try
        {
            return TestSharingPermissionUtils.parsePermissions(value);
        }
        catch (TestJSONException e)
        {
            fail("Unable to parse shares: " + e.getMessage());
            return null;
        }
    }
}
