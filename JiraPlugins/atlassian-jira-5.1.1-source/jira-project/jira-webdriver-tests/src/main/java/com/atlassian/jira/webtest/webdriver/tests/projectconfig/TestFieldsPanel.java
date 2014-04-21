package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.ChangeFieldSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.EditDefaultFieldConfigPage;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.ConfigureFieldConfigurationSchemePage;
import com.atlassian.jira.pageobjects.project.fields.Field;
import com.atlassian.jira.pageobjects.project.fields.FieldConfiguration;
import com.atlassian.jira.pageobjects.project.fields.FieldsPanel;
import com.atlassian.jira.pageobjects.project.fields.MockField;
import com.atlassian.jira.pageobjects.project.fields.MockFieldConfiguration;
import com.atlassian.jira.pageobjects.project.fields.MockProjectsDialogProject;
import com.atlassian.jira.pageobjects.project.fields.ProjectsDialogProject;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Project Configuration Fields Tab
 *
 * @since v4.4
 */

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE, Category.IE_INCOMPATIBLE })
@Restore ("xml/TestFieldsTab.xml")
public class TestFieldsPanel extends BaseJiraWebTest
{
    @Test
    public void testSystemDefaultFieldScheme()
    {

        FieldsPanel fieldsPanel = jira.gotoLoginPage().loginAsSysAdmin(FieldsPanel.class, "HSP");
        assertEquals("System Default Field Configuration", fieldsPanel.getSchemeName());
        assertTrue(fieldsPanel.isSchemeLinked());
        assertTrue(fieldsPanel.isSchemeChangeAvailable());
        assertTrue(canChangeScheme(fieldsPanel, "homosapien"));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "HSP");
        assertTrue(canEditSchemeWithNoSchemesPresent(fieldsPanel));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "HSP");

        final LinkedHashMap<String, String> expectedIssueTypes = getAllIssueTypes();

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        getSystemDefaultFieldConfig()
                                .setEditLink(true)
                                .setIsDefault(true)
                                .issueTypes(expectedIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("3 projects")
                );

        // Contains
        // - test if field config is not shared, the right text is shown
        // - test if field config is shared, the right text is shown
        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();
        assertEquals(expectedFieldConfigurations, fieldconfigurations);

        // Check that inline dialogs work
        final List<ProjectsDialogProject> projects = fieldconfigurations.get(0).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("homosapien", prependBaseUrl("/secure/projectavatar?size=small&pid=10000&avatarId=10011")),
                new MockProjectsDialogProject("monkey", prependBaseUrl("/secure/projectavatar?size=small&pid=10001&avatarId=10011"))
        );

        assertEquals(expectedProjects, projects);

        // Assignee field
        final List<String> screens = fieldconfigurations.get(0)
                .getFields().get(1)
                .openScreensDialog()
                .getScreens();

        final List<String> expectedScreens = Lists.newArrayList("Default Screen", "Resolve Issue Screen", "Workflow Screen");

        assertEquals(expectedScreens, screens);

    }

    @Test
    public void testSystemDefaultFieldSchemeAsProjectAdmin()
    {

        FieldsPanel fieldsPanel = jira.gotoLoginPage().login("project_admin", "project_admin", FieldsPanel.class, "HSP");
        assertEquals("System Default Field Configuration", fieldsPanel.getSchemeName());
        assertFalse(fieldsPanel.isSchemeLinked());
        assertFalse(fieldsPanel.isSchemeChangeAvailable());

        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();

        final LinkedHashMap<String, String> expectedIssueTypes = getAllIssueTypes();

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        getSystemDefaultFieldConfig()
                                .setEditLink(false)
                                .setIsDefault(true)
                                .issueTypes(expectedIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("2 projects")
                );

        assertEquals(expectedFieldConfigurations, fieldconfigurations);

        // Check that inline dialogs work
        final List<ProjectsDialogProject> projects = fieldconfigurations.get(0).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("homosapien", prependBaseUrl("/secure/projectavatar?size=small&pid=10000&avatarId=10011"))
        );

        assertEquals(expectedProjects, projects);

        // Assignee field
        final List<String> screens = fieldconfigurations.get(0)
                .getFields().get(1)
                .openScreensDialog()
                .getScreens();

        final List<String> expectedScreens = Lists.newArrayList("Default Screen", "Resolve Issue Screen", "Workflow Screen");

        assertEquals(expectedScreens, screens);
    }

    @Test
    public void testFieldSchemeWithMultipleFieldConfigs()
    {
        FieldsPanel fieldsPanel = jira.gotoLoginPage().loginAsSysAdmin(FieldsPanel.class, "LALA");
        assertEquals("Multiple Field Configuration Scheme", fieldsPanel.getSchemeName());
        assertTrue(fieldsPanel.isSchemeLinked());
        assertTrue(fieldsPanel.isSchemeChangeAvailable());
        assertTrue(canChangeScheme(fieldsPanel, "LALA"));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "LALA");
        assertTrue(canConfigureScheme(fieldsPanel, "Multiple Field Configuration Scheme"));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "LALA");

        final LinkedHashMap<String, String> allIssueTypes = getAllIssueTypes();

        final LinkedHashMap<String, String> defaultFieldConfigIssueTypes = Maps.newLinkedHashMap();
        defaultFieldConfigIssueTypes.put("Bug", allIssueTypes.get("Bug"));
        defaultFieldConfigIssueTypes.put("New Feature", allIssueTypes.get("New Feature"));

        final LinkedHashMap<String, String> otherFieldConfigIssueTypes = Maps.newLinkedHashMap();
        otherFieldConfigIssueTypes.put("Improvement", allIssueTypes.get("Improvement"));

        final LinkedHashMap<String, String> yetAnotherFieldConfigIssueTypes = Maps.newLinkedHashMap();
        yetAnotherFieldConfigIssueTypes.put("Task", allIssueTypes.get("Task"));

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        new MockFieldConfiguration("Other Field Configuration")
                                .setEditLink(true)
                                .setIsDefault(true)
                                .issueTypes(otherFieldConfigIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("3 projects")
                                .fields(getDefaultFields()),
                        getSystemDefaultFieldConfig()
                                .setEditLink(true)
                                .setIsDefault(false)
                                .issueTypes(defaultFieldConfigIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("3 projects"),
                        new MockFieldConfiguration("Yet Another Field Configuration")
                                .setEditLink(true)
                                .setIsDefault(false)
                                .issueTypes(yetAnotherFieldConfigIssueTypes)
                                .setHasSharedProjects(false)
                                .fields(getDefaultFields())
                );

        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();

        // Contains
        // - test if some other field config is default
        // - test if some other field config is not shared, the right usages is shown
        assertEquals(expectedFieldConfigurations, fieldconfigurations);

        // Check that inline dialogs work
        final List<ProjectsDialogProject> otherProjects = fieldconfigurations.get(0).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedOtherProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("LOLO", prependBaseUrl("/secure/projectavatar?size=small&pid=10111&avatarId=10011")),
                new MockProjectsDialogProject("monkey", prependBaseUrl("/secure/projectavatar?size=small&pid=10001&avatarId=10011"))
        );

        assertEquals(expectedOtherProjects, otherProjects);


        final List<ProjectsDialogProject> defaultProjects = fieldconfigurations.get(1).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedDefaultProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("homosapien", prependBaseUrl("/secure/projectavatar?size=small&pid=10000&avatarId=10011")),
                new MockProjectsDialogProject("monkey", prependBaseUrl("/secure/projectavatar?size=small&pid=10001&avatarId=10011"))
        );

        assertEquals(expectedDefaultProjects, defaultProjects);


        // Assignee field
        final List<String> screens = fieldconfigurations.get(1)
                .getFields().get(1)
                .openScreensDialog()
                .getScreens();

        final List<String> expectedScreens = Lists.newArrayList("Default Screen", "Resolve Issue Screen", "Workflow Screen");

        assertEquals(expectedScreens, screens);
    }

    @Test
    public void testFieldSchemeWithMultipleFieldConfigsAsProjectAdmin()
    {
        FieldsPanel fieldsPanel = jira.gotoLoginPage().login("project_admin", "project_admin", FieldsPanel.class, "LALA");
        assertEquals("Multiple Field Configuration Scheme", fieldsPanel.getSchemeName());
        assertFalse(fieldsPanel.isSchemeLinked());
        assertFalse(fieldsPanel.isSchemeChangeAvailable());

        final LinkedHashMap<String, String> allIssueTypes = getAllIssueTypes();

        final LinkedHashMap<String, String> defaultFieldConfigIssueTypes = Maps.newLinkedHashMap();
        defaultFieldConfigIssueTypes.put("Bug", allIssueTypes.get("Bug"));
        defaultFieldConfigIssueTypes.put("New Feature", allIssueTypes.get("New Feature"));

        final LinkedHashMap<String, String> otherFieldConfigIssueTypes = Maps.newLinkedHashMap();
        otherFieldConfigIssueTypes.put("Improvement", allIssueTypes.get("Improvement"));

        final LinkedHashMap<String, String> yetAnotherFieldConfigIssueTypes = Maps.newLinkedHashMap();
        yetAnotherFieldConfigIssueTypes.put("Task", allIssueTypes.get("Task"));

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        new MockFieldConfiguration("Other Field Configuration")
                                .setEditLink(false)
                                .setIsDefault(true)
                                .issueTypes(otherFieldConfigIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("2 projects")
                                .fields(getDefaultFields()),
                        getSystemDefaultFieldConfig()
                                .setEditLink(false)
                                .setIsDefault(false)
                                .issueTypes(defaultFieldConfigIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("2 projects"),
                        new MockFieldConfiguration("Yet Another Field Configuration")
                                .setEditLink(false)
                                .setIsDefault(false)
                                .issueTypes(yetAnotherFieldConfigIssueTypes)
                                .setHasSharedProjects(false)
                                .fields(getDefaultFields())
                );

        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();

        // Contains
        // - test if some other field config is default
        // - test if some other field config is not shared, the right usages is shown
        assertEquals(expectedFieldConfigurations, fieldconfigurations);


        final List<ProjectsDialogProject> otherProjects = fieldconfigurations.get(0).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedOtherProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("LOLO", prependBaseUrl("/secure/projectavatar?size=small&pid=10111&avatarId=10011"))
        );

        assertEquals(expectedOtherProjects, otherProjects);

        final List<ProjectsDialogProject> projects = fieldconfigurations.get(1).openSharedProjects().getProjects();

        final List<ProjectsDialogProject> expectedProjects = Lists.<ProjectsDialogProject>newArrayList(
                new MockProjectsDialogProject("LALA", prependBaseUrl("/secure/projectavatar?size=small&pid=10010&avatarId=10004")),
                new MockProjectsDialogProject("homosapien", prependBaseUrl("/secure/projectavatar?size=small&pid=10000&avatarId=10011"))
        );

        assertEquals(expectedProjects, projects);

        // Assignee field
        final List<String> screens = fieldconfigurations.get(1)
                .getFields().get(1)
                .openScreensDialog()
                .getScreens();

        final List<String> expectedScreens = Lists.newArrayList("Default Screen", "Resolve Issue Screen", "Workflow Screen");

        assertEquals(expectedScreens, screens);

        final List<String> otherScreens = fieldconfigurations.get(1)
                .getFields().get(1)
                .openScreensDialog()
                .getScreens();

        assertEquals(expectedScreens, otherScreens);

    }

    @Test
    public void testFieldConfigSchemeWithAllIssueTypesAssigned()
    {
        final FieldsPanel fieldsPanel = jira.gotoLoginPage().loginAsSysAdmin(FieldsPanel.class, "LOLO");
        assertEquals("bleah", fieldsPanel.getSchemeName());

        final LinkedHashMap<String, String> expectedIssueTypes = getAllIssueTypes();

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        new MockFieldConfiguration("Other Field Configuration")
                                .setEditLink(true)
                                .setIsDefault(false)
                                .issueTypes(expectedIssueTypes)
                                .setHasSharedProjects(true)
                                .setSharedProjectText("3 projects")
                                .fields(getDefaultFields())
                );

        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();
        assertEquals(expectedFieldConfigurations, fieldconfigurations);
    }

    @Restore("xml/TestFieldsTabXSS.xml")
    @Test
    public void testFieldSchemeWithXSS()
    {
        FieldsPanel fieldsPanel = jira.gotoLoginPage().loginAsSysAdmin(FieldsPanel.class, "XSS");
        assertEquals("<strong>Oh noes</strong>", fieldsPanel.getSchemeName());
        assertTrue(fieldsPanel.isSchemeLinked());
        assertTrue(fieldsPanel.isSchemeChangeAvailable());
        assertTrue(canChangeScheme(fieldsPanel, "XSS"));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "XSS");
        assertTrue(canConfigureScheme(fieldsPanel, "<strong>Oh noes</strong>"));
        fieldsPanel = pageBinder.navigateToAndBind(FieldsPanel.class, "XSS");

        final LinkedHashMap<String, String> expectedIssueTypes = getAllIssueTypesWithXSS();

        final List<FieldConfiguration> expectedFieldConfigurations =
                Lists.<FieldConfiguration>newArrayList(
                        new MockFieldConfiguration("<strong>Oh noes</strong>")
                                .setEditLink(true)
                                .setIsDefault(true)
                                .issueTypes(expectedIssueTypes)
                                .setHasSharedProjects(false)
                                .fields(getXSSFields())
                );

        // Contains
        // - Test a field config is used by other projects
        // - Test a field config is used by a scheme in another project, but not applied

        final List<FieldConfiguration> fieldconfigurations = fieldsPanel.getFieldConfigurations();

        assertEquals(expectedFieldConfigurations, fieldconfigurations);
    }

    private LinkedHashMap<String, String> getAllIssueTypes()
    {
        final LinkedHashMap<String,String> expectedIssueTypes = Maps.newLinkedHashMap();

        expectedIssueTypes.put("Bug", prependBaseUrl("/images/icons/bug.gif"));
        expectedIssueTypes.put("Improvement", prependBaseUrl("/images/icons/improvement.gif"));
        expectedIssueTypes.put("New Feature", prependBaseUrl("/images/icons/newfeature.gif"));
        expectedIssueTypes.put("Task", prependBaseUrl("/images/icons/task.gif"));
        return expectedIssueTypes;
    }

    private LinkedHashMap<String, String> getAllIssueTypesWithXSS()
    {
        final LinkedHashMap<String,String> expectedIssueTypes = Maps.newLinkedHashMap();

        expectedIssueTypes.put("Bug", prependBaseUrl("/images/icons/bug.gif"));
        expectedIssueTypes.put("<strong>Oh noes</strong>", prependBaseUrl("/images/icons/genericissue.gif"));
        expectedIssueTypes.put("Improvement", prependBaseUrl("/images/icons/improvement.gif"));
        expectedIssueTypes.put("New Feature", prependBaseUrl("/images/icons/newfeature.gif"));
        expectedIssueTypes.put("Task", prependBaseUrl("/images/icons/task.gif"));
        return expectedIssueTypes;
    }

    private String prependBaseUrl(final String url)
    {
        return jira.getProductInstance().getBaseUrl() + url;
    }

    private MockFieldConfiguration getSystemDefaultFieldConfig()
    {
        return new MockFieldConfiguration("Default Field Configuration")
                .fields(getDefaultFields());
    }

    private List<Field> getDefaultFields()
    {
        return Lists.<Field>newArrayList(
                new MockField("Affects Version/s", null, false, "Autocomplete Renderer", "1 screen"),
                new MockField("Assignee", null, false, null, "3 screens"),
                new MockField("Attachment", null, false, null, "1 screen"),
                new MockField("Comment", null, false, "Default Text Renderer", "No screens"),
                new MockField("Component/s", null, false, "Autocomplete Renderer", "1 screen"),
                new MockField("Description", null, false, "Default Text Renderer", "1 screen"),
                new MockField("Due Date", null, false, null, "1 screen"),
                new MockField("Environment", "For example operating system, software platform and/or hardware specifications (include as appropriate for the issue).",
                        false, "Default Text Renderer", "1 screen"),
                new MockField("Fix Version/s", null, false, "Autocomplete Renderer", "2 screens"),
                new MockField("Issue Type", null, true, null, "1 screen"),
                new MockField("Labels", null, false, null, "1 screen"),
                new MockField("Linked Issues", null, false, null, "No screens"),
                new MockField("Priority", null, false, null, "1 screen"),
                new MockField("Reporter", null, true, null, "1 screen"),
                new MockField("Resolution", null, false, null, "1 screen"),
                new MockField("Security Level", null, false, null, "1 screen"),
                new MockField("Summary", null, true, null, "1 screen")
        );
    }

    private List<Field> getXSSFields()
    {
          return Lists.<Field>newArrayList(
                new MockField("<strong>Oh noes</strong>", "Oh noes", false, "Default Text Renderer", "3 screens"),
                new MockField("Affects Version/s", null, false, "Autocomplete Renderer", "1 screen"),
                new MockField("Assignee", null, false, null, "3 screens"),
                new MockField("Comment", null, false, "Default Text Renderer", "No screens"),
                new MockField("Component/s", null, false, "Autocomplete Renderer", "1 screen"),
                new MockField("Description", null, false, "Default Text Renderer", "1 screen"),
                new MockField("Due Date", null, false, null, "1 screen"),
                new MockField("Environment", "For example operating system, software platform and/or hardware specifications (include as appropriate for the issue).",
                        false, "Default Text Renderer", "1 screen"),
                new MockField("Fix Version/s", null, false, "Autocomplete Renderer", "2 screens"),
                new MockField("Issue Type", null, true, null, "1 screen"),
                new MockField("Labels", null, false, null, "1 screen"),
                new MockField("Linked Issues", null, false, null, "No screens"),
                new MockField("Priority", null, false, null, "1 screen"),
                new MockField("Reporter", null, true, null, "1 screen"),
                new MockField("Resolution", null, false, null, "1 screen"),
                new MockField("Security Level", null, false, null, "1 screen"),
                new MockField("Summary", null, true, null, "1 screen")
        );
    }

    private boolean canConfigureScheme(final FieldsPanel fieldsPanel, final String schemeName)
    {
        final ConfigureFieldConfigurationSchemePage editFieldSchemePage = fieldsPanel.gotoConfigureFieldConfigurationScheme();
        return editFieldSchemePage.getName().equals(schemeName);
    }

    private boolean canChangeScheme(final FieldsPanel fieldsPanel, final String projectName)
    {
        final ChangeFieldSchemePage changeFieldSchemePage = fieldsPanel.gotoChangeConfigScheme();
        return changeFieldSchemePage.getProjectName().equals(projectName);
    }

    private boolean canEditSchemeWithNoSchemesPresent(final FieldsPanel fieldsPanel)
    {
        final EditDefaultFieldConfigPage editDefaultFieldConfigPage = fieldsPanel.gotoEditDefaultFieldConfigScheme();
        return editDefaultFieldConfigPage.getName().equals("Default Field Configuration");
    }

}
