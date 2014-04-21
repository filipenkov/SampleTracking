package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class TestOfBizWorkflowDescriptorStore extends LegacyJiraMockTestCase
{
    private static final String WORKFLOW_NAME = "testWorkflow";
    private static final String WORKFLOW_DESCRIPTOR_XML =
            "<workflow>\n" +
                    "  <initial-actions>\n" +
                    "    <action id=\"1\" name=\"Create Issue\">\n" +
                    "      <results>\n" +
                    "        <unconditional-result old-status=\"Finished\" status=\"Open\" step=\"1\"/>\n" +
                    "      </results>\n" +
                    "    </action>\n" +
                    "  </initial-actions>\n" +
                    "  <steps>\n" +
                    "    <step id=\"1\" name=\"Open\">\n" +
                    "    </step>\n" +
                    "  </steps>\n" +
                    "</workflow>\n";

    private static final String WORKFLOW_DESCRIPTOR =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.7//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_7.dtd\">\n" +
                    WORKFLOW_DESCRIPTOR_XML;

    public void testGetWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        WorkflowDescriptor workflowDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);

        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, workflowDescriptor.asXML());
    }

    public void testGetWorkflowWithNoWorkflow() throws FactoryException
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        WorkflowDescriptor workflowDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);

        assertNull(workflowDescriptor);
    }

    public void testGetWorkflowWithNullName() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizWorkflowStore.getWorkflow(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //YAY!!
        }
    }

    public void testGetWorkflowWithMultipleWorkflows() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "ANOTHER DESCRIPTOR"));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (IllegalStateException e)
        {
            //YAY!!
        }
    }

    public void testRemoveWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "AnotherWorkflow",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        assertTrue(ofBizWorkflowStore.removeWorkflow(WORKFLOW_NAME));
        assertNull(ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME));
        assertNotNull(ofBizWorkflowStore.getWorkflow("AnotherWorkflow"));
    }

    public void testRemoveWorkflowWithNullName()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizWorkflowStore.removeWorkflow(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }

    public void testRemoveWorkflowNonExistant()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        assertFalse(ofBizWorkflowStore.removeWorkflow(WORKFLOW_NAME));
    }

    public void testSaveWorkflowCreate() throws FactoryException
    {
        final WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class))
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                assertEquals(workflowDescriptor, descriptor);
                return WORKFLOW_DESCRIPTOR;
            }
        };

        ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, false);
        WorkflowDescriptor storedDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, storedDescriptor.asXML());
    }

    public void testSaveWorkflowUpdate() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        final WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class))
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                assertEquals(workflowDescriptor, descriptor);
                return WORKFLOW_DESCRIPTOR;
            }
        };

        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is in a bad state in the db. Lets update it
        }

        ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, true);
        WorkflowDescriptor updatedDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
        assertNotNull(updatedDescriptor);
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, updatedDescriptor.asXML());
    }

    public void testSaveWorkflowNullName()
    {
        final WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizWorkflowStore.saveWorkflow(null, workflowDescriptor, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Workflow name cannot be null!", e.getMessage());
        }
    }

    public void testSaveWorkflowNullDescriptor()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            JiraWorkflowDTO newJiraWorkflowDTO = new JiraWorkflowDTOImpl(new Long(1000), WORKFLOW_NAME, null);
            ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, null, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Workflow descriptor cannot be null!", e.getMessage());
        }
    }

    public void testSaveWorkflowReplaceWithFalseReplace() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        final WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class))
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                if (!descriptor.equals(workflowDescriptor))
                {
                    throw new RuntimeException("workflowDescriptor to be converted to XML is not equal to original descriptor!");
                }
                return WORKFLOW_DESCRIPTOR;
            }
        };

        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is in a bad state in the db. Lets update it
        }

        assertFalse(ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, false));
        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is still in a bad state in the db
        }
    }

    public void testGetWorkflowNames()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 1",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 2",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 3",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        final String[] workflowNames = ofBizWorkflowStore.getWorkflowNames();
        final List workflowNamesList = Arrays.asList(workflowNames);
        assertTrue(workflowNamesList.contains("Name 1"));
        assertTrue(workflowNamesList.contains("Name 2"));
        assertTrue(workflowNamesList.contains("Name 3"));
    }

    public void testGetWorkflowNamesNoNames()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        assertEquals(0, ofBizWorkflowStore.getWorkflowNames().length);
    }

    public void testGetAllWorkflowDescriptors()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 1",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 2",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 3",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));

        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        final List workflowDescriptors = ofBizWorkflowStore.getAllJiraWorkflowDTOs();
        assertEquals(3, workflowDescriptors.size());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(0)).getDescriptor().asXML());
//        assertEquals("Name 1", ((JiraWorkflowDTO) workflowDescriptors.get(0)).getName());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(1)).getDescriptor().asXML());
//        assertEquals("Name 2", ((JiraWorkflowDTO) workflowDescriptors.get(1)).getName());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(2)).getDescriptor().asXML());
//        assertEquals("Name 3", ((JiraWorkflowDTO) workflowDescriptors.get(2)).getName());
    }

    public void testGetAllWorkflowDescriptorsWithNone()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        final List workflowDescriptors = ofBizWorkflowStore.getAllJiraWorkflowDTOs();
        assertEquals(0, workflowDescriptors.size());
    }

    private void assertEqualsIgnoreWhitespace(String original, String stringBeingTested)
    {
        assertEquals(StringUtils.deleteWhitespace(original), StringUtils.deleteWhitespace(stringBeingTested));
    }    
}
