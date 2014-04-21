package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * A test for the Manage dashboards permissions collapsor
 *
 * @since v3.13
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestManageDashboardsPermissionsCollapsor extends JiraSeleniumTest
{
    private static final String DASHBOARD_ID_1 = "10010";
    private static final String PUBLIC_DASHBOARD = "Shared with all users";
    private static final String PRIVATE_DASHBOARD = "Private Dashboard";
    private static final String GROUP_JIRA_USERS = "Group: jira-users";
    private static final String GROUP_SHARE = "groupShare";
    private static final String SHARE_TYPE_SELECTOR = "share_type_selector";
    private static final String GROUP_JIRA_DEVELOPERS = "Group: jira-developers";
    private static final String GROUP_JIRA_ADMINISTRATORS = "Group: jira-administrators";
    private static final String PROJECT_HOMOSAPIEN = "Project: homosapien";
    private static final String PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE = "Project: monkey \nRole: Administrators";
    private static final String SHARE_ADD_GROUP = "share_add_group";
    private static final String LABEL_JIRA_DEVELOPERS = "label=jira-developers";
    private static final String LABEL_GROUP = "label=Group";
    private static final String LABEL_JIRA_USERS = "label=jira-users";
    private static final String ID_SHARE_LIST_COMPLETE = "id=share_list_complete_";
    private static final String ID_SHARE_LIST_SUMMARY = "id=share_list_summary_";
    private static final String SHARE_ADD_PROJECT = "share_add_project";
    private static final String PROJECT_SHARE_PROJECT = "projectShare-project";
    private static final String PROJECT_SHARE_ROLE = "projectShare-role";
    private static final String LABEL_MONKEY = "label=monkey";
    private static final String LABEL_ADMINISTRATORS = "label=Administrators";
    private static final String LABEL_PROJECT = "label=Project";


    public static Test suite()
    {
        return suiteFor(TestManageDashboardsPermissionsCollapsor.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("sharedfilters/AjaxSharedFilters.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD).gotoHome();
        getNavigator().gotoPage("/secure/AddPortalPage!default.jspa", true);
        client.type("portalPageName", PRIVATE_DASHBOARD);
        //remove any share permissions.
        while (client.isVisible("//img[@alt='Delete Share']")) {
            getNavigator().click("//img[@alt='Delete Share']");
        }
        getNavigator().clickAndWaitForPageLoad("submit");
    }

    public void testManageFiltersCollapser()
    {
        gotoManageFilters();
        assertThat.elementHasText("jquery=#pp_10010 .shareList", PRIVATE_DASHBOARD);
        assertThat.elementNotPresent("jquery=#share_list_complete_10010");
        assertThat.elementNotPresent("jquery=#share_list_summary_10010");

        gotoEditDashboard(DASHBOARD_ID_1);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_USERS);
        getNavigator().click(SHARE_ADD_GROUP);
        savePage();
        assertThat.elementHasText("jquery=#pp_10010 .shareList", GROUP_JIRA_USERS);
        assertThat.elementNotPresent("jquery=#share_list_complete_10010");
        assertThat.elementNotPresent("jquery=#share_list_summary_10010");

        gotoEditDashboard(DASHBOARD_ID_1);
        client.select(SHARE_TYPE_SELECTOR, LABEL_GROUP);
        client.select(GROUP_SHARE, LABEL_JIRA_DEVELOPERS);
        getNavigator().click(SHARE_ADD_GROUP);
        savePage();
        assertThat.elementHasText("jquery=#pp_10010 .shareList li:eq(0)", GROUP_JIRA_DEVELOPERS);
        assertThat.elementHasText("jquery=#pp_10010 .shareList li:eq(1)", GROUP_JIRA_USERS);
        assertThat.elementNotPresent("jquery=#share_list_complete_10010");
        assertThat.elementNotPresent("jquery=#share_list_summary_10010");


        gotoEditDashboard(DASHBOARD_ID_1);
        addSomePerms();
        savePage();
        assertThat.elementPresent(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);
        assertThat.elementPresent(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementVisible(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementNotVisible(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);

        assertThat.elementHasText("jquery=#share_list_summary_10010", "5 shares");
        assertThat.elementVisible("id=share_list_summary_10010");

        assertThat.elementNotVisible("id=share_list_complete_10010");
        assertThat.elementHasText("jquery=#share_list_complete_10010 li:eq(0)", GROUP_JIRA_ADMINISTRATORS);
        assertThat.elementHasText("jquery=#share_list_complete_10010 li:eq(1)", GROUP_JIRA_DEVELOPERS);
        assertThat.elementHasText("jquery=#share_list_complete_10010 li:eq(2)", GROUP_JIRA_USERS);
        assertThat.elementHasText("jquery=#share_list_complete_10010 li:eq(3)", PROJECT_HOMOSAPIEN);
        assertThat.elementHasText("jquery=#share_list_complete_10010 li:eq(4)", PROJECT_MONKEY_ROLE_ADMINISTRATORS_WITH_NEW_LINE);

        getNavigator().click(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementNotVisible(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementVisible(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);

        getNavigator().click(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);
        assertThat.elementVisible(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementNotVisible(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);

        getNavigator().click(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementNotVisible(ID_SHARE_LIST_SUMMARY + DASHBOARD_ID_1);
        assertThat.elementVisible(ID_SHARE_LIST_COMPLETE + DASHBOARD_ID_1);

    }

    private void gotoManageFilters()
    {
        getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa", true);

    }

    private void gotoEditDashboard(String pageId)
    {
        getNavigator().gotoPage("/secure/EditPortalPage!default.jspa?pageId=" + pageId, true);

    }


    private void savePage()
    {
        getNavigator().clickAndWaitForPageLoad("id=submit");
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
}