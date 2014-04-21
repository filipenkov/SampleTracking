package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearchBuilder;
import com.atlassian.jira.functest.framework.navigator.QuerySearchCondition;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Do a simple test of the Save and SaveAs functionality of navigator. This should be applicable to all versions of JIRA. The
 * test data is stored in Standard Form.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestSaveAsFilterSimple extends FuncTestCase
{
    private static final NavigatorSearch NAV_SEARCH1;
    private static final NavigatorSearch NAV_SEARCH2;
    private static final long FEATURE_FILTER_ID = 10001;

    private static final String ALL_FILTER_NAME = "AllFilter";
    private static final String FEATURE_FILTER_NAME = "FeatureFilter";
    private static final NavigatorSearch FEATURE_FILTER;
    private static final SharedEntityInfo FEATURE_FILTER_INFO = new SharedEntityInfo(FEATURE_FILTER_NAME, null, false, null);

    static
    {
        NavigatorSearchBuilder builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.BUG);
        builder.addQueryString("test").addQueryField(QuerySearchCondition.QueryField.SUMMARY);

        NAV_SEARCH1 = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addQueryString("super bad").addQueryField(QuerySearchCondition.QueryField.ENVIRONMENT);

        NAV_SEARCH2 = builder.createSearch();

        builder = new NavigatorSearchBuilder();
        builder.addIssueType(IssueTypeCondition.IssueType.NEW_FEATURE);

        FEATURE_FILTER = builder.createSearch();
    }

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSaveAsFilterSimple.xml");
    }

    /**
     * Test to make sure that the filter is favourited by default.
     */
    public void testSaveFavouriteByDefault()
    {
        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("Save");

        tester.assertFormElementEquals("favourite", "true");
    }

    /**
     * Test to make sure that SaveAs saves favourite by default.
     */
    public void testSaveAsFavouriteByDefault()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);
        tester.clickLinkWithText("Save as");

        tester.assertFormElementEquals("favourite", "true");
    }

    /**
     * Test to make sure that you can't save without a filter in the session.
     */
    public void testSaveNoSearchInSession()
    {
        navigation.login(ADMIN_USERNAME);

        tester.gotoPage("secure/SaveAsFilter!default.jspa");
        text.assertTextPresent(new WebPageLocator(tester), "There is no current search request.");

        tester.gotoPage("secure/SaveAsFilter.jspa");
        text.assertTextPresent(new WebPageLocator(tester), "There is no current search request.");
    }

    /**
     * Test to make sure that the anonymous user cannot save search requests.
     */
    public void testAnonymousCantSave()
    {
        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().displayAllIssues();

        tester.assertLinkNotPresentWithText("Save");

        tester.gotoPage("secure/SaveAsFilter!default.jspa");
        tester.assertSubmitButtonNotPresent("Save");

        tester.gotoPage("secure/SaveAsFilter.jspa");
        tester.assertSubmitButtonNotPresent("Save");
    }

    /**
     * Test that you can't save filter with invalid name.
     */
    public void testSaveInvalidName()
    {
        final SharedEntityInfo info = new SharedEntityInfo(null, null, false, null);

        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        saveFilterNoId(info);
        assertCurrentScreenCorrect(info);

        text.assertTextPresent(new WebPageLocator(tester), "You must specify a name to save this filter as.");
    }

    /**
     * Test that you can't save a modified filter with invalid name.
     */
    public void testSaveAsInvalidName()
    {
        final SharedEntityInfo info = new SharedEntityInfo(null, "This is a cool description", true, null);

        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        saveAsFilterNoId(info);
        assertCurrentScreenCorrect(info);

        text.assertTextPresent(new WebPageLocator(tester), "You must specify a name to save this filter as.");

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(FEATURE_FILTER_INFO, FEATURE_FILTER);
    }

    /**
     * This test attempts to create a filter with an incredibly long name that should be caught by the action which should
     * in turn complain with an error message which the method asserts is present in the response.
     */
    public void testSaveWithAFilterNameThatsTooLong()
    {
        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        final char[] nameChars = new char[ 500 ];
        Arrays.fill( nameChars, 'x');

        final String name = new String( nameChars );
        final String description = "Description";
        final boolean favourite = false;
        final String linkText = "Save";

        this.saveFilter( name, description, favourite, linkText );
        text.assertTextPresent(new WebPageLocator(tester), "The entered filter name is too long, it must be less than 255 chars.");
    }

    /**
     * Make sure you can't add a filter with the same name.
     */
    public void testSaveAlreadySaved()
    {
        final SharedEntityInfo info = new SharedEntityInfo(ALL_FILTER_NAME, "This is a cool description", true, null);

        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().displayAllIssues();
        saveFilterNoId(info);
        assertCurrentScreenCorrect(info);

        text.assertTextPresent(new WebPageLocator(tester), "Filter with same name already exists.");
    }

    /**
     * Make sure you can't save a modified filter with the same name.
     */
    public void testSaveAsAlreadySaved()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo info = new SharedEntityInfo(FEATURE_FILTER_NAME, null, true, null);

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(NAV_SEARCH2);

        saveAsFilterNoId(info);
        assertCurrentScreenCorrect(info);
        text.assertTextPresent(new WebPageLocator(tester), "Filter with same name already exists.");

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(FEATURE_FILTER_INFO, FEATURE_FILTER);
    }

    /**
     * Make sure you can't mess up the save with an invalid favourite value.
     */
    public void testInvalidFavourite()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo info = new SharedEntityInfo("testInvalidFavourite", null, false, null);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        tester.gotoPage("secure/SaveAsFilter.jspa?filterName=testInvalidFavourite&favourite=badjskajds&submit=Save");
        assertSearchSavedCorrectly(info, NAV_SEARCH1);
    }

    /**
     * Make sure you can't edit another filter by passing in the ID.
     */
    public void testInvalidFilterId()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo info = new SharedEntityInfo("testInvalidFilterId", null, true, null);

        navigation.issueNavigator().createSearch(NAV_SEARCH1);
        tester.gotoPage("secure/SaveAsFilter.jspa?filterName=testInvalidFilterId&submit=Save&filterId" + FEATURE_FILTER_ID);

        long id = getFilterIdAfterSave();

        assertFalse("Should only create new filters.", FEATURE_FILTER_ID == id);
        assertSearchSavedCorrectly(info, NAV_SEARCH1);

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertSearchSavedCorrectly(FEATURE_FILTER_INFO, FEATURE_FILTER);
    }

    /**
     * Make sure that you can save filter created under anonymous user. This can happen if a user creates a search before
     * they have actually logged in.
     */
    public void testSaveAnonymousSearch()
    {
        navigation.logout();
        navigation.gotoDashboard();

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAnonymousSearch", null, false, null);

        navigation.issueNavigator().createSearch(NAV_SEARCH2);

        navigation.login(ADMIN_USERNAME);

        navigation.issueNavigator().gotoNavigator();
        saveFilter(info);
        assertSearchSavedCorrectly(info, NAV_SEARCH2);
    }

    /**
     * Test some XSS names and descriptions.
     */

    public void testXSSSaveFilter()
    {
        navigation.login(ADMIN_USERNAME);

        SharedEntityInfo navigatorSearchInfo = new SharedEntityInfo("<b>testXSSSaveFilter</b>", "<b>description is rea=lly cool</b>", true, null);

        //note that we expect the webtest to remove any tags from the text. Thus is the title and descritoion it correct,
        //then we have escaped them correctly.
        createAndCheckFilter(navigatorSearchInfo, NAV_SEARCH1);
    }

    /**
     * Test some XSS names and descriptions.
     */

    public void testXSSSaveAsFilter()
    {
        navigation.login(ADMIN_USERNAME);

        SharedEntityInfo navigatorSearchInfo = new SharedEntityInfo("<b>testXSSSaveAsFilter</b>", "<b>description is rea=lly cool</b>", true, null);

        //note that we expect the webtest to remove any tags from the text. Thus is the title and descritoion it correct,
        //then we have escaped them correctly.
        modifyAndCheckFilter(navigatorSearchInfo, NAV_SEARCH1);
    }

    /**
     * Make sure you can add a filter with no description.
     */
    public void testSaveFilter()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo searchInfo = new SharedEntityInfo("testSaveFilter", null, true, null);
        createAndCheckFilter(searchInfo, NAV_SEARCH1);
    }

    /**
     * Make sure you can add a filter with no description.
     */
    public void testSaveAsFilter()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo searchInfo = new SharedEntityInfo("testSaveAsFilter", null, true, null);
        modifyAndCheckFilter(searchInfo, NAV_SEARCH2);
    }

    /**
     * Test with save description to ensure that it works as expected.
     */
    public void testSaveFilterWithDescription()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveFilterWithDescription", "Yet another description 4 testing.", true, null);
        createAndCheckFilter(info, NAV_SEARCH2);
    }

    /**
     * Test with save description to ensure that it works as expected.
     */
    public void testSaveAsFilterWithDescription()
    {
        navigation.login(ADMIN_USERNAME);

        final SharedEntityInfo info = new SharedEntityInfo("testSaveAsFilterWithDescription", "Description 4 test", true, null);
        modifyAndCheckFilter(info, NAV_SEARCH1);
    }

    /**
     * Test with save description to ensure that it works as expected.
     */
    public void testSaveFilterWithNotFavourite()
    {
        navigation.login(ADMIN_USERNAME);
        final SharedEntityInfo searchInfo = new SharedEntityInfo("testSaveFilterWithNot F= avourite", null, false, null);
        createAndCheckFilter(searchInfo, NAV_SEARCH1);
    }

     /**
     * Test with save description to ensure that it works as expected.
     */
    public void testSaveAsFilterWithNotFavourite()
    {
        navigation.login(ADMIN_USERNAME);
        final SharedEntityInfo searchInfo = new SharedEntityInfo("testSaveAsFilterWithNotFavourite", "Yey this is a saved filter", false, null);
        modifyAndCheckFilter(searchInfo, NAV_SEARCH2);
    }

    /**
     * Modify the filter and ensure it was saved correctly. Also validates that the original filter remains unchanged.
     *
     * @param info the search information.
     * @param search the search to create and check.
     * @return the id of the filter created.
     */
    private long modifyAndCheckFilter(final SharedEntityInfo info, final NavigatorSearch search)
    {
        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().modifySearch(search);
        final long id = saveAsFilter(info);
        assertSearchSavedCorrectly(info, search);

        navigation.issueNavigator().loadFilter(FEATURE_FILTER_ID, null);
        assertSearchSavedCorrectly(FEATURE_FILTER_INFO, FEATURE_FILTER);

        return id;
    }

    /**
     * Create the filter and ensure that it was added correctly.
     *
     * @param info the search info.
     * @param search the search to create and check.
     * @return the id of the filter created.
     */
    private long createAndCheckFilter(final SharedEntityInfo info, final NavigatorSearch search)
    {
        navigation.issueNavigator().createSearch(search);
        long id = saveFilter(info);
        assertSearchSavedCorrectly(info, search);
        return id;
    }

    /**
     * Save the passed navigator search.
     *
     * @param info the search to save.
     */
    private void saveFilterNoId(final SharedEntityInfo info)
    {
        saveFilter(info.getName(), info.getDescription(), info.isFavourite(), "Save");
    }

    /**
     * Save the passed navigator search.
     *
     * @param info the search to save.
     */
    private void saveAsFilterNoId(final SharedEntityInfo info)
    {
        saveFilter(info.getName(), info.getDescription(), info.isFavourite(), "Save As");
    }

    /**
     * Save the passed navigator search.
     *
     * @param info the search to save.
     * @return the id of the search to save.
     */
    private long saveFilter(final SharedEntityInfo info)
    {
        saveFilterNoId(info);
        return getFilterIdAfterSave();
    }

    /**
     * Save the passed navigator search.
     *
     * @param search the search to save.
     * @return the id of the search to save.
     */

    private long saveAsFilter(final SharedEntityInfo search)
    {
        saveAsFilterNoId(search);
        return getFilterIdAfterSave();
    }

    /**
     * Save the current filter. Assumes you are on the issue navigator screen.
     *
     * @param name the name of the filter.
     * @param description the name of the description.
     * @param favourite should be filter be a favourite.
     * @param linkText the link to click to save the filter.
     */
    private void saveFilter(final String name, final String description, final boolean favourite, final String linkText)
    {
        if (favourite)
        {
            tester.clickLinkWithText(linkText);
            tester.setFormElement("filterName", name);
            tester.setFormElement("filterDescription", description);
            tester.submit("saveasfilter_submit");
        }
        else
        {
            //This is a hack to get around the fact that JWebUnit does not support
            //setting hidden fields. Ahhhh.....

            saveUsingPut(name, description, favourite);
        }

    }

    /**
     * Save the filter directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     */
    private void saveUsingPut(final String name, final String description, final boolean favourite)
    {
        tester.gotoPage(createSaveUrl(name, description, favourite));
    }

    /**
     * Create the URL that can be used to perform a filter save.
     *
     * @param name the name of the search.
     * @param description the description of the search.
     * @param favourite should the filter be saved as a favourite.
     * @return the URL for the filter save.
     */
    private String createSaveUrl(String name, String description, boolean favourite)
    {
        StringBuilder buffer = new StringBuilder("secure/SaveAsFilter.jspa?submit=Save");
        if (!StringUtils.isBlank(name))
        {
            buffer.append("&filterName=").append(encode(name));
        }
        if (!StringUtils.isBlank(description))
        {
            buffer.append("&filterDescription=").append(encode(description));
        }
        buffer.append("&favourite=").append(String.valueOf(favourite));
        return buffer.toString();
    }

    private String encode(String name)
    {
        try
        {
            return URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse the URL after the filter has been saved to find the new filter id.
     *
     * @return the filter
     */
    private long getFilterIdAfterSave()
    {
        URL url = tester.getDialog().getResponse().getURL();
        if (url.getQuery() == null)
        {
            fail("Unable to save filter: Not redirected to navigator.");
        }
        else
        {
            Pattern pattern = Pattern.compile("requestId=(\\d+)");
            Matcher matcher = pattern.matcher(url.getQuery());
            if (matcher.find())
            {
                return Long.parseLong(matcher.group(1));
            }
            else
            {
                fail("Unable to save filter: Not redirected to navigator.");
            }
        }
        return Long.MIN_VALUE;
    }

    /**
     * Assert that the current search matches the passed search.
     *
     * @param info the search information to check.
     * @param expectedSearch the search to check.
     */
    private void assertSearchSavedCorrectly(final SharedEntityInfo info, final NavigatorSearch expectedSearch)
    {
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        assertions.getIssueNavigatorAssertions().assertSimpleSearch(expectedSearch, tester);
        assertions.getIssueNavigatorAssertions().assertSearchInfo(info);

        assertFilterOnManage(info);
    }

    /**
     * Assert that the current SaveAs state is correctly.
     *
     * @param info the state used to perform the check.
     */
    private void assertCurrentScreenCorrect(final SharedEntityInfo info)
    {
        tester.assertFormElementEquals("filterName", info.getName());
        tester.assertFormElementEquals("filterDescription", info.getDescription());
        tester.assertFormElementEquals("favourite", String.valueOf(info.isFavourite()));
    }

    /**
     * Make sure that the passed search is on the manage filter page.
     *
     * @param info the information to check.
     */
    private void assertFilterOnManage(final SharedEntityInfo info)
    {
        navigation.manageFilters().myFilters();
        FilterList list = parse.filter().parseFilterList(FilterParser.TableId.OWNED_TABLE);
        for (FilterItem item : list.getFilterItems())
        {
            if (info.getName().equals(item.getName()))
            {
                if (info.getDescription() == null)
                {
                    assertNull(item.getDescription());
                }
                else
                {
                    assertEquals(info.getDescription(), item.getDescription());
                }
                assertEquals(info.isFavourite(), item.isFav().booleanValue());
                return;
            }
        }
        fail("Filter not displayed on manage filters.");
    }
}
