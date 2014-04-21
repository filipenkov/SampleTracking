package com.atlassian.jira.workflow;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @since v3.13 */
public class TestCachingWorkflowDescriptorStore extends ListeningTestCase
{
    private static final String WORKFLOW_NAME = "jira";

    @Test
    public void testGetWorkflow() throws FactoryException
    {
        Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        mockWorkflowDescriptorStore.expectAndReturn("getAllJiraWorkflowDTOs", Collections.EMPTY_LIST);

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore((WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy(), null);

        try
        {
            cachingWorkflowDescriptorStore.getWorkflow(null);
            fail("getWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        //this should not call through to the delegate,as this is an eager loading cache. The mock will complain if it
        //does call a method.
        cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME);
    }

    @Test
    public void testRemoveWorkflow() throws FactoryException
    {
        Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        final JiraWorkflowDTOImpl dto = new JiraWorkflowDTOImpl(null, WORKFLOW_NAME, workflowDescriptor);
        mockWorkflowDescriptorStore.expectAndReturn("getAllJiraWorkflowDTOs", EasyList.build(dto));
        mockWorkflowDescriptorStore.expectAndReturn("removeWorkflow", new Constraint[] { P.eq(WORKFLOW_NAME) }, Boolean.TRUE);

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore((WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy(), null);

        WorkflowDescriptor cachedDescriptor = cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME);
        //check the cached dto is the same as the one we just eagerly loaded.
        assertEquals(dto.getDescriptor(), cachedDescriptor);

        try
        {
            cachingWorkflowDescriptorStore.removeWorkflow(null);
            fail("removeWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        boolean removed = cachingWorkflowDescriptorStore.removeWorkflow(WORKFLOW_NAME);
        assertTrue(removed);

        assertNull(cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
    }

    @Test
    public void testSaveWorkflow() throws FactoryException
    {
        Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        mockWorkflowDescriptorStore.expectAndReturn("getAllJiraWorkflowDTOs", Collections.EMPTY_LIST);
        mockWorkflowDescriptorStore.expectAndReturn("saveWorkflow", new Constraint[] { P.eq(WORKFLOW_NAME), P.eq(workflowDescriptor), P.eq(Boolean.TRUE) }, Boolean.TRUE);
        ImmutableWorkflowDescriptor immutableDescriptor = new ImmutableWorkflowDescriptor(workflowDescriptor);
        mockWorkflowDescriptorStore.expectAndReturn("getWorkflow", new Constraint[] { P.eq(WORKFLOW_NAME)}, immutableDescriptor);

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore((WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy(), null);

        WorkflowDescriptor cachedWorkflowDescriptor= cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME);
        //check that there's no cached DTO.
        assertNull(cachedWorkflowDescriptor);

        try
        {
            cachingWorkflowDescriptorStore.saveWorkflow(null, null, true);
            fail("saveWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        boolean saved = cachingWorkflowDescriptorStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, true);
        assertTrue(saved);

        //the cached dto should be equal to the one we just saved.
        assertNotNull(cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
        //The cached object should not be the same as the original descriptor. It should be wrapped by
        //an immutable wrapper.
        assertEquals(immutableDescriptor, cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
    }

    @Test
    public void testGetWorkflowNames() throws FactoryException
    {
        Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        final JiraWorkflowDTOImpl dto = new JiraWorkflowDTOImpl(null, WORKFLOW_NAME, workflowDescriptor);
        final JiraWorkflowDTOImpl dto2 = new JiraWorkflowDTOImpl(null, "AnotherWorkflow", workflowDescriptor);
        mockWorkflowDescriptorStore.expectAndReturn("getAllJiraWorkflowDTOs", EasyList.build(dto, dto2));

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore((WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy(), null);

        String[] workflowNames = cachingWorkflowDescriptorStore.getWorkflowNames();
        assertEquals(2, workflowNames.length);
        List workflowNameList = Arrays.asList(workflowNames);
        assertTrue(workflowNameList.contains(WORKFLOW_NAME));
        assertTrue(workflowNameList.contains("AnotherWorkflow"));
    }

    @Test
    public void testGetWorkflowDescriptors() throws FactoryException
    {
        Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        final WorkflowDescriptor descriptor = new WorkflowDescriptor();

        final JiraWorkflowDTOImpl dto = new JiraWorkflowDTOImpl(null, WORKFLOW_NAME, descriptor);
        final WorkflowDescriptor descriptor2 = new WorkflowDescriptor();
        final JiraWorkflowDTOImpl dto2 = new JiraWorkflowDTOImpl(null, "AnotherWorkflow", descriptor2);
        mockWorkflowDescriptorStore.expectAndReturn("getAllJiraWorkflowDTOs", EasyList.build(dto, dto2));

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore((WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy(), null);

        List<JiraWorkflowDTO> workflowDescriptors = cachingWorkflowDescriptorStore.getAllJiraWorkflowDTOs();
        assertEquals(2, workflowDescriptors.size());
        assertTrue(workflowDescriptors.contains(dto));
        assertTrue(workflowDescriptors.contains(dto2));
    }
}
