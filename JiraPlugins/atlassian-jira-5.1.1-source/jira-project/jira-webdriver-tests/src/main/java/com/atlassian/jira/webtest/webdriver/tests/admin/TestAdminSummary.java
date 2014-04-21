package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.AdminSummaryPage;
import com.atlassian.jira.pageobjects.pages.admin.ProjectIntroVideoDialog;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for the "Add Project" dialog.
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
@Restore("xml/TestAdminSummary.xml")
public class TestAdminSummary extends BaseJiraWebTest
{
    @Test
    public void testWithAdmin()
    {
        AdminSummaryPage page = jira.gotoLoginPage().loginAsSysAdmin(AdminSummaryPage.class);
        // Test all sections present
        assertTrue(page.getProjectsSection().isPresent());
        assertTrue(page.getPluginsSection().isPresent());
        assertTrue(page.getUsersSection().isPresent());
        assertTrue(page.getOptionsSection().isPresent());
        assertTrue(page.getSystemSection().isPresent());

        // Test that we have 3 projects and the categoriees link
        assertEquals("All 3 projects", page.getAllProjectLink().getText());
        assertTrue(page.getProjectCategoriesLink().isPresent());
        assertTrue(page.getAddNewProjectLink().isPresent());
        // Check we have links for each project
        assertTrue(page.getProjectLinkFor("HSP").isPresent());
        assertTrue(page.getProjectLinkFor("MKY").isPresent());
        assertTrue(page.getProjectLinkFor("XSS").isPresent());

        // Check each link and come back to this page
        testProjectLink(page, "HSP");
        testProjectLink(page, "MKY");
        testProjectLink(page, "XSS");

        long userCount = backdoor.usersAndGroups().getNumberOfUsers();
        long groupCount = backdoor.usersAndGroups().getNumberOfGroups();

        // Check the user counts
        assertEquals("Users (" + userCount + ")", page.getUsersText());
        assertEquals("Groups (" + groupCount + ")", page.getGroupsText());
        assertEquals("Roles (3)", page.getRolesText());
    }

    private void testProjectLink(AdminSummaryPage page, String project)
    {
        page.getProjectLinkFor(project).click();
        ProjectSummaryPageTab adminPage = pageBinder.bind(ProjectSummaryPageTab.class);
        assertEquals(project, adminPage.getProjectKey());
        pageBinder.navigateToAndBind(AdminSummaryPage.class);
    }

    @Test
    public void testWithProjectAdmin()
    {
        AdminSummaryPage page = jira.gotoLoginPage().login("fred", "fred", AdminSummaryPage.class);
        // Test only project section present
        assertTrue(page.getProjectsSection().isPresent());
        assertFalse(page.getUsersSection().isPresent());
        assertFalse(page.getOptionsSection().isPresent());
        assertFalse(page.getSystemSection().isPresent());

        // Test that we have 1 projects and the categoriees link
        assertEquals("All 1 projects", page.getAllProjectLink().getText());
        assertFalse(page.getProjectCategoriesLink().isPresent());
        assertFalse(page.getAddNewProjectLink().isPresent());

        assertTrue(page.getProjectLinkFor("HSP").isPresent());
        testProjectLink(page, "HSP");
    }

    @Test
    public void testWithNonAdmin()
    {
        try
        {
            jira.gotoLoginPage().login("user", "user", AdminSummaryPage.class);
            //This wont work hopefully
        }
        catch (Exception e)
        {
        }

        jira.logout();
        AdminSummaryPage page = jira.gotoLoginPage().loginAsSystemAdminAndFollowRedirect(AdminSummaryPage.class);

        assertTrue(page.getProjectsSection().isPresent());
    }

    @Test
    @Restore("xml/TestAdminSummaryIntroVideo.xml")
    public void testAddProjectIntroVideoDialog()
    {
        final ProjectIntroVideoDialog projectIntroVideoDialog = jira.gotoLoginPage().
                loginAsSysAdmin(AdminSummaryPage.class).openAddProjectVideo();
        
        assertTrue(projectIntroVideoDialog.isOpen());
    }
}
