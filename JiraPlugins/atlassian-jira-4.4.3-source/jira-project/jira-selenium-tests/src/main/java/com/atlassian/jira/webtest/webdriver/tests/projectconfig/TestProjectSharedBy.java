package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.ConfigureFieldScreenScheme;
import com.atlassian.jira.pageobjects.pages.admin.ConfigureIssueTypeScreenScheme;
import com.atlassian.jira.pageobjects.pages.admin.ConfigureScreen;
import com.atlassian.jira.pageobjects.pages.admin.EditDefaultFieldConfigPage;
import com.atlassian.jira.pageobjects.pages.admin.EditFieldConfigPage;
import com.atlassian.jira.pageobjects.pages.admin.EditFieldSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.EditIssueSecurityScheme;
import com.atlassian.jira.pageobjects.pages.admin.EditNotificationsPage;
import com.atlassian.jira.pageobjects.pages.admin.EditPermissionScheme;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.EditIssueTypeSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.EditWorkflowScheme;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/TestProjectSharedBy.xml")
public class TestProjectSharedBy extends BaseJiraWebTest
{
    private static final String WF_USED = "Used";
    private static final String WF_NOT_USED = "Not Used";

    private static final List<String> ALL_PROJECTS = asList("<strong>XSS</strong>", "homosapien", "monkey");
    private static final String PROJECT_COUNT = "3 projects";

    @Test
    public void testWorkflow()
    {
        //Check an active workflow.
        ViewWorkflowSteps steps = jira.gotoLoginPage().loginAsSysAdmin(ViewWorkflowSteps.class, WF_USED);
        ProjectSharedBy sharedBy = steps.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(WF_USED, steps.getWorkflowName());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        //Check the draft.
        steps = jira.visit(ViewWorkflowSteps.class, WF_USED, true);
        sharedBy = steps.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        //Check a workflow that is not used.
        steps = jira.visit(ViewWorkflowSteps.class, WF_NOT_USED);
        sharedBy = steps.getSharedBy();
        assertFalse(sharedBy.isPresent());
        assertEquals(WF_NOT_USED, steps.getWorkflowName());
    }

