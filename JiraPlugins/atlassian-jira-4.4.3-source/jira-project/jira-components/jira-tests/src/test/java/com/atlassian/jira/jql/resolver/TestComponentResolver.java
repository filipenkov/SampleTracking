package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestComponentResolver extends MockControllerTestCase
{
    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.findByComponentNameCaseInSensitive("component1");

        final MockProjectComponent mockComponent1 = new MockProjectComponent(1L, "component1");
        final MockProjectComponent mockComponent2 = new MockProjectComponent(2L, "component1");
        mockController.setReturnValue(CollectionBuilder.newBuilder(mockComponent1, mockComponent2).asList());
        mockController.replay();

        final List<String> result = resolver.getIdsFromName("component1");
        assertEquals(2, result.size());
        assertTrue(result.contains(mockComponent1.getId().toString()));
        assertTrue(result.contains(mockComponent2.getId().toString()));
        mockController.verify();
    }

    @Test
    public void testGetIdsFromNameDoesntExist() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.findByComponentNameCaseInSensitive("abc");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIdsFromName("abc");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        final MockProjectComponent mockComponent = new MockProjectComponent(2L, "component1");

        projectComponentManager.find(2L);
        mockController.setReturnValue(mockComponent);
        mockController.replay();

        final ProjectComponent result = resolver.get(2L);
        assertEquals(mockComponent, result);
        mockController.verify();
    }

    @Test
    public void testGetIdDoesntExist() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.find(100L);
        mockController.setReturnValue(null);
        mockController.replay();

        final ProjectComponent result = resolver.get(100L);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testGetIdDoesntException() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.find(100L);
        mockController.setThrowable(new EntityNotFoundException());
        mockController.replay();

        final ProjectComponent result = resolver.get(100L);
        assertNull(result);
        mockController.verify();
    }

    @Test
    public void testNameExists() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.findByComponentNameCaseInSensitive("name");
        mockController.setReturnValue(CollectionBuilder.newBuilder(new MockProjectComponent(1000L, "name")).asList());
        projectComponentManager.findByComponentNameCaseInSensitive("noname");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        assertTrue(resolver.nameExists("name"));
        assertFalse(resolver.nameExists("noname"));
        mockController.verify();
    }

    @Test
    public void testIdExists() throws Exception
    {
        final ProjectComponentManager projectComponentManager = mockController.getMock(ProjectComponentManager.class);

        ComponentResolver resolver = new ComponentResolver(projectComponentManager);

        projectComponentManager.find(10L);
        mockController.setReturnValue(new MockProjectComponent(1000L, "name"));
        projectComponentManager.find(11L);
        mockController.setReturnValue(null);
        mockController.replay();

        assertTrue(resolver.idExists(10L));
        assertFalse(resolver.idExists(11L));
        mockController.verify();
    }

}
