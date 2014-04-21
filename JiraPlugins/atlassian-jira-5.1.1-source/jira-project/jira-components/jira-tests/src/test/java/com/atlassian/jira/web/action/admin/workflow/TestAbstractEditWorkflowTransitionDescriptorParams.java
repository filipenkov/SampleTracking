package com.atlassian.jira.web.action.admin.workflow;

import org.junit.Test;
import static org.junit.Assert.*;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for the AbstractAddWorkflowTransitionDescriptorParams class.
 */
public class TestAbstractEditWorkflowTransitionDescriptorParams extends ListeningTestCase
{
    /**
     * Mock class only used to instantiate AbstractEditWorkflowTransitionDescriptorParams
     * so it can be tested. None of the methods work.
     */
    private final class MockChangeWorkflowTransitionDescriptorParams extends AbstractEditWorkflowTransitionDescriptorParams
    {

        ArrayList errors = new ArrayList();

        public MockChangeWorkflowTransitionDescriptorParams()
        {
            super(null, null, null, null);
        }

        protected Class getWorkflowModuleDescriptorClass()
        {
            return null;
        }

        protected void addWorkflowDescriptor()
        {

        }

        public String getWorkflowDescriptorName()
        {
            return null;
        }

        public String getText(String key)
        {
            return key;
        }

        public void addErrorMessage(String string)
        {
            errors.add(string);
        }

        ////////////////////////////////////////

        protected String getPluginType()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        protected void setupWorkflowDescriptor()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        protected String getHighLightParamPrefix()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        protected void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    @Test
    public void testSetupWorkflowDescriptorParams()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        assertTrue(adder.errors.isEmpty());
        adder.setupWorkflowDescriptorParams(new HashMap());
        Map params = adder.getDescriptorParams();
        assertTrue(params.isEmpty());
        assertEquals(4, adder.errors.size());
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.name"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.step"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.transition"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.count"));
    }
}