    @Test
    public void testWorkflowScheme()
    {
        EditWorkflowScheme editWorkflowScheme = jira.gotoLoginPage().loginAsSysAdmin(EditWorkflowScheme.class, 10001L);
        ProjectSharedBy sharedBy = editWorkflowScheme.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        editWorkflowScheme = jira.visit(EditWorkflowScheme.class, 10000L);
        sharedBy = editWorkflowScheme.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testFieldLayout()
    {
        //Check an active field layout.
        EditFieldConfigPage fieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(EditFieldConfigPage.class, 10000L);
        ProjectSharedBy sharedBy = fieldConfigPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Used", fieldConfigPage.getName());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("<strong>XSS</strong>", "homosapien"), sharedBy.getProjects());

        //Check an active system default field layout.
        EditDefaultFieldConfigPage defaultFieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(EditDefaultFieldConfigPage.class);
        sharedBy = defaultFieldConfigPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Default Field Configuration", fieldConfigPage.getName());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("monkey"), sharedBy.getProjects());

        //Check a field layout not used
        EditFieldConfigPage notusedFieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(EditFieldConfigPage.class, 10001L);
        assertEquals("Not used", fieldConfigPage.getName());
        sharedBy = notusedFieldConfigPage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testFieldLayoutScheme()
    {
        //Check an active field scheme.
        final EditFieldSchemePage editFieldSchemePage = jira.gotoLoginPage().loginAsSysAdmin(EditFieldSchemePage.class, 10000L);
        ProjectSharedBy sharedBy = editFieldSchemePage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Used", editFieldSchemePage.getName());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("homosapien"), sharedBy.getProjects());

        //Check a field scheme not used
        EditFieldSchemePage notusedFieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(EditFieldSchemePage.class, 10100L);
        assertEquals("Not used", notusedFieldConfigPage.getName());
        sharedBy = notusedFieldConfigPage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testScreenScheme()
    {
        //Check an active field layout.
        ConfigureFieldScreenScheme fieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(ConfigureFieldScreenScheme.class, 1L);
        ProjectSharedBy sharedBy = fieldConfigPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Default Screen Scheme", fieldConfigPage.getName());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("homosapien", "monkey"), sharedBy.getProjects());

        //Check an active system default field layout.
        fieldConfigPage = jira.gotoLoginPage().loginAsSysAdmin(ConfigureFieldScreenScheme.class, 10000L);
        sharedBy = fieldConfigPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Used", fieldConfigPage.getName());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("<strong>XSS</strong>"), sharedBy.getProjects());

        //Check a field layout not used
        ConfigureFieldScreenScheme notUsedScreenScheme = jira.gotoLoginPage().loginAsSysAdmin(ConfigureFieldScreenScheme.class, 10001L);
        assertEquals("Not used", notUsedScreenScheme.getName());
        sharedBy = notUsedScreenScheme.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testIssueTypeScreenScheme()
    {
        //Check an active field scheme.
        final ConfigureIssueTypeScreenScheme editIssueTypeScreenSchemePage = jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureIssueTypeScreenScheme.class, 10000L);
        ProjectSharedBy sharedBy = editIssueTypeScreenSchemePage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Used", editIssueTypeScreenSchemePage.getName());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("<strong>XSS</strong>"), sharedBy.getProjects());

        final ConfigureIssueTypeScreenScheme editDefaultIssueTypeScreenScheme = jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureIssueTypeScreenScheme.class, 1L);
        sharedBy = editDefaultIssueTypeScreenScheme.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("Default Issue Type Screen Scheme", editDefaultIssueTypeScreenScheme.getName());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Lists.<String>newArrayList("homosapien", "monkey"), sharedBy.getProjects());

        //Check a field scheme not used
        ConfigureIssueTypeScreenScheme notUsedIssueTypeScreenScheme = jira.gotoLoginPage()
                .loginAsSysAdmin(ConfigureIssueTypeScreenScheme.class, 10001L);
        assertEquals("Not used", editDefaultIssueTypeScreenScheme.getName());
        sharedBy = notUsedIssueTypeScreenScheme.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testIssueTypeScheme()
    {
        EditIssueTypeSchemePage issueTypeSchemePage = jira.gotoLoginPage().loginAsSysAdmin(EditIssueTypeSchemePage.class, 10010L);
        ProjectSharedBy sharedBy = issueTypeSchemePage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        issueTypeSchemePage = jira.visit(EditIssueTypeSchemePage.class, 10011L);
        sharedBy = issueTypeSchemePage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testIssueSecurityScheme()
    {
        EditIssueSecurityScheme issueSecuritySchemePage = jira.gotoLoginPage().loginAsSysAdmin(EditIssueSecurityScheme.class, 10000L);
        ProjectSharedBy sharedBy = issueSecuritySchemePage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        issueSecuritySchemePage = jira.visit(EditIssueSecurityScheme.class, 10001L);
        sharedBy = issueSecuritySchemePage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testNotificationScheme()
    {
        EditNotificationsPage editNotificationsPage = jira.gotoLoginPage().loginAsSysAdmin(EditNotificationsPage.class, 10000L);
        ProjectSharedBy sharedBy = editNotificationsPage .getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        editNotificationsPage = jira.visit(EditNotificationsPage.class, 10010L);
        sharedBy = editNotificationsPage .getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testPermissionScheme()
    {
        EditPermissionScheme permissionSchemePage = jira.gotoLoginPage().loginAsSysAdmin(EditPermissionScheme.class, 10000L);
        ProjectSharedBy sharedBy = permissionSchemePage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals(PROJECT_COUNT, sharedBy.getTriggerText());
        assertEquals(ALL_PROJECTS, sharedBy.getProjects());

        permissionSchemePage = jira.visit(EditPermissionScheme.class, 10001L);
        sharedBy = permissionSchemePage.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Test
    public void testScreensUnused()
    {
        ConfigureScreen configureScreen = jira.gotoLoginPage().loginAsSysAdmin(ConfigureScreen.class, 3L);
        ProjectSharedBy sharedBy = configureScreen.getSharedBy();
        assertFalse(sharedBy.isPresent());
    }

    @Restore ("xml/TestProjectSharedByScreens.xml")
    @Test
    public void testScreensUsedByBothWorkflowsAndScreenSchemes()
    {
        // Just by workflows first
        ConfigureScreen configureScreen = jira.gotoLoginPage().loginAsSysAdmin(ConfigureScreen.class, 10000L);
        ProjectSharedBy sharedBy = configureScreen.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Project A"), sharedBy.getProjects());

        configureScreen = jira.gotoLoginPage().loginAsSysAdmin(ConfigureScreen.class, 10001L);
        sharedBy = configureScreen.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("1 project", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Project D"), sharedBy.getProjects());

        configureScreen = jira.gotoLoginPage().loginAsSysAdmin(ConfigureScreen.class, 10002L);
        sharedBy = configureScreen.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Project B", "Project C"), sharedBy.getProjects());

    }

}
