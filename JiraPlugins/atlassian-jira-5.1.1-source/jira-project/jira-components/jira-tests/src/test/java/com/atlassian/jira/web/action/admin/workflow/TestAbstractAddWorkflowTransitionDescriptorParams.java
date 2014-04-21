package com.atlassian.jira.web.action.admin.workflow;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for the AbstractAddWorkflowTransitionDescriptorParams class.
 */
public class TestAbstractAddWorkflowTransitionDescriptorParams extends ListeningTestCase
{
    /**
     * Mock class only used to instantiate AbstractAddWorkflowTransitionDescriptorParams
     * so it can be tested. None of the methods work.
     */
    private final class MockChangeWorkflowTransitionDescriptorParams extends AbstractAddWorkflowTransitionDescriptorParams
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

    }


    @Test
    public void testSetupWorkflowDescriptorParams()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        assertTrue(adder.errors.isEmpty());
        adder.setupWorkflowDescriptorParams(new HashMap());
        Map params = adder.getDescriptorParams();
        assertTrue(params.isEmpty());
        assertEquals(6, adder.errors.size());
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.name"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.step"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.transition"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.cannot.find.plugin.module.key"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.count"));
        assertTrue(adder.errors.contains("admin.errors.workflows.cannot.find.nested"));
    }

    @Test
    public void testSetupWithStartingMap()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        Map existing = EasyMap.build("count", "admin.errors.workflows.cannot.find.count");
        assertTrue(existing.containsKey("count"));
        adder.setupWorkflowDescriptorParams(existing);
        assertEquals(5, adder.errors.size());
        Map params = adder.getDescriptorParams();
        assertFalse(params.containsKey("count"));
    }
}
