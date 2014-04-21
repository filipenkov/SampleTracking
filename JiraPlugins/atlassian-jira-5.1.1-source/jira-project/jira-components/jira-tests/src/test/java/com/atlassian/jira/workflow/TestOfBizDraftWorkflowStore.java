package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;

/**
 *
 */
public class TestOfBizDraftWorkflowStore extends LegacyJiraMockTestCase
{
    private static final String PARENT_WORKFLOW_NAME = "testWorkflow";
    private static final String PARENT_WORKFLOW_CONTENTS =
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
            "  </steps>\n";

    private static final String PARENT_WORKFLOW_DESCRIPTOR_XML =
            "<workflow>\n" + PARENT_WORKFLOW_CONTENTS + "</workflow>\n";

    public static final String WORKFLOW_XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.7//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_7.dtd\">\n";

    private static final String PARENT_WORKFLOW_DESCRIPTOR = WORKFLOW_XML_HEADER + PARENT_WORKFLOW_DESCRIPTOR_XML;

    public void testGetDraftWorkflow()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        JiraWorkflow draftWorkflow = ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME);

        assertNotNull(draftWorkflow);
        assertTrue(draftWorkflow instanceof JiraDraftWorkflow);
        final WorkflowDescriptor descriptor = draftWorkflow.getDescriptor();

        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, descriptor.asXML());
    }

    public void testGetDraftWorkflowWithNoDraftWorkflows()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        final JiraWorkflow workflow = ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME);
        assertNull(workflow);
    }

    public void testGetDraftWorkflowGVWithManyDraftWorkflows()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizDraftWorkflowStore.getDraftWorkflowGV(PARENT_WORKFLOW_NAME);
            fail("No exception was thrown");
        }
        catch (IllegalStateException e)
        {
            //should throw an exception
        }
    }

    public void testGetDraftWorkflowGVWithNullName()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);
        try
        {
            ofBizDraftWorkflowStore.getDraftWorkflowGV(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Can not get a draft workflow for a parent workflow name of null.", e.getMessage());
        }
    }

    public void testGetDraftWorkflowGVNoneFound()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        assertNull(ofBizDraftWorkflowStore.getDraftWorkflowGV(PARENT_WORKFLOW_NAME));
    }

    public void testAddDraftWorkflow() throws FactoryException
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class))
        {
            boolean draftWorkflowExistsForParent(Long parentWorkflowId)
            {
                return false;
            }
        };

        //This should really be a ConfigurableJiraWorkflow, but since the standard source release build doesn't include
        //that class, the source release build will fail.
        JiraWorkflow parentWorkflow = new AbstractJiraWorkflow(null, WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR))
        {

            public String getName()
            {
                return PARENT_WORKFLOW_NAME;
            }

            public boolean isDraftWorkflow()
            {
                return false;
            }

            public void reset()
            {
                //nothing
            }
        };
        JiraWorkflow draftWorkflow = ofBizDraftWorkflowStore.createDraftWorkflow("testuser", parentWorkflow);

        assertNotNull(draftWorkflow);
        assertTrue(draftWorkflow instanceof JiraDraftWorkflow);
        final WorkflowDescriptor descriptor = draftWorkflow.getDescriptor();

        //timestamps are really hard to test. So, lets remove it.
        descriptor.getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATED_DATE);
        //we're also removing the user attribute so that we can do a clean XML comparison below. But we at least
        //check that it's the right user :)
        assertEquals("testuser", descriptor.getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME));
        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, descriptor.asXML());
    }

    public void testDraftWorkflowWithNullUsername()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        try
        {
            ofBizDraftWorkflowStore.createDraftWorkflow(null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //should have gotten this exception since null username is an illegal argument.
        }
    }

    public void testDraftWorkflowDoesNotExistsForParent()
    {
        final MockControl mockOfBizDelegatorControl = MockControl.createControl(OfBizDelegator.class);
        final OfBizDelegator mockOfBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();

        mockOfBizDelegator.findByAnd(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME, EasyMap.build("parentname", PARENT_WORKFLOW_NAME));
        mockOfBizDelegatorControl.setReturnValue(Collections.EMPTY_LIST);

        mockOfBizDelegatorControl.replay();

        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(mockOfBizDelegator);
        assertFalse(ofBizDraftWorkflowStore.draftWorkflowExistsForParent(PARENT_WORKFLOW_NAME));

        mockOfBizDelegatorControl.verify();
    }

    public void testDraftWorkflowExistsForParent()
    {
        final MockControl mockOfBizDelegatorControl = MockControl.createControl(OfBizDelegator.class);
        final OfBizDelegator mockOfBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();

        mockOfBizDelegator.findByAnd(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME, EasyMap.build("parentname", PARENT_WORKFLOW_NAME));
        mockOfBizDelegatorControl.setReturnValue(EasyList.build(new MockGenericValue("DraftWorkflow",
                EasyMap.build("id", new Long(10000),
                        "parentid", PARENT_WORKFLOW_NAME,
                        "descriptor", PARENT_WORKFLOW_DESCRIPTOR))));

        mockOfBizDelegatorControl.replay();

        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(mockOfBizDelegator);
        assertTrue(ofBizDraftWorkflowStore.draftWorkflowExistsForParent(PARENT_WORKFLOW_NAME));

        mockOfBizDelegatorControl.verify();
    }

    public void testCreateDraftWorkflowNullWorkflowObject()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);
        try
        {
            ofBizDraftWorkflowStore.createDraftWorkflow("dude", null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // This should happen :)
        }
    }

    public void testCreateDraftWorkflowNullName()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);
        try
        {
            Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
            mockJiraWorkflow.expectAndReturn("getName", null);
            ofBizDraftWorkflowStore.createDraftWorkflow("dude", (JiraWorkflow) mockJiraWorkflow.proxy());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // This should happen :)
        }
    }

    public void testCreateDraftWorkflowNullDescriptor()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);
        try
        {
            Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
            mockJiraWorkflow.expectAndReturn("getName", "dude");
            mockJiraWorkflow.expectAndReturn("getDescriptor", null);
            ofBizDraftWorkflowStore.createDraftWorkflow("dude", (JiraWorkflow) mockJiraWorkflow.proxy());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // This should happen :)
        }
    }

    public void testDeleteDraftWorkflow()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertTrue(ofBizDraftWorkflowStore.deleteDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
    }

    public void testDeleteNonExistantDraftWorkflow()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));

        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertFalse(ofBizDraftWorkflowStore.deleteDraftWorkflow("someOtherWorkflow"));
        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
    }

    public void testDeleteDraftWorflowWithNullParent()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);

        try
        {
            ofBizDraftWorkflowStore.deleteDraftWorkflow(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //happy
        }
    }

    public void testUpdateDraftWorkflowWithNullUpdatedWorkflow()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);

        try
        {
            ofBizDraftWorkflowStore.updateDraftWorkflow("testuser", PARENT_WORKFLOW_NAME, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Can not update a draft workflow with a null workflow/descriptor.", e.getMessage());
        }
    }

    public void testUpdateDraftWorkflowWithNullUpdatedWorkflowDescriptor()
    {
        JiraDraftWorkflow parentWorkflow = new JiraDraftWorkflow(null, null, null)
        {
            public void reset()
            {
                //nothing
            }
        };

        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null);

        try
        {
            ofBizDraftWorkflowStore.updateDraftWorkflow("testuser", PARENT_WORKFLOW_NAME, parentWorkflow);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Can not update a draft workflow with a null workflow/descriptor.", e.getMessage());
        }
    }

    public void testUpdateDraftWorkflowWithNonExistentParent() throws FactoryException
    {
        JiraDraftWorkflow parentWorkflow = new JiraDraftWorkflow(null, null, WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR))
        {
            public void reset()
            {
                //nothing
            }
        };
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(null)
        {
            GenericValue getDraftWorkflowGV(String parentWorkflowName)
            {
                return null;
            }
        };
        try
        {
            ofBizDraftWorkflowStore.updateDraftWorkflow("testuser", PARENT_WORKFLOW_NAME, parentWorkflow);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unable to find a draft workflow associated with the parent workflow name '" + PARENT_WORKFLOW_NAME + "'", e.getMessage());
        }
    }

    public void testUpdateDraftWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, "my bs descriptor"));

        JiraDraftWorkflow parentWorkflow = new JiraDraftWorkflow(null, null, WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR))
        {
            public void reset()
            {
                //nothing
            }
        };
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        final JiraWorkflow workflow = ofBizDraftWorkflowStore.updateDraftWorkflow("testuser", PARENT_WORKFLOW_NAME, parentWorkflow);
        //the timestamp is too hard to test.  I wont do it!
        workflow.getDescriptor().getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATED_DATE);
        assertEquals("testuser", workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME));
        workflow.getDescriptor().getMetaAttributes().remove(JiraDraftWorkflow.JIRA_META_UPDATE_AUTHOR_NAME);

        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, workflow.getDescriptor().asXML());
    }

    public void testUpdateDraftWorkflowWithNormalWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, "my bs descriptor"));

        //This should really be a ConfigurableJiraWorkflow, but since the standard source release build doesn't include
        //that class, the source release build will fail.
        JiraWorkflow parentWorkflow = new AbstractJiraWorkflow(null, WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR))
        {

            public String getName()
            {
                return null;
            }

            public boolean isDraftWorkflow()
            {
                return false;
            }

            public void reset()
            {
                //nothing
            }
        };
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizDraftWorkflowStore.updateDraftWorkflow("testuser", PARENT_WORKFLOW_NAME, parentWorkflow);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Only draft workflows may be updated via this method.", e.getMessage());
        }
    }

    public void testUpdateDraftWorkflowNullUsername()
    {
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        try
        {
            ofBizDraftWorkflowStore.updateDraftWorkflow(null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Can not update a draft workflow with a null username.", e.getMessage());
        }
    }

    public void testUpdateDraftWorkflowWithoutAudit() throws FactoryException
    {
        final String workflowString =
                "<workflow>\n"
                + "<meta name=\"jira.update.author.name\">admin</meta>\n"
                + "<meta name=\"jira.updated.date\">1256700109714</meta>\n"
                + PARENT_WORKFLOW_CONTENTS
                + "</workflow>\n";
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                EasyMap.build(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD,
                        WORKFLOW_XML_HEADER + workflowString));

        JiraDraftWorkflow parentWorkflow = new JiraDraftWorkflow(null, null, WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR))
        {
            public void reset()
            {
                //nothing
            }
        };
        OfBizDraftWorkflowStore ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(ComponentManager.getComponentInstanceOfType(OfBizDelegator.class));
        final JiraWorkflow workflow = ofBizDraftWorkflowStore.updateDraftWorkflowWithoutAudit(PARENT_WORKFLOW_NAME, parentWorkflow);
        assertEquals("1256700109714", workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATED_DATE));
        assertEquals("admin", workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME));

        assertEqualsIgnoreWhitespace(workflowString, workflow.getDescriptor().asXML());
    }

    private void assertEqualsIgnoreWhitespace(String original, String stringBeingTested)
    {
        assertEquals(StringUtils.deleteWhitespace(original), StringUtils.deleteWhitespace(stringBeingTested));
    }

}
