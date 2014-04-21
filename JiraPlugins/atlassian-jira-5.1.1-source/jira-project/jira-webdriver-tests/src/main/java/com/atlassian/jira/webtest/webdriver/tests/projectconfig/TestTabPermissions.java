package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigErrorPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for project permissions checks.
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestTabPermissions.xml")
public class TestTabPermissions extends BaseJiraWebTest
{
    private static final String KEY_WITH_PROJ_ADMIN = "PROJ";
    private static final String KEY_NO_ADMINS = "NOADMIN";
    private static final String KEY_ANON = "ANON";

    private static final String USER_SIMPLEADMIN = "simpleadmin";
    private static final String USER_PROJECTADMIN = "fred";

    @Test
    public void testProjectDoesNotExist()
    {
        ProjectConfigErrorPage page = jira.gotoLoginPage().loginAsSysAdmin(ProjectConfigErrorPage.class, "BAD");
        assertFalse(page.getMessages().isEmpty());
        page.clickViewProjects();
    }

    @Test
    public void testAdminsHaveAccessWhenProjectAdminDoesNot()
    {
        ProjectSummaryPageTab summaryPageTab = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, KEY_NO_ADMINS);
        assertEquals(KEY_NO_ADMINS, summaryPageTab.getProjectKey());

        summaryPageTab = jira.gotoLoginPage().login(USER_SIMPLEADMIN, USER_SIMPLEADMIN, ProjectSummaryPageTab.class, KEY_NO_ADMINS);
        assertEquals(KEY_NO_ADMINS, summaryPageTab.getProjectKey());

        //Project admin should not be able to see this project.
        ProjectConfigErrorPage errorPage = jira.gotoLoginPage().login(USER_PROJECTADMIN, USER_PROJECTADMIN,
                ProjectConfigErrorPage.class, KEY_NO_ADMINS);
        assertTrue(!errorPage.getMessages().isEmpty());
    }

    @Test
    public void testAdminsHaveAccess()
    {
        ProjectSummaryPageTab summaryPageTab = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, KEY_WITH_PROJ_ADMIN);
        assertEquals(KEY_WITH_PROJ_ADMIN, summaryPageTab.getProjectKey());

        summaryPageTab = jira.gotoLoginPage().login(USER_SIMPLEADMIN, USER_SIMPLEADMIN, ProjectSummaryPageTab.class, KEY_WITH_PROJ_ADMIN);
        assertEquals(KEY_WITH_PROJ_ADMIN, summaryPageTab.getProjectKey());

        //Project admin should not be able to see this project.
        summaryPageTab = jira.gotoLoginPage().login(USER_PROJECTADMIN, USER_PROJECTADMIN,
                ProjectSummaryPageTab.class, KEY_WITH_PROJ_ADMIN);
        assertEquals(KEY_WITH_PROJ_ADMIN, summaryPageTab.getProjectKey());
    }

    @Test
    public void testBadUrls()
    {
        //Test no project specified.
        ProjectConfigErrorPage page = jira.gotoLoginPage().loginAsSysAdmin(ProjectConfigErrorPage.class, "");
        assertTrue(page.hasErrors());

        //Bad panel.
        page = pageBinder.navigateToAndBind(ProjectConfigErrorPage.class, KEY_NO_ADMINS + "/somepanelthatdoesnotexist");
        assertTrue(page.hasErrors());
    }

    @Test
    public void testAnonymousAsProjectAdmin()
    {
        ProjectSummaryPageTab page = jira.visit(ProjectSummaryPageTab.class, KEY_ANON);
        assertEquals(KEY_ANON, page.getProjectKey());
    }

    @Test
    public void testAnonymousRedirect()
    {
        jira.logout();

        //We actually get redirected to the login page, so don't try an bind now.
        jira.visitDelayed(ProjectSummaryPageTab.class, KEY_NO_ADMINS);

        JiraLoginPage loginPage = pageBinder.bind(JiraLoginPage.class);
        ProjectSummaryPageTab pageTab = loginPage.loginAsSystemAdminAndFollowRedirect(ProjectSummaryPageTab.class, KEY_NO_ADMINS);
        assertEquals(KEY_NO_ADMINS, pageTab.getProjectKey());

        jira.logout();

        //We actually get redirected to the login page, so don't try an bind now.
        jira.visitDelayed(ProjectSummaryPageTab.class, KEY_NO_ADMINS);

        //Make sure a user without permission to see the project is displayed an error message and is not redirected
        //to the login page.
        loginPage = pageBinder.bind(JiraLoginPage.class);
        ProjectConfigErrorPage errorPage = loginPage.loginAndFollowRedirect(USER_PROJECTADMIN, USER_PROJECTADMIN,
                ProjectConfigErrorPage.class, KEY_NO_ADMINS);
        
        assertTrue(errorPage.hasErrors());
    }
}
