/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 14, 2004
 * Time: 5:04:19 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWorkflowUtil extends TestCase
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
        final JiraAuthenticationContext mockJiraAuthenticationContext = new MockSimpleAuthenticationContext(null, null, new MockI18nHelper());
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext));

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
        mockJiraWorkflow.expectAndReturn("getName", "Gregory");
        String displayName = WorkflowUtil.getWorkflowDisplayName((JiraWorkflow) mockJiraWorkflow.proxy());
        assertEquals("Gregory (common.words.draft)", displayName);
        mockJiraWorkflow.verify();
    }

    public void testInterpolateProjectKey() throws Exception
    {
        GenericValue gvProject = new MockGenericValue("Project", 12L);
        gvProject.setString("key", "TST");
        assertEquals(null, WorkflowUtil.interpolateProjectKey(null, null));
        assertEquals(null, WorkflowUtil.interpolateProjectKey(gvProject, null));
        assertEquals("blah", WorkflowUtil.interpolateProjectKey(gvProject, "blah"));
        assertEquals("blahTST", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}"));
        assertEquals("TSTblah", WorkflowUtil.interpolateProjectKey(gvProject, "${pkey}blah"));
        assertEquals("blahTSTblah", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}blah"));
        // Only works for first
        assertEquals("blahTSTblah${pkey}", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}blah${pkey}"));

        // Actually this is broken?
        assertEquals("blahTST", WorkflowUtil.interpolateProjectKey(gvProject, "blah${SOME RUBBISH}"));
    }

    public void testReplaceProjectKey() throws Exception
    {
        Project project = new MockProject(12L, "TST");
        assertEquals(null, WorkflowUtil.replaceProjectKey(null, null));
        assertEquals(null, WorkflowUtil.replaceProjectKey(project, null));
        assertEquals("blah", WorkflowUtil.replaceProjectKey(project, "blah"));
        assertEquals("blahTST", WorkflowUtil.replaceProjectKey(project, "blah${pkey}"));
        assertEquals("TSTblah", WorkflowUtil.replaceProjectKey(project, "${pkey}blah"));
        assertEquals("blahTSTblah", WorkflowUtil.replaceProjectKey(project, "blah${pkey}blah"));
        // Only works for first
        assertEquals("blahTSTblah${pkey}", WorkflowUtil.replaceProjectKey(project, "blah${pkey}blah${pkey}"));

        assertEquals("blah${SOME RUBBISH}", WorkflowUtil.replaceProjectKey(project, "blah${SOME RUBBISH}"));
    }
}