package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 *
 * @since v3.13
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestEditFilter extends JiraSeleniumTest
{
    private static final String FRED = "fred";
    private static final String ADMIN = "admin";
    private static final String EDIT_FILTER_PAGE = "/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=";
    private static final String UNABLE_TO_CHANGE_A_FILTER = "Unable to change a filter that you do not own";
    private static final String NO_CURRENT_REQUEST = "There is no current search request";
    private static final String PROJECT_MONKEY_ROLE_ADMINISTRATORS = "Project: monkey Role: Administrators";
    private static final String FILTER_NOT_SHARED = "Not shared";
    private static final String PRIVATE_FILTER = "Private filter";
    private static final String GROUP_JIRA_USERS = "Group: jira-users";
    private static final String GROUP_SHARE = "groupShare";
    private static final String SHARE_TYPE_SELECTOR = "share_type_selector";
    private static final String GROUP_JIRA_DEVELOPERS = "Group: jira-developers";
    private static final String GROUP_JIRA_ADMINISTRATORS = "Group: jira-administrators";
    private static final String PROJECT_HOMOSAPIEN = "Project: homosapien";
    private static final String PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE = "Project: monkey \nRole: Administrators";
    private static final String SHARE_ADD_GROUP = "share_add_group";
    private static final String SHARE_ADD_PROJECT = "share_add_project";
    private static final String PROJECT_SHARE_PROJECT = "projectShare-project";
    private static final String PROJECT_SHARE_ROLE = "projectShare-role";
    private static final String LABEL_JIRA_DEVELOPERS = "label=jira-developers";
    private static final String LABEL_GROUP = "label=Group";
    private static final String LABEL_JIRA_USERS = "label=jira-users";
    private static final String ID_PROJECT_SHARE_PROJECT = "id=projectShare-project";
    private static final String ID_PROJECT_SHARE_ROLE = "id=projectShare-role";
    private static final String ID_GROUP_SHARE = "id=groupShare";
    private static final String LABEL_PROJECT = "label=Project";
    private static final String ID_PROJECT_SHARE_ROLE_GROUP = "id=projectShare-role-group";
    private static final String PROJECT_MONKEY = "Project: monkey";
    private static final String ID_SHARE_DISPLAY_DIV = "id=share_display_div";
    private static final String ID_SHARE_DESCRIPTION = "id=share_type_description";
    private static final String LABEL_MONKEY = "label=monkey";
    private static final String LABEL_ADMINISTRATORS = "label=Administrators";
    private static final String LABEL_HOMOSAPIEN = "label=homosapien";
    private static final String FILTER_ID_10000 = "10000";
    private static final String COMPLETE_SPAN_PRE = "//span[@id='share_list_complete_";
    private static final String ID_SHARE_LIST_COMPLETE = "id=share_list_complete_";
    private static final String ID_SHARE_LIST_SUMMARY = "id=share_list_summary_";
    private static final String ROW_ID = "//tr[@id='mf_";
    private static final String SHARE_WITH_EVERYONE = "Share with everyone";
    private static final String SHARED_WITH_ALL_USERS = "Shared with all users";
    private static final String ENABLE_FAVOURITES = "Add this filter to your favourites";
    private static final String DISABLE_FAVOURITES = "Remove this filter from your favourites";

    public static Test suite()
    {
        return suiteFor(TestEditFilter.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("sharedfilters/AjaxSharedFilters.xml");
        safeLogin(ADMIN, ADMIN);
    }

    public void testNoGlobalPermission()
    {
        safeLogin(FRED, FRED);
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10005", true);
        assertThat.textPresent("Freds special filter");
        client.typeWithFullKeyEvents("filterName", "Renamed Filter for Fred");
        client.typeWithFullKeyEvents("filterDescription", "Description for Fred");
        saveFilter();
        assertThat.textPresent("Renamed Filter for Fred");
        assertThat.textPresent("Description for Fred");
        assertThat.textPresent("Filters are issue searches that have been saved for re-use.");

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10010", true);
        assertThat.textPresent("No Global Shares allowed but are here");
        assertThat.textPresent("You do not have permission to share. All shares are invalid.");
        assertThat.elementHasText("share_div_0_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("share_div_1_inner", PROJECT_MONKEY);

        saveFilter();
        assertThat.textPresent("No Global Shares allowed but are here");
        assertThat.textPresent("You do not have permission to share. All shares are invalid.");
        assertThat.elementHasText("share_div_0_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("share_div_1_inner", PROJECT_MONKEY);
        client.click("//img[@alt='Delete Share']");
        client.click("//img[@alt='Delete Share']");
        assertThat.textPresent(FILTER_NOT_SHARED);
        assertThat.elementNotPresent("share_div_0_inner");
        assertThat.elementNotPresent("share_div_1_inner");

        saveFilter();
        assertThat.textPresent("Filters are issue searches that have been saved for re-use.");

    }

    public void testGlobalPermissions()
    {
        // assert adding nothing does nothing
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.textPresent("Tester 1");
        assertThat.textPresent(FILTER_NOT_SHARED);
        saveFilter();
        assertThat.textPresent("Tester 1");
        assertThat.textPresent("Filters are issue searches that have been saved for re-use.");
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", PRIVATE_FILTER);

        // check global share add and remove
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.textPresent("Tester 1");
        assertThat.elementHasText("//div[@id='share_display_div']/div", FILTER_NOT_SHARED);
        assertThat.elementDoesNotContainText(ID_SHARE_DISPLAY_DIV, SHARED_WITH_ALL_USERS);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, SHARE_WITH_EVERYONE);
        getNavigator().click("share_add_global");
        assertThat.elementHasText(ID_SHARE_DISPLAY_DIV, SHARED_WITH_ALL_USERS);
        assertEquals(SHARE_WITH_EVERYONE, client.getAttribute("id=share_div_0_inner@title"));
        getNavigator().click("//img[@alt='Delete Share']");
        assertThat.elementHasText("//div[@id='share_display_div']/div", FILTER_NOT_SHARED);
        assertThat.elementDoesNotContainText(ID_SHARE_DISPLAY_DIV, SHARED_WITH_ALL_USERS);
        getNavigator().click("share_add_global");
        assertThat.elementHasText(ID_SHARE_DISPLAY_DIV, SHARED_WITH_ALL_USERS);
        assertEquals(SHARE_WITH_EVERYONE, client.getAttribute("id=share_div_1_inner@title"));
        saveFilter();
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", SHARED_WITH_ALL_USERS);
        assertEquals("Shared with everyone", client.getAttribute("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList li:eq(0)@title"));
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementPresent("share_div_0");
        assertEquals(SHARE_WITH_EVERYONE, client.getAttribute("id=share_div_0_inner@title"));
        assertThat.elementHasText(ID_SHARE_DISPLAY_DIV, SHARED_WITH_ALL_USERS);
        saveFilter();
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", SHARED_WITH_ALL_USERS);
        assertEquals("Shared with everyone", client.getAttribute("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList li:eq(0)@title"));
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        getNavigator().click("//img[@alt='Delete Share']");
        assertThat.textPresent(FILTER_NOT_SHARED);
        saveFilter();
        assertThat.textPresent("Tester 1");
        assertThat.textPresent("Filters are issue searches that have been saved for re-use.");
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", PRIVATE_FILTER);
        assertEquals("Not shared with any other users", client.getAttribute("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList li:eq(0)@title"));

        // check Global removes all others
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        addSomePerms();
        assertThat.elementHasText("id=share_div_0_inner", GROUP_JIRA_USERS);
        assertEquals("Share with all users in the 'jira-users' group", client.getAttribute("id=share_div_0_inner@title"));
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_ADMINISTRATORS);
        assertEquals("Share with all users in the 'jira-administrators' group", client.getAttribute("id=share_div_1_inner@title"));
        assertThat.elementHasText("id=share_div_2_inner", PROJECT_HOMOSAPIEN);
        assertEquals("Share with all users with permission to browse the 'homosapien' project", client.getAttribute("id=share_div_2_inner@title"));
        assertThat.elementHasText("id=share_div_3_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);
        assertEquals("Share with all users in the project role 'Administrators' for project 'monkey'", client.getAttribute("id=share_div_3_inner@title"));

        client.chooseCancelOnNextConfirmation();
        getNavigator().click("share_add_global");
        assertEquals("This operation will remove all current shares.", client.getConfirmation());

        assertThat.elementHasText("id=share_div_0_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_2_inner", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("id=share_div_3_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);

        client.chooseOkOnNextConfirmation();
        client.select(SHARE_TYPE_SELECTOR, "label=Everyone");
        getNavigator().click("share_add_global");
        assertEquals("This operation will remove all current shares.", client.getConfirmation());

        assertThat.elementHasText("id=share_div_5_inner", SHARED_WITH_ALL_USERS);
        client.select(SHARE_TYPE_SELECTOR, "label=Everyone");
        getNavigator().click("share_add_global");
        assertThat.elementNotPresent("id=share_div_6_inner");

        saveFilter(false);
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", SHARED_WITH_ALL_USERS);

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        addSomePerms();
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("id=share_div_2_inner", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_3_inner", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("id=share_div_4_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);

        saveFilter();
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);

        client.chooseCancelOnNextConfirmation();
        client.select(SHARE_TYPE_SELECTOR, "label=Everyone");
        getNavigator().click("share_add_global");
        assertEquals("This operation will remove all current shares.", client.getConfirmation());

        //these will be ordered now
        assertThat.elementHasText("id=share_div_0_inner", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("id=share_div_2_inner", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("id=share_div_3_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);

        client.chooseOkOnNextConfirmation();
        client.select(SHARE_TYPE_SELECTOR, "label=Everyone");
        getNavigator().click("share_add_global");
        assertEquals("This operation will remove all current shares.", client.getConfirmation());

        assertThat.elementHasText("id=share_div_5_inner", SHARED_WITH_ALL_USERS);
        saveFilter();
        assertThat.elementHasText("jquery=#mf_" + FILTER_ID_10000 + " ul.shareList", SHARED_WITH_ALL_USERS);

    }

    public void testInvalidPermissions()
    {
        safeLogin(FRED, FRED);
        getNavigator().gotoPage("/secure/AddFavourite.jspa?entityId=10001&entityType=SearchRequest", false);
        getNavigator().gotoPage("/secure/AddFavourite.jspa?entityId=10003&entityType=SearchRequest", false);
        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementPresent("id=mf_10005");
        assertThat.elementPresent("id=mf_10010");
        assertThat.elementPresent("id=mf_10001");
        assertThat.elementPresent("id=mf_10003");
        assertThat.elementNotPresent("id=mf_" + FILTER_ID_10000);
        assertThat.elementNotPresent("id=mf_10002");
        assertThat.elementNotPresent("id=mf_10004");

        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("/secure/project/DeletePermission!default.jspa?id=10005&schemeId=0", true);
        getNavigator().clickAndWaitForPageLoad("id=delete_submit");

        safeLogin(FRED, FRED);
        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementPresent("id=mf_10005");
        assertThat.elementPresent("id=mf_10010");
        assertThat.elementPresent("id=mf_10001");
        assertThat.elementNotPresent("id=mf_" + FILTER_ID_10000);
        assertThat.elementNotPresent("id=mf_10002");
        assertThat.elementNotPresent("id=mf_10003");
        assertThat.elementNotPresent("id=mf_10004");

        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementPresent("id=mf_" + FILTER_ID_10000);
        assertThat.elementPresent("id=mf_10001");
        assertThat.elementPresent("id=mf_10002");
        assertThat.elementPresent("id=mf_10003");
        assertThat.elementPresent("id=mf_10004");

        assertThat.elementHasText("jquery=#mf_10003 ul.shareList", PROJECT_HOMOSAPIEN);
        assertEquals("Shared with all users with permission to browse the 'homosapien' project", client.getAttribute("jquery=#mf_10003 ul.shareList li:eq(0)@title"));
        assertThat.elementHasText("jquery=#mf_10004 ul.shareList", PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE);
        assertEquals("Shared with all users in the project role 'Administrators' for project 'monkey'", client.getAttribute("jquery=#mf_10004 ul.shareList li:eq(0)@title"));

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10004", true);
        assertThat.elementHasText("//div[@class='error']", "You can not share with a role in project 'monkey' as you do not have browse permission");

        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10002", true);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_DEVELOPERS);
        getNavigator().click(SHARE_ADD_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        saveFilter();
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(0)", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(1)", GROUP_JIRA_DEVELOPERS);
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(2)", GROUP_JIRA_USERS);

        safeLogin(FRED, FRED);
        getNavigator().gotoPage("/secure/AddFavourite.jspa?entityId=10002&entityType=SearchRequest", false);
        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementPresent("id=mf_10002");
        assertThat.elementHasText("jquery=#mf_10002 ul.shareList li:eq(0)", GROUP_JIRA_USERS);
        assertThat.elementNotPresent("jquery=#mf_10002 ul.shareList li:eq(1)");
        assertThat.elementNotPresent("jquery=#mf_10002 ul.shareList li:eq(2)");

        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("/secure/admin/user/EditUserGroups!default.jspa?name=admin", true);
        client.selectOption("groupsToLeave", "jira-developers");
        getNavigator().clickAndWaitForPageLoad("leave");
        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(0)", GROUP_JIRA_ADMINISTRATORS);
        assertEquals("Shared with all users in the 'jira-administrators' group", client.getAttribute("jquery=#share_list_complete_10002 li:eq(0)@title"));
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(1)", GROUP_JIRA_DEVELOPERS);
        assertEquals("Shared with all users in the 'jira-developers' group", client.getAttribute("jquery=#share_list_complete_10002 li:eq(1)@title"));
        assertThat.elementHasText("jquery=#share_list_complete_10002 li:eq(2)", GROUP_JIRA_USERS);
        assertEquals("Shared with all users in the 'jira-users' group", client.getAttribute("jquery=#share_list_complete_10002 li:eq(2)@title"));


    }

    public void testGroupPermissions()
    {
        // assert adding nothing does nothing
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementNotVisible(ID_GROUP_SHARE);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_PROJECT);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_ROLE);

        assertThat.elementHasText("//option[@value='group']", "Group");
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        assertThat.elementVisible(ID_GROUP_SHARE);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_PROJECT);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_ROLE);
        assertThat.elementPresent("//option[@value='jira-administrators']");
        assertThat.elementPresent("//option[@value='jira-developers']");
        assertThat.elementPresent("//option[@value='jira-users']");

        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        client.select(GROUP_SHARE, "label=jira-administrators");
        getNavigator().click(SHARE_ADD_GROUP);

        assertThat.elementHasText("id=share_div_0_inner", GROUP_JIRA_USERS);
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_ADMINISTRATORS);
        saveFilter();

        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(1)", GROUP_JIRA_USERS);

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementHasText("id=share_div_0_inner", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_1_inner", GROUP_JIRA_USERS);

        getNavigator().click("//div[@id='share_div_1_inner']/img[2]");
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", GROUP_JIRA_ADMINISTRATORS);

        // assert only groups you are a member of are shown
        safeLogin("somegroups", "somegroups");
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10020", true);

        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        assertThat.elementVisible(ID_GROUP_SHARE);
        assertThat.elementNotPresent("//option[@value='jira-administrators']");
        assertThat.elementPresent("//option[@value='jira-developers']");
        assertThat.elementPresent("//option[@value='jira-users']");

        //assert you can't add same group twice
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10020 ul.shareList li:eq(0)", GROUP_JIRA_USERS);
        assertThat.elementNotPresent("jquery=#mf_10000 ul.shareList li:eq(1)");

    }

    public void testProjectPermissions()
    {
        // assert drop down contain correct values and display at the correct time

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementNotVisible(ID_GROUP_SHARE);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_PROJECT);
        assertThat.elementNotVisible(ID_PROJECT_SHARE_ROLE);

        assertThat.elementHasText("//option[@value='project']", "Project");
        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        assertThat.elementVisible(ID_PROJECT_SHARE_PROJECT);
        assertThat.elementVisible(ID_PROJECT_SHARE_ROLE);
        assertThat.elementNotVisible(ID_GROUP_SHARE);
        assertThat.elementPresent("//select[@id='projectShare-project']/option[@value='" + FILTER_ID_10000 + "']");
        assertThat.elementPresent("//select[@id='projectShare-project']/option[@value='10001']");
        assertThat.elementNotPresent("//select[@id='projectShare-project']/option[@value='10010']");
        assertThat.elementPresent("//select[@id='projectShare-role']/option[@value='']");
        assertThat.elementNotPresent(ID_PROJECT_SHARE_ROLE_GROUP);
        assertThat.elementNotPresent("//select[@id='projectShare-role-group']/option[@value='10010']");
        assertThat.elementNotPresent("//select[@id='projectShare-role-group']/option[@value='10002']");
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        assertThat.elementPresent(ID_PROJECT_SHARE_ROLE_GROUP);
        assertThat.elementPresent("//optgroup[@id='projectShare-role-group']/option[@value='10010']");
        assertThat.elementPresent("//optgroup[@id='projectShare-role-group']/option[@value='10002']");

        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_ROLE, LABEL_ADMINISTRATORS);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_HOMOSAPIEN);
        getNavigator().click(SHARE_ADD_PROJECT);


        assertThat.elementHasText("id=share_div_0_inner", PROJECT_MONKEY);
        assertThat.elementHasText("id=share_div_1_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_2_inner", PROJECT_HOMOSAPIEN);
        client.click("//img[@alt='Delete Share']");
        saveFilter();


        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(1)", PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE);

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementHasText("id=share_div_0_inner", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("id=share_div_1_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);

        getNavigator().click("//div[@id='share_div_0_inner']/img[2]");
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE);

        // assert can't add twice
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        client.select(PROJECT_SHARE_ROLE, LABEL_ADMINISTRATORS);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_HOMOSAPIEN);
        getNavigator().click(SHARE_ADD_PROJECT);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        getNavigator().click(SHARE_ADD_PROJECT);

        assertThat.elementHasText("id=share_div_0_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);
        assertThat.elementHasText("id=share_div_1_inner", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("id=share_div_2_inner", PROJECT_MONKEY);

        // Lets check when there are no projects
        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        getNavigator().gotoAdmin();
        getNavigator().gotoPage("/secure/project/DeleteProject!default.jspa?pid=10010&returnUrl=ViewProjects.jspa", true);
        getNavigator().clickAndWaitForPageLoad("Delete");
        getNavigator().gotoPage("/secure/project/DeleteProject!default.jspa?pid=10000&returnUrl=ViewProjects.jspa", true);
        getNavigator().clickAndWaitForPageLoad("Delete");
        getNavigator().gotoPage("/secure/project/DeleteProject!default.jspa?pid=10001&returnUrl=ViewProjects.jspa", true);
        getNavigator().clickAndWaitForPageLoad("Delete");

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10004", true);
        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        assertThat.visibleByTimeout("//span[@id='share_project']/span");
        assertThat.elementHasText("//span[@id='share_project']/span", "You do not have permission to browse any projects.");
        assertThat.elementNotPresent("id=share_add_project");
        assertThat.elementNotPresent("id=shareProject-project");
        assertThat.elementNotPresent("id=shareProject-role");
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        assertThat.elementNotVisible("//span[@id='share_project']/span");

    }

    public void testFavourite()
    {
        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.textPresent(DISABLE_FAVOURITES);
        assertThat.textNotPresent(ENABLE_FAVOURITES);
        saveFilter();

        getNavigator().gotoPage("/secure/ManageFilters.jspa?filterView=favourites", true);
        assertThat.elementPresent("id=mf_favourites");
        assertThat.elementContainsText("id=fav_a_mf_favourites_SearchRequest_" + FILTER_ID_10000, DISABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_mf_favourites_SearchRequest_" + FILTER_ID_10000, ENABLE_FAVOURITES);
        getNavigator().click("id=fav_a_mf_favourites_SearchRequest_" + FILTER_ID_10000);
        waitFor(2000);
        assertThat.elementDoesNotContainText("id=fav_a_mf_favourites_SearchRequest_" + FILTER_ID_10000, DISABLE_FAVOURITES);
        assertThat.elementContainsText("id=fav_a_mf_favourites_SearchRequest_" + FILTER_ID_10000, ENABLE_FAVOURITES);

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementContainsText("id=fav_a_favourite", ENABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_favourite", DISABLE_FAVOURITES);
        saveFilter();

        assertThat.elementNotPresent("id=mf_" + FILTER_ID_10000);  //wont be present on fav tab

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementContainsText("id=fav_a_favourite", ENABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_favourite", DISABLE_FAVOURITES);
        getNavigator().click("id=fav_a_favourite");
        waitFor(2000);
        assertThat.elementContainsText("id=fav_a_favourite", DISABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_favourite", ENABLE_FAVOURITES);
        saveFilter();
        assertThat.elementPresent("id=mf_" + FILTER_ID_10000);

        getNavigator().gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=" + FILTER_ID_10000, true);
        assertThat.elementContainsText("id=fav_a_favourite", DISABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_favourite", ENABLE_FAVOURITES);
        getNavigator().click("id=fav_a_favourite");
        waitFor(2000);
        assertThat.elementContainsText("id=fav_a_favourite", ENABLE_FAVOURITES);
        assertThat.elementDoesNotContainText("id=fav_a_favourite", DISABLE_FAVOURITES);
        saveFilter();
        assertThat.elementNotPresent("id=mf_" + FILTER_ID_10000);
    }

    public void testXSS()
    {
        getNavigator().gotoPage("/secure/admin/AddProject!default.jspa", true);
        client.type("name", "<b>XSS Proj</b>");
        client.type("key", "XSS");
        client.type("lead", ADMIN);
        getNavigator().clickAndWaitForPageLoad("Add");

        getNavigator().gotoPage("/secure/project/ViewProjectRoles.jspa", true);
        client.type("name", "<b>XXS Role</b>");
        getNavigator().click("role_submit");

        getNavigator().gotoPage("/secure/project/GroupRoleActorAction.jspa?projectRoleId=10020&projectId=10020", true);
        client.type("groupNames", "jira-users");
		getNavigator().clickAndWaitForPageLoad("add");

        gotoEditFilter(FILTER_ID_10000);

        assertThat.elementHasText("//select[@id='projectShare-project']/option[1]", "<b>XSS Proj</b>");
        assertThat.elementHasText("//optgroup[@id='projectShare-role-group']/option[1]", "<b>XXS Role</b>");

        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, "label=<b>XSS Proj</b>");
        client.select(PROJECT_SHARE_ROLE, "label=<b>XXS Role</b>");
        getNavigator().click(SHARE_ADD_PROJECT);
        assertThat.elementHasText("id=share_div_0_inner", "Project: <b>XSS Proj</b> Role: <b>XXS Role</b>");
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", "Project: <b>XSS Proj</b> \nRole: <b>XXS Role</b>");

    }

    private void gotoEditFilter(final String filterId)
    {
        getNavigator().gotoPage(EDIT_FILTER_PAGE + filterId, true);
    }

    public void testNotOwner()
    {
        gotoEditFilter("10005");
        assertThat.textPresent("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.");
        assertThat.elementNotPresent("id=Save");
        getNavigator().gotoPage("/secure/EditFilter.jspa?filterId=10005&filterName=Nicks", true);
        assertThat.elementHasText("jquery=.aui-message.error", "The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.");

        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        gotoEditFilter("10010");
        assertThat.elementHasText("jquery=.aui-message.error", UNABLE_TO_CHANGE_A_FILTER);
        assertThat.elementNotPresent("id=Save");
        getNavigator().gotoPage("/secure/EditFilter.jspa?filterId=10010&filterName=Nicks", true);
        assertThat.elementHasText("jquery=.aui-message.error", UNABLE_TO_CHANGE_A_FILTER);

    }

    public void testNoFilterInSession()
    {
        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("/secure/EditFilter!default.jspa", true);
        assertThat.elementHasText("jquery=.aui-message.error", NO_CURRENT_REQUEST);
        getNavigator().gotoPage("/secure/EditFilter.jspa", true);
        assertThat.elementHasText("jquery=.aui-message.error", NO_CURRENT_REQUEST);
    }

    public void testShareInSessionWithNotExistantShares()
    {
        gotoEditFilter("10004");
        assertThat.elementHasText("id=share_div_0_inner", PROJECT_MONKEY_ROLE_ADMINISTRATORS);
        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        getNavigator().gotoPage("/secure/project/DeleteProjectRole!default.jspa?id=10002", true);
        getNavigator().clickAndWaitForPageLoad("id=delete_submit");
        getNavigator().gotoPage("/secure/EditFilter!default.jspa", true);
        assertThat.elementNotPresent("id=share_div_1_inner");
        assertThat.textPresent(FILTER_NOT_SHARED);
    }

    public void testManageFiltersCollapser()
    {
        gotoManageFilters();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", PRIVATE_FILTER);
        assertThat.elementNotPresent(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementNotPresent(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);

        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", GROUP_JIRA_USERS);
        assertThat.elementNotPresent(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementNotPresent(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);

        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_DEVELOPERS);
        getNavigator().click(SHARE_ADD_GROUP);
        saveFilter();
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(0)", GROUP_JIRA_DEVELOPERS);
        assertThat.elementHasText("jquery=#mf_10000 ul.shareList li:eq(1)", GROUP_JIRA_USERS);
        assertThat.elementNotPresent(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementNotPresent(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);


        gotoEditFilter(FILTER_ID_10000);
        addSomePerms();
        saveFilter();
        assertThat.elementPresent(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementPresent(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementVisible(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementNotVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);

        assertThat.elementHasText("jquery=#share_list_summary_10000", "5 shares");
        assertThat.elementVisible("id=share_list_summary_" + FILTER_ID_10000);

        assertThat.elementNotVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementHasText("jquery=#share_list_complete_10000 li:eq(0)", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("jquery=#share_list_complete_10000 li:eq(1)", GROUP_JIRA_DEVELOPERS);
        assertThat.elementHasText("jquery=#share_list_complete_10000 li:eq(2)", GROUP_JIRA_USERS);
        assertThat.elementHasText("jquery=#share_list_complete_10000 li:eq(3)", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("jquery=#share_list_complete_10000 li:eq(4)", PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE);

        getNavigator().click(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementNotVisible(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);

        assertThat.elementNotVisible("//span[@id='share_list_summary_" + FILTER_ID_10000 + "']/table/tbody/tr/td[2]");

        assertThat.elementVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);

        getNavigator().click(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);
        assertThat.elementVisible(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementNotVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);

        getNavigator().click(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementNotVisible(ID_SHARE_LIST_SUMMARY + FILTER_ID_10000);
        assertThat.elementVisible(ID_SHARE_LIST_COMPLETE + FILTER_ID_10000);

    }

    public void testDescriptions()
    {
        gotoEditFilter(FILTER_ID_10000);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, SHARE_WITH_EVERYONE);
        getNavigator().click("share_add_global");
        assertEquals(SHARE_WITH_EVERYONE, client.getAttribute("id=share_div_0_inner@title"));

        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, "Share with all users in the 'jira-administrators' group");
        getNavigator().click(SHARE_ADD_GROUP);
        assertEquals("Share with all users in the 'jira-administrators' group", client.getAttribute("id=share_div_1_inner@title"));

        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, "Share with all users in the 'jira-users' group");
        getNavigator().click(SHARE_ADD_GROUP);
        assertEquals("Share with all users in the 'jira-users' group", client.getAttribute("id=share_div_2_inner@title"));

        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, "Share with all users with permission to browse the 'homosapien' project");
        getNavigator().click(SHARE_ADD_PROJECT);
        assertEquals("Share with all users with permission to browse the 'homosapien' project", client.getAttribute("id=share_div_3_inner@title"));

        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, "Share with all users with permission to browse the 'monkey' project");
        client.select(PROJECT_SHARE_ROLE, LABEL_ADMINISTRATORS);
        assertThat.elementHasText(ID_SHARE_DESCRIPTION, "Share with all users in the project role 'Administrators' for project 'monkey'");
        getNavigator().click(SHARE_ADD_PROJECT);
        assertEquals("Share with all users in the project role 'Administrators' for project 'monkey'", client.getAttribute("id=share_div_4_inner@title"));

        saveFilter();

        assertEquals("Shared with all users in the 'jira-administrators' group", client.getAttribute("jquery=#share_list_complete_10000 li:eq(0)@title"));
        assertEquals("Shared with all users in the 'jira-users' group", client.getAttribute("jquery=#share_list_complete_10000 li:eq(1)@title"));
        assertEquals("Shared with all users with permission to browse the 'homosapien' project", client.getAttribute("jquery=#share_list_complete_10000 li:eq(2)@title"));
        assertEquals("Shared with all users in the project role 'Administrators' for project 'monkey'", client.getAttribute("jquery=#share_list_complete_10000 li:eq(3)@title"));

        gotoEditFilter(FILTER_ID_10000);
        assertEquals("Share with all users in the 'jira-administrators' group", client.getAttribute("id=share_div_0_inner@title"));
        assertEquals("Share with all users in the 'jira-users' group", client.getAttribute("id=share_div_1_inner@title"));
        assertEquals("Share with all users with permission to browse the 'homosapien' project", client.getAttribute("id=share_div_2_inner@title"));
        assertEquals("Share with all users in the project role 'Administrators' for project 'monkey'", client.getAttribute("id=share_div_3_inner@title"));


    }

    public void testDirtyFlag()
    {
        gotoEditFilter(FILTER_ID_10000);
        saveFilter();
        assertThat.elementPresent("id=mf_favourites");

        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
//        saveFilter();
        client.chooseOkOnNextConfirmation();
        client.click("id=filter-edit-submit");
        assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        assertThat.elementPresent("id=mf_favourites");

        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.chooseCancelOnNextConfirmation();
        getNavigator().click("id=filter-edit-submit");
        assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());

        assertThat.elementPresent("id=share_display_component");

        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        getNavigator().click(SHARE_ADD_GROUP);
        client.select(GROUP_SHARE, "label=jira-users");
        client.chooseCancelOnNextConfirmation();
        getNavigator().click("id=filter-edit-submit");
        assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());
        assertThat.elementPresent("id=share_display_component");

        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        gotoEditFilter(FILTER_ID_10000);
        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        client.chooseCancelOnNextConfirmation();
        getNavigator().click("id=filter-edit-submit");
        assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());
        assertThat.elementPresent("id=share_display_component");

        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_ROLE, LABEL_ADMINISTRATORS);
        client.chooseCancelOnNextConfirmation();
        getNavigator().click("id=filter-edit-submit");
        assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());
        assertThat.elementPresent("id=share_display_component");

        getNavigator().click(SHARE_ADD_PROJECT);
        saveFilter();
        assertThat.elementPresent("id=mf_favourites");

    }

    private void gotoManageFilters()
    {
        getNavigator().gotoPage("/secure/ManageFilters.jspa", true);
    }

    private void saveFilter()
    {
        saveFilter(false);
    }

    private void saveFilter(final boolean confirm)
    {
        if (confirm)
        {
            getSeleniumClient().chooseOkOnNextConfirmation();
        }
        getNavigator().clickAndWaitForPageLoad("id=filter-edit-submit");
        if (confirm)
        {
            assertEquals("You have not added the share that you selected. Are you sure that you want to continue?", client.getConfirmation());
            getSeleniumClient().chooseOkOnNextConfirmation();
        }
    }

    private void addSomePerms()
    {
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        client.select(GROUP_SHARE, "label=jira-administrators");
        getNavigator().click(SHARE_ADD_GROUP);
        client.select(SHARE_TYPE_SELECTOR, LABEL_PROJECT);
        getNavigator().click(SHARE_ADD_PROJECT);
        client.select(PROJECT_SHARE_PROJECT, LABEL_MONKEY);
        client.select(PROJECT_SHARE_ROLE, LABEL_ADMINISTRATORS);
        getNavigator().click(SHARE_ADD_PROJECT);
    }

    private void safeLogin(final String username, final String password)
    {
        //we have to logout to ensure that the session is clean for the next test. The tabs on managefilters is
        //remembered.
        getNavigator().logout(getXsrfToken());
        getNavigator().login(username, password);
    }
}
