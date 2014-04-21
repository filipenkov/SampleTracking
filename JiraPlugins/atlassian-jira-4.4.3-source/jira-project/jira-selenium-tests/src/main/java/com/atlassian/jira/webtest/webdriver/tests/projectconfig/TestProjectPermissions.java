package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.EditPermissionScheme;
import com.atlassian.jira.pageobjects.pages.admin.SelectPermissionScheme;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.project.permissions.Permission;
import com.atlassian.jira.pageobjects.project.permissions.PermissionGroup;
import com.atlassian.jira.pageobjects.project.permissions.ProjectPermissionPageTab;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *
 * @since v4.4
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.PERMISSIONS })
@Restore ("xml/ProjectPermissionConfig.xml")
public class TestProjectPermissions extends BaseJiraWebTest
{
    public static final String PROJECT_HSP = "HSP";

    @Test
    public void testTabNavigation()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_HSP);
        ProjectConfigTabs tabs = config.getTabs();
        assertTrue(tabs.isSummaryTabSelected());

        ProjectPermissionPageTab projectPermissionPageTab = tabs.gotoProjectPermissionsTab();
        assertTrue(projectPermissionPageTab.getTabs().isProjectPermissionsTabSelected());
        assertEquals(PROJECT_HSP, projectPermissionPageTab.getProjectKey());

        final ProjectSharedBy sharedBy = projectPermissionPageTab.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Shared project", "homosapien"), sharedBy.getProjects());
    }

    @Test
    public void testProjectAdmin()
    {
        final ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().login("project_admin", "project_admin", ProjectPermissionPageTab.class, PROJECT_HSP);
        assertEquals("Default Permission Scheme", projectPermissionPage.getSchemeName());
        assertEquals("This is the default Permission Scheme. Any new projects that are created will be assigned this scheme", projectPermissionPage.getSchemeDescription());
        final List<PermissionGroup> permissionGroups = projectPermissionPage.getPermissionGroups();
        assertEquals(6, permissionGroups.size());

        // Assert the cog actions aren't present
        assertFalse(projectPermissionPage.isSchemeLinked());
        assertFalse(projectPermissionPage.isSchemeChangeAvailable());

        final ProjectSharedBy sharedBy = projectPermissionPage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testDefaultGroups()
    {
        ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().loginAsSysAdmin(ProjectPermissionPageTab.class, PROJECT_HSP);

        assertEquals("Default Permission Scheme", projectPermissionPage.getSchemeName());
        assertEquals("This is the default Permission Scheme. Any new projects that are created will be assigned this scheme", projectPermissionPage.getSchemeDescription());
        final List<PermissionGroup> permissionGroups = projectPermissionPage.getPermissionGroups();
        assertEquals(6, permissionGroups.size());

        assertTrue(projectPermissionPage.isSchemeLinked());

        final EditPermissionScheme editPermissionScheme = projectPermissionPage.gotoScheme();

        projectPermissionPage = editPermissionScheme.back(ProjectPermissionPageTab.class, PROJECT_HSP);
        assertEquals("Default Permission Scheme", projectPermissionPage.getSchemeName());
        assertEquals("This is the default Permission Scheme. Any new projects that are created will be assigned this scheme", projectPermissionPage.getSchemeDescription());

        assertTrue(projectPermissionPage.isSchemeChangeAvailable());
        final SelectPermissionScheme selectPermissionScheme = projectPermissionPage.gotoSelectScheme();
        selectPermissionScheme.setSchemeByName("Empty Scheme");
        selectPermissionScheme.submit();

        projectPermissionPage = jira.visit(ProjectPermissionPageTab.class, PROJECT_HSP);
        assertEquals("Empty Scheme", projectPermissionPage.getSchemeName());
        assertEquals("", projectPermissionPage.getSchemeDescription());
    }

    @Test
    public void testNoPermissions()
    {
        final ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().loginAsSysAdmin(ProjectPermissionPageTab.class, "MKY");

        assertEquals("Empty Scheme", projectPermissionPage.getSchemeName());
        assertEquals("", projectPermissionPage.getSchemeDescription());
        final List<PermissionGroup> permissionGroups = projectPermissionPage.getPermissionGroups();
        assertEquals(6, permissionGroups.size());
        for (PermissionGroup permissionGroup : permissionGroups)
        {
            final List<Permission> permissions = permissionGroup.getPermissions();
            for (Permission permission : permissions)
            {
                assertTrue(permission.getEntities().isEmpty());
            }
        }

        final ProjectSharedBy sharedBy = projectPermissionPage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testAllTypes()
    {
        final ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().loginAsSysAdmin(ProjectPermissionPageTab.class, "CHOC");
        assertEquals("Choc Full Scheme", projectPermissionPage.getSchemeName());
        assertEquals("Choc Full Permission Scheme", projectPermissionPage.getSchemeDescription());

        final Permission permission = projectPermissionPage.getPermissionByName("Create Issues");
        final List<String> entities = permission.getEntities();
        final List<String> expectedEntities = CollectionBuilder.newBuilder("Reporter", "Group (jira-administrators)", "Group (Anyone)", "Single User (admin)", "Project Lead", "Current Assignee", "Project Role (Administrators)", "User Custom Field Value (User Picker)", "Group Custom Field Value (Group Picker)").asList();
        assertEquals(expectedEntities, entities);

        final ProjectSharedBy sharedBy = projectPermissionPage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testXSS()
    {
        final ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().loginAsSysAdmin(ProjectPermissionPageTab.class, "XSS");
        assertEquals("<script>alert(\"wtf\");</script>", projectPermissionPage.getSchemeName());
        assertEquals("<script>alert(\"wtf\");</script>", projectPermissionPage.getSchemeDescription());
        final Permission permission = projectPermissionPage.getPermissionByName("Administer Projects");

        final List<String> entities = permission.getEntities();
        final List<String> expectedEntities = CollectionBuilder.newBuilder("User Custom Field Value (<script>alert(\"wtf\");</script>)").asList();
        assertEquals(expectedEntities, entities);

        final ProjectSharedBy sharedBy = projectPermissionPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("<script>alert(\"wtf\");</script>", "Another Shared project"), sharedBy.getProjects());
    }

    @Test
    public void testProjectAdminCanViewSharedBy()
    {
        final ProjectPermissionPageTab projectPermissionPage = jira.gotoLoginPage().login("project_admin", "project_admin", ProjectPermissionPageTab.class, "XSS");
        assertEquals("<script>alert(\"wtf\");</script>", projectPermissionPage.getSchemeName());
        assertEquals("<script>alert(\"wtf\");</script>", projectPermissionPage.getSchemeDescription());
        final Permission permission = projectPermissionPage.getPermissionByName("Administer Projects");

        final List<String> entities = permission.getEntities();
        final List<String> expectedEntities = CollectionBuilder.newBuilder("User Custom Field Value (<script>alert(\"wtf\");</script>)").asList();
        assertEquals(expectedEntities, entities);

        final ProjectSharedBy sharedBy = projectPermissionPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("<script>alert(\"wtf\");</script>", "Another Shared project"), sharedBy.getProjects());
    }

}
