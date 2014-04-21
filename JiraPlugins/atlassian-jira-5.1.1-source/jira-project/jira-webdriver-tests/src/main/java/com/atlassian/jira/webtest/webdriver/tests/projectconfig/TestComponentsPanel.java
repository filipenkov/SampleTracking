package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.project.components.Component;
import com.atlassian.jira.pageobjects.project.components.ComponentsPageTab;
import com.atlassian.jira.pageobjects.project.components.DeleteComponentDialog;
import com.atlassian.jira.pageobjects.project.components.EditComponentForm;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the components panel.
 *
 * @since v4.4
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@Restore ("xml/ComponentsConfig.xml")
public class TestComponentsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_NO_COMPONENTS = "MKY";
    private static final String PROJECT_WITH_COMPONENTS = "HSP";

    @Test
    public void testTabNavigation()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_NO_COMPONENTS);
        ComponentsPageTab componentsPage = config.getTabs().gotoComponentsTab();
        assertTrue(config.getTabs().isComponentsTabSelected());
        assertTrue(componentsPage.getComponents().isEmpty());
        assertEquals(PROJECT_NO_COMPONENTS, componentsPage.getProjectKey());
    }

    @Test
    public void testNoComponent()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, PROJECT_NO_COMPONENTS);
        assertTrue(componentsPage.getComponents().isEmpty());
        assertTrue(componentsPage.hasEmptyMessage());
    }

    @Test
    public void testWithComponents()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, PROJECT_WITH_COMPONENTS);
        assertFalse(componentsPage.hasEmptyMessage());

        List<Component> components = Lists.newArrayList();

        components.add(new Component("1000").setName("<b>XSS</b>").setDescription("<b>Im an XSS DescriptionK</b>")
                .setLead(new User("admin", "Administrator")).setComponentAssigneeType(Component.ComponentAssigneeType.COMPONENT_LEAD));
        components.add(new Component("1001").setName("New Component 1")
                .setComponentAssigneeType(Component.ComponentAssigneeType.PROJECT_DEFAULT)
                .setLead(new User("'><b>reallybaduser</b>", "<b>Really Bad User</b>")));
        components.add(new Component("1002").setName("New Component 2")
                .setComponentAssigneeType(Component.ComponentAssigneeType.PROJECT_LEAD));
        components.add(new Component("1003").setName("New Component 3")
                .setLead(new User("fred", "Fred Normal"))
                .setComponentAssigneeType(Component.ComponentAssigneeType.UNASSIGNED));

        assertComponentsEquals(components, componentsPage.getComponents());
    }

    @Test
    public void testCreateComponent()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, PROJECT_WITH_COMPONENTS);

        EditComponentForm createComponentForm = componentsPage.getCreateComponentForm()
                .fill("New component 1", "I am new component 1", "admin", "PROJECT_LEAD")
                .submit();

        assertEquals("A component with the name New component 1 already exists in this project.",
                createComponentForm.getNameField().getError());

        assertNull("Expected no error for description field",
                createComponentForm.getDescriptionField().getError());

        assertNull("Expected no error for component lead field",
                createComponentForm.getComponentLeadField().getError());

        assertNull("Expected no error for default assignee field",
                createComponentForm.getDefaultAssigneeField().getError());

        createComponentForm = componentsPage.getCreateComponentForm()
                .fill("New component 1", "I am new component 1", "admin", "Project Lead")
                .submit();

        createComponentForm.fill("Really New component 1", "I am new component 1", "admin", "PROJECT_LEAD")
                .submit();

        assertNull("Expected no error for name field",
                createComponentForm.getNameField().getError());

        Component newComponent = componentsPage.getComponentByName("Really New component 1");

        assertNotNull(newComponent);

        assertEquals("Really New component 1", newComponent.getName());
        assertEquals("I am new component 1", newComponent.getDescription());
        assertEquals("Administrator", newComponent.getLeadLink().getUser().getFullName());
        assertEquals(Component.ComponentAssigneeType.PROJECT_LEAD, newComponent.getComponentAssigneeType());
    }

    @Test
    @Restore("xml/NoBrowseUsersPermission.xml")
    public void testUserPickerDisabled()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, "HSP");

        EditComponentForm createForm = componentsPage.getCreateComponentForm();

        assertTrue("Expected lead picker to be disabled",
                createForm.isLeadpickerDisabled());

        createForm.fill("Scott's component", "Some description", "fred", "COMPONENT_LEAD")
                .submit();


        Component newComponent = componentsPage.getComponentByName("Scott's component");

        assertEquals("Fred Normal", newComponent.getLead().getFullName());
    }


    @Test
    public void testEditComponent()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, PROJECT_WITH_COMPONENTS);

        EditComponentForm editComponentForm = componentsPage.getComponentByName("New Component 1")
                .edit("name")
                .fill("New Component 3", null, "admin", "COMPONENT_LEAD")
                .submit();

        assertEquals(editComponentForm.getNameField().getError(),
                "A component with the name New Component 3 already exists in this project.");

        editComponentForm.fill("Changed New Component 1", "A new description", "admin", "COMPONENT_LEAD")
                .submit();

        Component changedComponent = componentsPage.getComponentByName("Changed New Component 1");

        assertEquals("Changed New Component 1", changedComponent.getName());
        assertEquals("A new description", changedComponent.getDescription());
        assertEquals("Administrator", changedComponent.getLeadLink().getUser().getFullName());
        assertEquals(Component.ComponentAssigneeType.COMPONENT_LEAD, changedComponent.getComponentAssigneeType());


        componentsPage.getComponentByName("New Component 3")
                .edit("name")
                .fill("New Component 3", "", "admin", "Unassigned")
                .submit();

        changedComponent = componentsPage.getComponentByName("New Component 3");

        assertEquals("admin", changedComponent.getLead().getUserName());

        componentsPage.getComponentByName("New Component 3")
                .edit("name")
                .fill("New Component 3", "", "", "Unassigned")
                .submit();

        changedComponent = componentsPage.getComponentByName("New Component 3");

        assertNull("Expected component lead to be removed", changedComponent.getLead());

    }

    @Test
    @Restore ("xml/BadAssigneeTypeAndLead.xml")
    public void testInvalidComponentLead()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, "BAIN");

        final Component component = componentsPage.getComponentByName("Bad Lead");

        assertTrue("Expected component lead [mark] to be invalid as the LDAP directory he existed in is offline",
                component.hasInvalidLead());
    }

    @Test
    public void testDeleteComponent()
    {

        // New Component 1 removal (1 related issues)

        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, "DEL");

        DeleteComponentDialog deleteComponentDialog = componentsPage.getComponentByName("New Component 1")
                .delete();

        assertTrue("Expected delete component dialog to have swap component operation",
                deleteComponentDialog.hasComponentSwapOperation());

        assertTrue("Expected delete component dialog to have remove component operation",
                deleteComponentDialog.hasComponentRemoveOperation());

        assertEquals("Expected issues with component to be 1", 1,
                deleteComponentDialog.getIssuesInComponentCount());

        deleteComponentDialog.setSwapComponent("New Component 3");

        deleteComponentDialog.submit();

        assertNull("Expected New Component 1 to be removed",
                componentsPage.getComponentByName("New Component 1"));

        AdvancedSearch advancedSearch = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("project = \"Delete Component\" AND component=\"New Component 1\"")
                .submit();

        assertEquals("No matching issues found.",
                advancedSearch.getJQLInfo());

        advancedSearch = advancedSearch.enterQuery("project =\"Delete Component\" AND component=\"New Component 3\"").submit();

        assertEquals("Expected the issues from New Component 1 to be swapped to New Component 3", 1,
                advancedSearch.getResults().getTotalCount());

        componentsPage = pageBinder.navigateToAndBind(ComponentsPageTab.class, "DEL");

        assertNull("Expected New Component 1 to be removed",
                componentsPage.getComponentByName("New Component 1"));


        // New Component 2 removal (No related issues)

        deleteComponentDialog = componentsPage.getComponentByName("New Component 2").delete();

        assertEquals("There are no issues that use this component. It is safe to delete.",
                deleteComponentDialog.getInfoMessage());

        assertFalse("Expected delete component dialog NOT to have swap component operation",
                deleteComponentDialog.hasComponentSwapOperation());

        assertFalse("Expected delete component dialog NOT to have remove component operation",
                deleteComponentDialog.hasComponentRemoveOperation());

        componentsPage = deleteComponentDialog.submit();

        assertNull("Expected New Component 2 to be removed",
                componentsPage.getComponentByName("New Component 2"));


        // New Component 3 removal (1 related issues)

        deleteComponentDialog = componentsPage.getComponentByName("New Component 3").delete();

        assertFalse("Should be no remove operation as there are no other components",
                deleteComponentDialog.hasComponentRemoveOperation());

        assertFalse("Should be no swap operation as there are no other components",
                deleteComponentDialog.hasComponentSwapOperation());

        componentsPage = deleteComponentDialog.submit();

        assertNull("Expected New Component 3 to be removed",
                componentsPage.getComponentByName("New Component 3"));

        assertTrue("Expected message indicating there are no components",
                componentsPage.hasEmptyMessage());

        advancedSearch = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("project = \"Delete Component\" AND component is not EMPTY")
                .submit();

        assertEquals("Expected all component values to be removed from all issues in project", "No matching issues found.",
                advancedSearch.getJQLInfo());

    }

    @Test
    @Restore ("xml/BadAssigneeTypeAndLead.xml")
    public void testInvalidAssigneeType()
    {
        ComponentsPageTab componentsPage = jira.gotoLoginPage().loginAsSysAdmin(ComponentsPageTab.class, "FRH");

        assertTrue("Expected Component 1 to be invalid assignee as Component Lead is not assignable",
                componentsPage.getComponentByName("Component 1").hasInvalidAssignee());

        assertTrue("Expected Component 2 to be invalid assignee as Project Lead is not assignable",
                componentsPage.getComponentByName("Component 2").hasInvalidAssignee());

         assertTrue("Expected Component 2 to be invalid assignee as Project Default is not assignable",
                componentsPage.getComponentByName("Component 3").hasInvalidAssignee());

        assertTrue("Expected Component 4 to be invalid assignee as un-assigned issues are not allowed",
                componentsPage.getComponentByName("Component 4").hasInvalidAssignee());
    }

    private void assertComponentsEquals(List<Component> expectedComponent, List<Component> actualComponents)
    {
        ListIterator<Component> actualIter = actualComponents.listIterator();
        ListIterator<Component> expectedIter = expectedComponent.listIterator();

        while (expectedIter.hasNext())
        {
            int index = expectedIter.nextIndex();
            if (!actualIter.hasNext())
            {
                fail("More expected than actual components. Extra components: "
                        + actualComponents.subList(index, expectedComponent.size()));
            }
            Component expected = expectedIter.next();
            Component actual = actualIter.next();

            assertEqualsComponent(expected, actual, index);
        }

        if (actualIter.hasNext())
        {
            fail("More actual than expected components. Extra components: "
                    + actualComponents.subList(actualIter.nextIndex(), actualComponents.size()));
        }
    }

    private void assertEqualsComponent(Component expected, Component actual, int index)
    {
        assertEquals(String.format("Component name does not match [%d].", index),
                expected.getName(), actual.getName());

        assertEquals(String.format("Component description does not match [%d].", index),
                expected.getDescription(), actual.getDescription());

        assertEquals(String.format("Component user does not match [%d].", index),
                expected.getLead(), actual.getLead());

        assertEquals(String.format("Component assignee type does not match [%d].", index),
                expected.getComponentAssigneeType(), actual.getComponentAssigneeType());

        if (expected.getLead() != null)
        {
            assertTrue(String.format("Component should have user hover [%d].", index),
                    actual.getLeadLink().isHoverLink());
        }
    }
}
