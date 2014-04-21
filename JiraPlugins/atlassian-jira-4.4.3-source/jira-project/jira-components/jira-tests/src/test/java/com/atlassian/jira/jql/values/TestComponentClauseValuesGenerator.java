package com.atlassian.jira.jql.values;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.user.User;

import java.util.Locale;

/**
 * @since v4.0
 */
public class TestComponentClauseValuesGenerator extends MockControllerTestCase
{
    private ProjectComponentManager projectComponentManager;
    private ProjectManager projectManager;
    private PermissionManager permissionManager;
    private ComponentClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {

        projectComponentManager = mockController.getMock(ProjectComponentManager.class);
        projectManager = mockController.getMock(ProjectManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);

        valuesGenerator = new ComponentClauseValuesGenerator(projectComponentManager, projectManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(2L);
        mockController.setReturnValue(project2);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);

        assertEquals(5, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("C comp"));
        assertEquals(possibleValues.getResults().get(4), new ClauseValuesGenerator.Result("D comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");

        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "Aa comp", 10);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockProject project1 = new MockProject(1L, "TST", "Test");

        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "Aa comp blah", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "Aa comp", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("Aa comp blah"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoMatching() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "F", 10);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesSomeMatching() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        final MockProject project1 = new MockProject(1L, "TST", "Test");

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "a", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHitMax() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(2L);
        mockController.setReturnValue(project2);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 4);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("C comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesCompoentReferencesProjectDoesNotExist() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        projectManager.getProjectObj(1L);
        mockController.setReturnValue(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(null);
        projectManager.getProjectObj(2L);
        mockController.setReturnValue(project2);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(null);

        permissionManager.hasPermission(Permissions.BROWSE, project2, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);

        assertEquals(1, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("C comp"));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoPermForProject() throws Exception
    {
        final MockProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final MockProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final MockProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final MockProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final MockProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);

        final MockProject project1 = new MockProject(1L, "TST", "Test");
        final MockProject project2 = new MockProject(2L, "ANA", "Another");

        projectComponentManager.findAll();
        mockController.setReturnValue(CollectionBuilder.newBuilder(component5, component4, component3, component2, component1).asList());

        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);
        projectManager.getProjectObj(2L);
        mockController.setReturnValue(project2);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);
        permissionManager.hasPermission(Permissions.BROWSE, project2, null);
        mockController.setReturnValue(false);
        permissionManager.hasPermission(Permissions.BROWSE, project1, null);
        mockController.setReturnValue(true);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa comp"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A comp"));
        assertEquals(possibleValues.getResults().get(2), new ClauseValuesGenerator.Result("B comp"));
        assertEquals(possibleValues.getResults().get(3), new ClauseValuesGenerator.Result("D comp"));

        mockController.verify();
    }

}
