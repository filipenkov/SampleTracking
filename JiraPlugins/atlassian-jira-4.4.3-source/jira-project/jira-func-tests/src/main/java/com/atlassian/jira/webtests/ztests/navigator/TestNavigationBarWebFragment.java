package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.log4j.Logger;

/**
 * Simple test case that checks the links on the top system navigation bar
 * is visible with correct permissions.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestNavigationBarWebFragment extends JIRAWebTest
{
    public static final Logger log = Logger.getLogger(TestNavigationBarWebFragment.class);

    public TestNavigationBarWebFragment(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestNavigationBarWebFragment.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        restoreBlankInstance();
        super.tearDown();
    }

    public void testNavigationBarWebFragment()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertLinkPresent("home_link"); //always visible

        _checkBrowseAndFindIssueLinksVisiblity();
        _checkCreateIssueLinkVisiblity();
        _checkAdminLinkVisiblityToProjectAdmin();
        _checkAdminLinkVisiblityToSystemAdmin();
    }

    public void _checkBrowseAndFindIssueLinksVisiblity()
    {
        //check we have the browse/find link to start with.
        assertLinkPresent("browse_link");
        assertLinkPresent("find_link");

        //remove the browse project permission and assert links are not available
        removeBrowsePermission();
        assertLinkNotPresent("browse_link");
        assertLinkNotPresent("find_link");

        //add the browse permission back and check its displayed correctly.
        addBrowsePermission();
        getNavigation().gotoDashboard();
        assertLinkPresent("find_link");
        assertLinkPresent("browse_link");
        assertLinkPresentWithText("Projects");

        //select a project and assert new link text
        gotoPage("/plugins/servlet/project-config/HSP/summary");
        navigation.gotoDashboard();
        assertLinkPresent("browse_link");
        assertLinkPresentWithText("Projects");
    }

    public void _checkCreateIssueLinkVisiblity()
    {
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        //check we have the create issue link to start with.
        assertLinkPresent("create_link");

        //remove the permission and assert link is not present
        removeCreatePermission();
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        assertLinkNotPresent("create_link");

        //readd the permission and assert its back
        addCreatePermission();
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        assertLinkPresent("create_link");
    }

    public void _checkAdminLinkVisiblityToProjectAdmin()
    {
        login("project_admin","project_admin");
        assertLinkPresent("admin_link");

        //login as admin and remove the project admin permission from user: project_admin
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        removeProjectAdminPermission();
        navigation.gotoDashboard();
        assertLinkPresent("admin_link");

        //log back in as the project_admin, and assert admin link is not available
        logout();//must explicitly logout to invalidate session (SessionKeys.USER_PROJECT_ADMIN)
        login("project_admin","project_admin");
        assertLinkNotPresent("admin_link");

        //login as admin and add the project admin permission for user: project_admin
        logout();//not neccessary but safe to logout here also
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        addProjectAdminPermission();
        navigation.gotoDashboard();
        assertLinkPresent("admin_link");

        //log back in as project_admin and assert link is back
        logout();//must explicitly logout to create new session (SessionKeys.USER_PROJECT_ADMIN)
        login("project_admin","project_admin");
        assertLinkPresent("admin_link");
    }

    public void _checkAdminLinkVisiblityToSystemAdmin()
    {
        login("system_admin","system_admin");
        assertLinkPresent("admin_link");

        //login as admin and remove the system_admin from the administrators group
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        removeUserFromGroup("system_admin", "jira-administrators");
        navigation.gotoDashboard();
        assertLinkPresent("admin_link");

        //log back in as the system_admin, and assert admin link is not available
        logout();//must explicitly logout to invalidate session (SessionKeys.USER_PROJECT_ADMIN)
        login("system_admin","system_admin");
        assertLinkNotPresent("admin_link");

        //login as admin and add the system_admin permission for user: system_admin
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        addUserToGroup("system_admin", "jira-administrators");
        navigation.gotoDashboard();
        assertLinkPresent("admin_link");

        //log back in as project_admin and assert link is back
        login("system_admin","system_admin");
        assertLinkPresent("admin_link");
    }

    //--------------------------------------------------------------------------------------------------- helper methods
    public void removeBrowsePermission()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_10_10000");
        submit("Delete");
    }

    public void removeCreatePermission()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_11_10000");//create perm
        submit("Delete");
    }

    public void removeProjectAdminPermission()
    {
        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_23_jira-developers");
        submit("Delete");
    }

    public void addBrowsePermission()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_10");//browse perm
        checkCheckbox("type", "group");
        selectOption("group", "jira-users");
        submit(" Add ");
    }

    public void addCreatePermission()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_11");//create perm
        checkCheckbox("type", "group");
        selectOption("group", "jira-users");
        submit(" Add ");
    }

    public void addProjectAdminPermission()
    {
        navigation.gotoAdminSection("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_23");
        checkCheckbox("type", "group");
        selectOption("group", "jira-developers");
        submit(" Add ");
    }
}
