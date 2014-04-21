/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 14, 2004
 * Time: 5:04:19 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.dynamic.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWorkflowUtil extends LegacyJiraMockTestCase
{
    public void testAddtoExistingInputs()
    {
        List existingList = new ArrayList();
        existingList.add("item1");
        existingList.add("item2");

        Map inputs = EasyMap.build("Test Key", existingList);

        List toAdd = new ArrayList();
        toAdd.add("item3");
        toAdd.add("item4");

        WorkflowUtil.addToExistingTransientArgs(inputs, "Test Key", toAdd);
        existingList.addAll(toAdd);
        assertEquals(existingList, inputs.get("Test Key"));
    }

    public void testAddtoEmtpyListInputs()
    {
        Map inputs = new HashMap();
        List toAdd = new ArrayList();
        toAdd.add("item3");
        toAdd.add("item4");

        WorkflowUtil.addToExistingTransientArgs(inputs, "Test Key", toAdd);
        assertEquals(toAdd, inputs.get("Test Key"));
    }

    public void testGetWorkflowDisplayNameWithNullWorkflow()
    {
        String displayName = WorkflowUtil.getWorkflowDisplayName(null);
        assertNull(displayName);
    }

    public void testGetWorkflowDisplayNameNormalWorkflow()
    {
        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        mockJiraWorkflow.expectAndReturn("getName", "Gregory");
        String displayName = WorkflowUtil.getWorkflowDisplayName((JiraWorkflow) mockJiraWorkflow.proxy());
        assertEquals("Gregory", displayName);
        mockJiraWorkflow.verify();
    }
    
    public void testGetWorkflowDisplayNameDraftWorkflow()
    {
        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
        mockJiraWorkflow.expectAndReturn("getName", "Gregory");
        String displayName = WorkflowUtil.getWorkflowDisplayName((JiraWorkflow) mockJiraWorkflow.proxy());
        assertEquals("Gregory (Draft)", displayName);
        mockJiraWorkflow.verify();
    }
}