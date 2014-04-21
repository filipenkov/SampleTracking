package com.atlassian.jira.webtest.selenium.menu;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since v4.00
 */
@SkipInBrowser(browsers={Browser.IE}) //Text (' Issue Navigator ') not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestIssuesMenu extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";

    private final String CONTEXT = getEnvironmentData().getContext();

    private static final int ISSUES_OVERLOAD_NO = 5;
    private static final int FILTERS_OVERLOAD_NO = 10;

    private static final Map<String, String> LINK_TO_PAGE = new LinkedHashMap<String, String>();
    private static final Map<String, String> ISSUE_TO_ID = new LinkedHashMap<String, String>();

    private static final String ISSUES_LINK = "find_link";
    private static final String ISSUES_PAGE = "Issue Navigator";
    private static final String ISSUES_DROPDOWN = "find_link_drop";

    private static final String CREATE_FILTER_LINK = "issues_new_search_link_lnk";
    private static final String CREATE_FILTER_PAGE = "Issue Navigator";
    private static final String CREATE_ISSUE_LINK = "issues_new_issue_link_lnk";
    private static final String CREATE_ISSUE_PAGE = "Create Issue";
    private static final String MANAGE_FILTER_LINK = "issues_manage_filters_link_lnk";
    private static final String MANAGE_FILTER_PAGE = "Manage Filters";

    private static final String ACTIVE_FILTER_TAB_XPATH = "//ul[@id='filterFormHeader']/li[@class='active']";
    private final String RECENT_ISSUES_LIST_XPATH = "//ul[@id='issues_history_main']/li[<no>]/a[@href='" + CONTEXT + "/browse/<issueKey>']";
    private final String MORE_ISSUES_LIST_XPATH = "//ul[@id='issues_history_main']/li[" + (ISSUES_OVERLOAD_NO + 1) + "]/a[@href='" + CONTEXT + "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=key+in+issueHistory%28%29']";
    private final String MORE_FILTERS_LIST_XPATH = "//ul[@id='issues_filter_main']/li[" + (FILTERS_OVERLOAD_NO + 1) + "]/a[@href='" + CONTEXT + "/secure/ManageFilters.jspa?filterView=favourites']";
    private static final String FAVOURITE_FILTERS_LIST_XPATH = "//ul[@id='issues_filter_main']/li[<no>]/a";
    private final String MY_FILTERS_LIST_TAB_XPATH = "//a[@href='" + CONTEXT + "/secure/ManageFilters.jspa?filterView=my']";
    private final String POPULAR_FILTERS_LIST_TAB_XPATH = "//a[@href='" + CONTEXT + "/secure/ManageFilters.jspa?filterView=popular']";
    private static final String EDIT_FILTER_A_LINK = "edit_filter_10000";


    private static final String[] LOGGED_IN_ISSUES_LIST = { CREATE_FILTER_LINK, CREATE_ISSUE_LINK, MANAGE_FILTER_LINK };
    private static final String[] LOGGED_IN_DIFF_LOGGED_OUT_LIST = { CREATE_ISSUE_LINK };
    private static final String[] LOGGED_OUT_ISSUES_LIST = { CREATE_FILTER_LINK, MANAGE_FILTER_LINK };

    private static final String ISSUE_ID_LINK = "TPA-";

    private static final String[] FILTER_LIST = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k" };
    private static final String FILTERNAME_ELEMENT = "//h1[@class='item-summary']";
    private static final String FILTER_B_ID = "filter_lnk_10001";
    private static final String FILTER_A_ID = "filter_lnk_10000";
    private static final String UNSAVED_FILTER_LINK = "curr_search_lnk_unsaved_lnk";

    public static Test suite()
    {
        return suiteFor(TestIssuesMenu.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssuesMenu.xml");

        LINK_TO_PAGE.put(CREATE_FILTER_LINK, CREATE_FILTER_PAGE);
        LINK_TO_PAGE.put(CREATE_ISSUE_LINK, CREATE_ISSUE_PAGE);
        LINK_TO_PAGE.put(MANAGE_FILTER_LINK, MANAGE_FILTER_PAGE);

        ISSUE_TO_ID.put("TPA-1", "abc");
        ISSUE_TO_ID.put("TPA-2", "def");
        ISSUE_TO_ID.put("TPA-3", "ghi");
        ISSUE_TO_ID.put("TPA-4", "jkl");    
        ISSUE_TO_ID.put("TPA-5", "mno");
        ISSUE_TO_ID.put("TPA-6", "pqr");
        ISSUE_TO_ID.put("TPA-7", "a1");

        client.deleteCookie("jira.issue.navigator.type", getEnvironmentData().getContext() + "/secure/");
    }

    public void testIssuesMenu()
    {
//        getNavigator().logout(getXsrfToken());
//        _testLoggedOutNoIssuesPermission();
//        _testNoFiltersCurrentFilter();
//        _testLoggedOutWithIssuesPermission();
//        _testCreateIssues();
//        _testOverloadIssues();

        //restore data and log in
        restoreData("TestIssuesMenu.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        _testLoggedIn();
        _testNoFiltersCurrentFilter();
        _testCreateIssues();
        _testOverloadIssues();

        _testChangeHistory();
        _testDeleteIssue();
        _testSubtaskAppearsInRecentIssues();


        _testCreateFilters();
        _testOverloadFilters();
        _testDeleteFilter();

        _testUnfavouriteFilter();
        _testFavouriteFilter();
        _testChangePermission();

    }

    private void _testSubtaskAppearsInRecentIssues()
    {
        //Create a subtask
        client.click(ISSUES_DROPDOWN);
        String subtaskIssue = RECENT_ISSUES_LIST_XPATH.replace("<no>", "" + 5).replace("<issueKey>", "TPA-1");
        assertThat.visibleByTimeout(subtaskIssue, TIMEOUT);
        client.click(subtaskIssue, true);
        client.click("create-subtask", true);
        client.typeInElementWithName("summary", "a1");
        client.click("Create", true);

        checkRecentIssues("TPA-7",
                "TPA-1",
                "TPA-6",
                "TPA-5",
                "TPA-3");
        checkNoIssue("TPA-2");
    }


    private void _testLoggedOutNoIssuesPermission()
    {
        getNavigator().gotoHome();
        checkIssuesDropDown();
        checkLinks(LOGGED_OUT_ISSUES_LIST);
        checkNoLinks(LOGGED_IN_DIFF_LOGGED_OUT_LIST);
    }

    private void _testNoFiltersCurrentFilter()
    {
        //Create a filter on the fly
        client.click(ISSUES_LINK);
        assertThat.visibleByTimeout("issue-filter-submit-base", TIMEOUT);
        client.click("issue-filter-submit-base", true);

        //Check unsaved filter appears in dropdown and can be accessed
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        assertThat.elementVisibleContainsText(UNSAVED_FILTER_LINK, "Unsaved filter (No issues)");
        client.click(UNSAVED_FILTER_LINK, true);
        assertThat.elementContainsText(ACTIVE_FILTER_TAB_XPATH, "Summary");
    }

    private void _testLoggedOutWithIssuesPermission()
    {
        //Give create new issues permission to anyone
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
        client.click("0_edit", true);
        client.click("add_perm_11", true);
        client.click("type_group");
        client.click(" Add ", true);
        getNavigator().logout(getXsrfToken());

        //New permission changes the dropdown
        getNavigator().gotoHome();
        checkIssuesDropDown();
        checkLinks(LOGGED_IN_ISSUES_LIST);

    }

    private void _testLoggedOutIssuesPermissionDeleted()
    {
        //Delete permission given previously
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
        client.click("0_edit", true);
        client.click("del_perm_11_", true);
        client.click("Delete", true);
        getNavigator().logout(getXsrfToken());

        //Check that dropdown returns to normal
        _testLoggedOutNoIssuesPermission();

    }

    private void _testCreateIssues()
    {
        for (int i = 0; i < ISSUES_OVERLOAD_NO; i++)
        {
            createIssue("TPA", ISSUE_TO_ID.get(ISSUE_ID_LINK + (i+1)));
        }

        checkRecentIssues("TPA-5",
                "TPA-4",
                "TPA-3",
                "TPA-2",
                "TPA-1");

    }

    private void _testOverloadIssues()
    {
        createIssue("TPA", ISSUE_TO_ID.get(ISSUE_ID_LINK + (ISSUES_OVERLOAD_NO+1)));

        checkRecentIssues("TPA-6",
                "TPA-5",
                "TPA-4",
                "TPA-3",
                "TPA-2",
                "TPA-1");
    }

    private void _testLoggedIn()
    {
        getNavigator().gotoHome();
        checkIssuesDropDown();
        checkLinks(LOGGED_IN_ISSUES_LIST);
    }

    private void _testChangeHistory()
    {
        //Change the order of history
        client.click(ISSUES_DROPDOWN);
        String change = RECENT_ISSUES_LIST_XPATH.replace("<no>", "" + 3).replace("<issueKey>", "TPA-4");
        assertThat.visibleByTimeout(change, TIMEOUT);
        client.click(change, true);

        checkRecentIssues("TPA-4",
                "TPA-6",
                "TPA-5",
                "TPA-3",
                "TPA-2",
                "TPA-1");

    }

    private void _testDeleteIssue()
    {
        //Delete an issue
        client.click(ISSUES_DROPDOWN);
        String delete = RECENT_ISSUES_LIST_XPATH.replace("<no>", "" + 1).replace("<issueKey>", "TPA-4");
        assertThat.visibleByTimeout(delete, TIMEOUT);
        client.click(delete, true);
        client.click("delete-issue");
        assertThat.elementPresentByTimeout("id=delete-issue-submit", TIMEOUT);
        client.click("id=delete-issue-submit");

        assertThat.elementPresentByTimeout("id=issuetable", TIMEOUT);

        checkRecentIssues("TPA-6",
                "TPA-5",
                "TPA-3",
                "TPA-2",
                "TPA-1");
        checkNoIssue("TPA-3");

    }

    private void _testCreateFilters()
    {
        for (int i = 0; i < FILTERS_OVERLOAD_NO; i++)
        {
            createSavedFilter(FILTER_LIST[i]);
        }

        checkFilters("a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j"
        );
    }

    private void _testOverloadFilters()
    {
        createSavedFilter(FILTER_LIST[FILTERS_OVERLOAD_NO]);

        checkFilters("a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k"
        );
    }

    private void _testDeleteFilter()
    {
        //Delete a filter
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click("delete_10001");
        assertThat.textPresentByTimeout("Delete Filter: b", 10000);
        client.click("Delete");
        assertThat.elementNotPresentByTimeout("mf_10001", 10000);

        checkFilters("a",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k"
        );
        checkNoFilter(FILTER_B_ID);
    }

    private void _testUnfavouriteFilter()
    {
        //Unfavourite a filter
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click("fav_a_mf_favourites_SearchRequest_10005");

        assertThat.elementNotPresentByTimeout("fav_a_mf_owned_SearchRequest_10005", TIMEOUT);
        checkFilters("a",
                "c",
                "d",
                "e",
                "g",
                "h",
                "i",
                "j",
                "k"
        );


    }

    private void _testFavouriteFilter()
    {
        //Favourite a filter
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click(MY_FILTERS_LIST_TAB_XPATH);
        assertThat.elementPresentByTimeout("fav_a_mf_owned_SearchRequest_10005", 10000);
        client.click("fav_a_mf_owned_SearchRequest_10005");

        assertThat.elementPresentByTimeout("jquery=#fav_a_mf_owned_SearchRequest_10005.enabled", TIMEOUT);
        checkFilters("a",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k"
        );
    }

    private void _testChangePermission()
    {
        //Change permissions for filter to global
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click(EDIT_FILTER_A_LINK, true);
        client.click("share_add_global");
        client.click("Save", true);

        //Check that another user can favourite the global filter
        getNavigator().logout(getXsrfToken());
        getNavigator().login(TEST_USERNAME, TEST_PASSWORD);
        checkNoFilter(FILTER_A_ID);                
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click(POPULAR_FILTERS_LIST_TAB_XPATH);
        assertThat.elementPresentByTimeout("fav_a_mf_popular_SearchRequest_10000", TIMEOUT);
        client.click("fav_a_mf_popular_SearchRequest_10000");

        assertThat.elementPresentByTimeout("jquery=#fav_a_mf_popular_SearchRequest_10000.enabled", TIMEOUT);
        checkFilters("a");

        //Delete filter's global permission
        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(MANAGE_FILTER_LINK, TIMEOUT);
        client.click(MANAGE_FILTER_LINK, true);
        client.click(MY_FILTERS_LIST_TAB_XPATH);
        assertThat.elementPresentByTimeout(EDIT_FILTER_A_LINK, TIMEOUT);
        client.click(EDIT_FILTER_A_LINK, true);
        client.click("//img[@class='shareTrash']");
        client.click("Save", true);

        //Check that another user can no longer access the filter
        getNavigator().logout(getXsrfToken());
        getNavigator().login(TEST_USERNAME, TEST_PASSWORD);
        checkNoFilter(FILTER_A_ID);


    }

    private void checkIssuesLinks()
    {
        client.click(ISSUES_DROPDOWN);
        assertThat.textPresentByTimeout("Recent Issues", TIMEOUT);

    }

    private void checkRecentIssues(String... issuesList)
    {
        //For all visible issues click and check they go to the right page
        for (int i = 0; i < ISSUES_OVERLOAD_NO && i < issuesList.length; i++)
        {
            checkIssuesLinks();
            String replaced = RECENT_ISSUES_LIST_XPATH.replace("<no>", "" + ISSUES_OVERLOAD_NO).replace("<issueKey>", issuesList[ISSUES_OVERLOAD_NO - i - 1]);
            assertThat.visibleByTimeout(replaced, TIMEOUT);
            client.click(replaced, true);
            assertThat.textPresent(ISSUE_TO_ID.get(issuesList[ISSUES_OVERLOAD_NO - i - 1]));

        }
        //If there are more issues, check more issues link appears and works
        if (issuesList.length > ISSUES_OVERLOAD_NO)
        {
            client.click(ISSUES_DROPDOWN);
            assertThat.visibleByTimeout(MORE_ISSUES_LIST_XPATH, TIMEOUT);
            client.click(MORE_ISSUES_LIST_XPATH, true);
            assertThat.textPresent(" Issue Navigator ");
            assertThat.elementContainsText("fieldJqlQuery", "key in issueHistory()");
            assertThat.textPresent("Key descending");
        }
    }

    private void createIssue(String projectName, String issueSummary)
    {
        //Create issue with projectName and issueSummary
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(CREATE_ISSUE_LINK, TIMEOUT);
        client.click(CREATE_ISSUE_LINK, true);
        client.select("project", projectName);
        client.click("Next", true);
        client.typeInElementWithName("summary", issueSummary);
        client.click("Create", true);
    }

    private void checkIssuesDropDown()
    {
        assertThat.visibleByTimeout(ISSUES_LINK, TIMEOUT);
        client.click(ISSUES_LINK, true);
        assertThat.textPresent(ISSUES_PAGE);
        assertThat.elementVisible(ISSUES_DROPDOWN);

    }

    private void checkLinks(String... linkOrder)
    {
        //Check links appear in the issue dropdown
        for (int i = 0; i < linkOrder.length; i++)
        {
            client.click(ISSUES_DROPDOWN);
            assertThat.visibleByTimeout(linkOrder[i], TIMEOUT);
            client.click(linkOrder[i], true);
            assertThat.textPresent(LINK_TO_PAGE.get(linkOrder[i]));
            getNavigator().gotoHome();
        }
    }

    private void checkNoLinks(String... linkOrder)
    {
        //Check links do not appear in the issue dropdown
        for (int i = 0; i < linkOrder.length; i++)
        {
            client.click(ISSUES_DROPDOWN);
            assertThat.elementNotPresentByTimeout(linkOrder[i], TIMEOUT);
            getNavigator().gotoHome();
        }
    }

    private void createSavedFilter(String filterName)
    {
        //Create a filter and save it
        client.click(ISSUES_DROPDOWN);
        assertThat.visibleByTimeout(CREATE_FILTER_LINK, TIMEOUT);
        client.click(CREATE_FILTER_LINK, true);
        client.click("id=issue-filter-submit", true);
        client.click("filtersavenew", true);
        client.typeInElementWithName("filterName", filterName);
        client.click("submit", true);
    }

    private void checkFilters(String... filterName)
    {
        //For all visible filters click and check they go to the right page
        for (int i = 0; i < FILTERS_OVERLOAD_NO && i < filterName.length; i++)
        {
            assertThat.visibleByTimeout(ISSUES_DROPDOWN, TIMEOUT);
            client.click(ISSUES_DROPDOWN);
            visibleByTimeoutWithDelay("jquery=div.aui-list h5:contains('Favourite Filters')", TIMEOUT);
            String replaced = FAVOURITE_FILTERS_LIST_XPATH.replace("<no>", "" + (i + 1));
            assertThat.elementVisibleContainsText(replaced, filterName[i]);
            client.click(replaced, true);
            assertThat.elementVisibleContainsText(FILTERNAME_ELEMENT, filterName[i]);
        }
        //If there are more filters, check more filters link appears and works
        if (filterName.length > FILTERS_OVERLOAD_NO)
        {
            client.click(ISSUES_DROPDOWN);
            assertThat.visibleByTimeout(MORE_FILTERS_LIST_XPATH, TIMEOUT);
            client.click(MORE_FILTERS_LIST_XPATH, true);
            assertThat.textPresent("Manage Filters");
        }
    }

    private void checkNoIssue(String issueNo)
    {
        client.click(ISSUES_DROPDOWN);
        assertThat.elementNotPresent(issueNo);
        client.click(ISSUES_DROPDOWN);
    }

    private void checkNoFilter(String filterNo)
    {
        client.click(ISSUES_DROPDOWN);
        assertThat.elementNotPresent(filterNo);
        client.click(ISSUES_DROPDOWN);
    }
}