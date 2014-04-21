package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import java.util.List;

/**
 * @since v4.0
 */
public class TestProjectResolver extends MockControllerTestCase
{
    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        final MockProject mockProject = new MockProject(1L, "key", "name");

        expect(projectManager.getProjectObjByKeyIgnoreCase("name"))
                .andReturn(mockProject);
        replay();

        final List<String> result = resolver.getIdsFromName("name");
        assertEquals(1, result.size());
        assertTrue(result.contains(mockProject.getId().toString()));
    }

    @Test
    public void testGetIdsFromNameDoesntExistKeyDoes() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        final MockProject mockProject = new MockProject(1L, "key", "name");

        expect(projectManager.getProjectObjByKeyIgnoreCase("key"))
                .andReturn(null);
        expect(projectManager.getProjectObjByName("key"))
                .andReturn(mockProject);
        replay();

        final List<String> result = resolver.getIdsFromName("key");
        assertEquals(1, result.size());
        assertTrue(result.contains(mockProject.getId().toString()));
    }
    
    @Test
    public void testGetIdsFromNameAndKeyDoesntExist() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObjByKeyIgnoreCase("abc"))
                .andReturn(null);
        expect(projectManager.getProjectObjByName("abc"))
                .andReturn(null);
        replay();

        final List<String> result = resolver.getIdsFromName("abc");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        final MockProject mockProject = new MockProject(2L, "version1");

        expect(projectManager.getProjectObj(2L))
                .andReturn(mockProject);
        replay();

        final Project result = resolver.get(2L);
        assertEquals(mockProject, result);
    }

    @Test
    public void testGetIdDoesntExist() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObj(100L))
                .andReturn(null);
        replay();

        final Project result = resolver.get(100L);
        assertNull(result);
    }

    @Test
    public void testNameExistsAsName() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObjByKeyIgnoreCase("name"))
                .andReturn(new MockProject(1000, "name"));
        replay();

        assertTrue(resolver.nameExists("name"));
    }

    @Test
    public void testNameExistsAsKey() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObjByKeyIgnoreCase("name"))
                .andReturn(null);
        expect(projectManager.getProjectObjByName("name"))
                .andReturn(new MockProject(1000, "name"));
        replay();

        assertTrue(resolver.nameExists("name"));
    }
    
    @Test
    public void testNameAndKeyDoesntExist() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObjByKeyIgnoreCase("name"))
                .andReturn(null);
        expect(projectManager.getProjectObjByName("name"))
                .andReturn(null);
        replay();

        assertFalse(resolver.nameExists("name"));
    }

    @Test
    public void testIdExists() throws Exception
    {
        final ProjectManager projectManager = getMock(ProjectManager.class);

        ProjectResolver resolver = new ProjectResolver(projectManager);

        expect(projectManager.getProjectObj(10L))
                .andReturn(new MockProject(1000, "name"));
        expect(projectManager.getProjectObj(11L))
                .andReturn(null);
        replay();

        assertTrue(resolver.idExists(10L));
        assertFalse(resolver.idExists(11L));
    }
}
