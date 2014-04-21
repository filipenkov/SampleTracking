package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * This test asserts that the links in the admin menu is available/unavailable under various conditions.
 *
 * To test just the admin menu, this test case only uses users with permission to goto the administration section
 *
 * To keep it simple, the test xml file only changed the group settings (and not any roles)
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestAdminMenuWebFragment extends JIRAWebTest
{
    public TestAdminMenuWebFragment(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWebFragment.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        restoreBlankInstance();
        super.tearDown();
    }

    public void testBackToProjectConfigLink()
    {
        login(ADMIN_USERNAME);
        navigation.gotoAdminSection("view_projects");

        // put the project into the session
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        clickLinkWithText(PROJECT_HOMOSAP);
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");

        // put the issue type tab in the session
        clickLink("view_project_issuetypes_tab");
        assertions.assertNodeByIdExists("project-config-panel-issuetypes");
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");
        assertions.assertNodeByIdHasText("project-config-issuetype-scheme-name", "Default Issue Type Scheme");

        // asert the back link is present on pages with titles
        clickLink("project-config-issuetype-scheme-change");

        assertions.assertNodeByIdExists("proj-config-return-link");
        assertions.assertNodeByIdHasText("back-lnk", "Back to project: homosapien");
        clickLink("back-lnk");

        // assert link went back to correct tab
        assertions.assertNodeByIdExists("project-config-panel-issuetypes");
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");
        assertions.assertNodeByIdHasText("project-config-issuetype-scheme-name", "Default Issue Type Scheme");

        // assert back link present on pages with titles.
        clickLink("workflows");
        assertions.assertNodeByIdExists("proj-config-return-link");
        assertions.assertNodeByIdHasText("back-lnk", "Back to project: homosapien");
        clickLink("back-lnk");

        // assert link went back to correct tab
        assertions.assertNodeByIdExists("project-config-panel-issuetypes");
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");
        assertions.assertNodeByIdHasText("project-config-issuetype-scheme-name", "Default Issue Type Scheme");


        // clear the project from session
        clickLink("view_projects");
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");

        // make sure it doeesn't appear on other pages.
        clickLink("workflows");
        assertions.assertNodeByIdDoesNotExist("proj-config-return-link");

    }

    public void testAdminMenuWebFragment()
    {
        _testSystemAdminCanSeeAllAdminSections();
        _testProjectAdminCanSeeProjectSectionOnly();
        _testOtherUsersCannotSeeAdminSections();
    }

    /**
     * Test that all administrative sections are AVAILABLE when the user IS a system administrator
     */
    public void _testSystemAdminCanSeeAllAdminSections()
    {
        //assert system administrators can see all the sections
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoAdmin();
        assertAdminLinksAreVisible();
    }

    /**
     * Test that the system administrator can only see the project section
     */
    public void _testProjectAdminCanSeeProjectSectionOnly()
    {
        //assert project administrator can only see the project section
        login("project_admin", "project_admin");
        navigation.gotoAdmin();
        assertAdminLinksAreNotVisible();
    }

    /**
     * Test that all other users (including not logged in) can only see project section with restriction message
     */
    public void _testOtherUsersCannotSeeAdminSections()
    {
        //users should not be able to view the Admin link - so go there directly
        login("user", "user");
        gotoPage("/secure/project/ViewProjects.jspa");
        assertTextPresent("You do not have the permissions to administer any projects, or there are none created.");
        assertAdminLinksAreNotVisible();

        //non-logged-in users should not be able to view the Admin link - so go there directly
        logout();
        gotoPage("/secure/project/ViewProjects.jspa");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-message.warning"),"If you log in or sign up for an account, you might be able to see more here.");
        assertions.assertNodeByIdDoesNotExist("adminMenu"); // Users that are not logged in shouldn't see any admin menu
    }

    public void assertAdminLinksAreVisible()
    {
        //Now assert all the links are available
        assertLinkPresent("view_projects");//always visible
        assertLinkPresent("user_browser");
        assertLinkPresent("group_browser");
        assertLinkPresent("project_role_browser");
        assertLinkPresent("attachments");
        assertLinkPresent("cvs_modules");
        assertLinkPresent("edit_default_dashboard");
        assertLinkPresent("general_configuration");
        assertLinkPresent("global_permissions");
        assertLinkPresent("linking");
        assertLinkPresent("lookandfeel");
        assertLinkPresent("outgoing_mail");
        assertLinkPresent("incoming_mail");
        assertLinkPresent("timetracking");
        assertLinkPresent("user_defaults");
        assertLinkPresent("permission_schemes");
        assertLinkPresent("scheme_tools");
        assertLinkPresent("view_custom_fields");
        assertLinkPresent("field_configuration");
        assertLinkPresent("issue_field_columns");
        assertLinkPresent("field_screens");
        assertLinkPresent("issue_types");
        assertLinkPresent("priorities");
        assertLinkPresent("resolutions");
        assertLinkPresent("statuses");
        assertLinkPresent("backup_data");
        assertLinkPresent("restore_data");
        assertLinkPresent("jelly_runner");
        assertLinkPresent("send_email");
        assertLinkPresent("edit_announcement");
        assertLinkPresent("indexing");
        assertLinkNotPresent("issue_caching"); //this is never displayed
        assertLinkPresent("integrity_checker");
        assertLinkPresent("license_details");
        assertLinkPresent("listeners");
        assertLinkPresent("logging_profiling");
        assertLinkPresent("mail_queue");
        assertLinkPresent("upm-admin-link");
        assertLinkPresent("scheduler_details");
        assertLinkPresent("services");
        assertLinkPresent("system_info");

        assertLinkPresent("view_categories");
        assertLinkPresent("security_schemes");
        assertLinkPresent("workflow_schemes");
        assertLinkPresent("field_configuration");
        assertLinkPresent("issue_fields");
        assertLinkPresent("issue_type_screen_scheme");

        assertLinkPresent("eventtypes");
        assertLinkPresent("subtasks");
        assertLinkPresent("workflows");
        assertLinkPresent("field_screen_scheme");
    }

    public void assertAdminLinksAreNotVisible()
    {
        //Now assert all the links are available
        assertLinkPresent("view_projects");//always visible
        assertLinkNotPresent("user_browser");
        assertLinkNotPresent("group_browser");
        assertLinkNotPresent("project_role_browser");
        assertLinkNotPresent("attachments");
        assertLinkNotPresent("cvs_modules");
        assertLinkNotPresent("edit_default_dashboard");
        assertLinkNotPresent("general_configuration");
        assertLinkNotPresent("global_permissions");
        assertLinkNotPresent("linking");
        assertLinkNotPresent("lookandfeel");
        assertLinkNotPresent("outgoing_mail");
        assertLinkNotPresent("incoming_mail");
        assertLinkNotPresent("timetracking");
        assertLinkNotPresent("user_defaults");
        assertLinkNotPresent("permission_schemes");
        assertLinkNotPresent("scheme_tools");
        assertLinkNotPresent("view_custom_fields");
        assertLinkNotPresent("field_configuration");
        assertLinkNotPresent("issue_field_columns");
        assertLinkNotPresent("field_screens");
        assertLinkNotPresent("issue_types");
        assertLinkNotPresent("priorities");
        assertLinkNotPresent("resolutions");
        assertLinkNotPresent("statuses");
        assertLinkNotPresent("backup_data");
        assertLinkNotPresent("restore_data");
        assertLinkNotPresent("jelly_runner");
        assertLinkNotPresent("send_email");
        assertLinkNotPresent("edit_announcement");
        assertLinkNotPresent("indexing");
        assertLinkNotPresent("issue_caching"); //this is never displayed
        assertLinkNotPresent("integrity_checker");
        assertLinkNotPresent("license_details");
        assertLinkNotPresent("listeners");
        assertLinkNotPresent("logging_profiling");
        assertLinkNotPresent("mail_queue");
        assertLinkNotPresent("plugins");
        assertLinkNotPresent("scheduler_details");
        assertLinkNotPresent("services");
        assertLinkNotPresent("system_info");

        assertLinkNotPresent("view_categories");
        assertLinkNotPresent("security_schemes");
        assertLinkNotPresent("workflow_schemes");
        assertLinkNotPresent("field_configuration");
        assertLinkNotPresent("issue_fields");
        assertLinkNotPresent("issue_type_screen_scheme");

        assertLinkNotPresent("eventtypes");
        assertLinkNotPresent("subtasks");
        assertLinkNotPresent("workflows");
        assertLinkNotPresent("field_screen_scheme");
    }
}
