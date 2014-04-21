package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.project.AddProjectDialog;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.PermissionsPanel;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.notifications.NotificationsPanel;
import com.atlassian.jira.pageobjects.project.summary.people.PeoplePanel;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for the "Add Project" dialog.
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PROJECTS  })
@Restore("xml/blankprojects.xml")
public class TestAddProject extends BaseJiraWebTest
{
    public static final String FIELD_NAME = "name";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_LEAD = "lead";

    @Test
    public void testInvalidData()
    {
        AddProjectDialog dialog = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog();
        dialog.createProjectFail("HSP", "homosapien", null);

        //Try and create a duplicate project.
        Map<String, String> errors = dialog.getFormErrors();
        assertEquals("A project with that name already exists.", errors.get(FIELD_NAME));
        assertEquals("A project with that project key already exists.", errors.get(FIELD_KEY));

        //Bad name and description.
        dialog.createProjectFail("", "", "");
        errors = dialog.getFormErrors();
        assertEquals("You must specify a valid project name.", errors.get(FIELD_NAME));
        assertEquals("You must specify a unique project key, at least 2 characters long, containing only uppercase letters.", errors.get(FIELD_KEY));
        assertEquals("You must specify a project lead.", errors.get(FIELD_LEAD));

        //Lets set some of the fields to make sure sure they are propgated through hidden parameters.
        dialog.createProjectFail("DD", null, "admin");
        errors = dialog.getFormErrors();
        assertEquals("You must specify a valid project name.", errors.get(FIELD_NAME));

        //Lets now create a correct project and make sure it works after all these errors.
        ProjectSummaryPageTab success = dialog.createProjectSuccess(null, "DeadDude", null);
        assertProjectData(success, "DD", "DeadDude", "Administrator");
    }

    @Test
    @Restore("xml/NoBrowseUsersPermission.xml")
    public void testUserPickerDisabled()
    {
        AddProjectDialog dialog = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog();

        assertTrue("Expected lead picker to be disabled",
                dialog.isLeadpickerDisabled());

        //Lets set some of the fields to make sure sure they are propgated through hidden parameters.
        dialog.createProjectFail("DD", "Your ABCs", "adminfred");
        Map<String, String> errors = dialog.getFormErrors();
        assertEquals("The user you have specified as project lead does not exist.", errors.get(FIELD_LEAD));

        ProjectSummaryPageTab summary = dialog.createProjectSuccess("ABC", null, "fred");
        assertProjectData(summary, "ABC", "Your ABCs", "Fred Normal");
    }

    @Test
    public void testSuccess()
    {
        AddProjectDialog dialog = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog();

        ProjectSummaryPageTab summary = dialog.createProjectSuccess("DUD", "DUDE", null);
        assertProjectData(summary, "DUD", "DUDE", "Administrator");

        dialog = jira.visit(ViewProjectsPage.class).openCreateProjectDialog();
        summary = dialog.createProjectSuccess("ABC", "Your ABCs", "fred");
        assertProjectData(summary, "ABC", "Your ABCs", "Fred Normal");

        dialog = jira.visit(ViewProjectsPage.class).openCreateProjectDialog();
        //Try and create a project key and name that is too long. They should be limited by AUI.
        summary = dialog.createProjectSuccess(StringUtils.repeat("K", 256), StringUtils.repeat("N", 151), "fred");
        assertProjectData(summary, StringUtils.repeat("K", 255), StringUtils.repeat("N", 150), "Fred Normal");
    }

    @Test
    @Restore("xml/testBrowseProjectCreateIssue.xml")
    public void testDefaultPermissionSchemeSelectedByDefault() throws Exception
    {
        AddProjectDialog dialog = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog();
        ProjectSummaryPageTab summary = dialog.createProjectSuccess("DUD", "DUDE", null);

        assertProjectData(summary, "DUD", "DUDE", "Administrator");
        PermissionsPanel permissionsPanel = summary.openPanel(PermissionsPanel.class);
        assertTrue(permissionsPanel.isHasPermissionScheme());
        assertEquals("Default Permission Scheme", permissionsPanel.getPermissionScheme());

        NotificationsPanel notificationsPanel = summary.openPanel(NotificationsPanel.class);
        assertEquals("None", notificationsPanel.getNotificationSchemeLinkText());
    }

    @Test
    @Restore("xml/TestAddProjectLinkVisibility.xml")
    public void testAddProjectLinkVisibility()
    {
        ViewProjectsPage projectsPage = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class);
        assertTrue(projectsPage.canCreateProject());

        projectsPage = jira.gotoLoginPage().login("projectadmin", "projectadmin", ViewProjectsPage.class);
        assertFalse(projectsPage.canCreateProject());

        projectsPage = jira.gotoLoginPage().login("fred", "fred", ViewProjectsPage.class);
        assertFalse(projectsPage.canCreateProject());
    }

    /**
     * JRADEV-4818: What happens when there is only one user in the system.
     */
    @Test
    @Restore("xml/TestCreateProjectOneUser.xml")
    public void testAddProjectOneUser()
    {
        AddProjectDialog createProjectDialog = jira.gotoLoginPage().loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog();

        assertFalse(createProjectDialog.isLeadPresent());
        createProjectDialog.setName("Admin Project");
        createProjectDialog.setKey("ADM");

        ProjectSummaryPageTab summaryPageTab = createProjectDialog.submitSuccess();
        assertProjectData(summaryPageTab, "ADM", "Admin Project", "Administrator");

        //Guest will always see the lead because the default lead as the current user is invalid and as such
        //it must be specified manually.

        //This test can't be executed until JRADEV-6018 has been fixed.
//        jira.logout();
//        createProjectDialog = jira.goTo(ViewProjectsPage.class).openCreateProjectDialog();
//        assertTrue(createProjectDialog.isLeadPresent());
//        summaryPageTab = createProjectDialog.createProjectSuccess("GUEST", "GUEST", "admin");
//        assertProjectData(summaryPageTab, "GUEST", "GUEST", "admin");
    }

    private ProjectSummaryPageTab assertProjectData(ProjectSummaryPageTab summaryPageTab, String key, String name, String lead)
    {
        assertEquals(key, summaryPageTab.getProjectKey());
        assertEquals(name, summaryPageTab.getProjectHeader().getProjectName());
        assertEquals(lead, summaryPageTab.openPanel(PeoplePanel.class).getProjectLead());

        return summaryPageTab;
    }
}
