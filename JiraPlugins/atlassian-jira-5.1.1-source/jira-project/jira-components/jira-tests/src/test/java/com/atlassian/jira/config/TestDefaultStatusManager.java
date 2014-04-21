package com.atlassian.jira.config;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultStatusManager
{
    StatusManager statusManager;
    ConstantsManager constantsManager;
    OfBizDelegator ofBizDelegator;
    IssueIndexManager issueIndexManager;
    TranslationManager translationManager;
    JiraAuthenticationContext jiraAuthenticationContext;
    WorkflowManager workflowManager;

    @Before
    public void setUp()
    {
        constantsManager = Mockito.mock(ConstantsManager.class);
        translationManager = Mockito.mock(TranslationManager.class);
        jiraAuthenticationContext = Mockito.mock(JiraAuthenticationContext.class);
        issueIndexManager = Mockito.mock(IssueIndexManager.class);
        ofBizDelegator = Mockito.mock(OfBizDelegator.class);
        workflowManager = Mockito.mock(WorkflowManager.class);
        statusManager = new DefaultStatusManager(constantsManager, ofBizDelegator, issueIndexManager, translationManager, jiraAuthenticationContext, workflowManager)
        {
            @Override
            protected String getNextStringId() throws GenericEntityException
            {
                return "10";
            }

            @Override
            protected void removePropertySet(GenericValue
                    constantGv)
            {
                //DO NOTHING
            }
        };
    }

    @Test
    public void testCreateStatus() throws Exception
    {
        GenericValue statusOpenGV = new MockGenericValue("Status", 1l);
        statusOpenGV.set("sequence", Long.valueOf(1));
        Status statusOpen = new StatusImpl(statusOpenGV, translationManager, jiraAuthenticationContext);

        GenericValue statusReadyForQaGV = new MockGenericValue("Status", 10000l);
        statusReadyForQaGV.set("sequence", Long.valueOf(2));
        Status statusReadyForQa = new StatusImpl(statusReadyForQaGV, translationManager, jiraAuthenticationContext);
        statusReadyForQa.setName("Ready for QA");
        statusReadyForQa.setDescription("Issue is ready to be qa-ed");
        statusReadyForQa.setIconUrl("http://test");

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));
        when(ofBizDelegator.createValue(eq(ConstantsManager.STATUS_CONSTANT_TYPE), argThat(new StatusFieldsdArgumentMatcher("10000", "Ready for QA", "Issue is ready to be qa-ed", "http://test")))).thenReturn(statusReadyForQaGV);

        Status status = statusManager.createStatus("Ready for QA", "Issue is ready to be qa-ed", "http://test");
        Assert.assertEquals("10000", status.getId());
        Assert.assertEquals("Ready for QA", status.getName());
        Assert.assertEquals("Issue is ready to be qa-ed", status.getDescription());
        Assert.assertEquals("http://test", status.getIconUrl());

    }

    @Test
    public void testEditStatus() throws Exception
    {
        final BooleanHolder calledStored = new BooleanHolder();
        GenericValue statusOpenGV = new MockGenericValue("Status", 1l)
        {
            @Override
            public void store() throws GenericEntityException
            {
                calledStored.booleanValue = true;
            }
        };
        statusOpenGV.set("sequence", Long.valueOf(1));
        Status statusOpen = new StatusImpl(statusOpenGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));
        statusManager.editStatus(statusOpen, "New Status", null, "http://myurl.com");
        assertTrue(calledStored.booleanValue);
        assertEquals("New Status", statusOpen.getName());
        assertEquals(null, statusOpen.getDescription());
        assertEquals("http://myurl.com", statusOpen.getIconUrl());
    }

    @Test
    public void testRemoveStatus() throws Exception
    {
        final BooleanHolder removedOne = new BooleanHolder();
        GenericValue statusClosedGV = new MockGenericValue("Status", 2l)
        {
            @Override
            public void remove()
            {
                removedOne.booleanValue = true;
            }

            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                return Collections.emptyList();
            }
        };
        statusClosedGV.set("sequence", Long.valueOf(2));
        statusClosedGV.set("name", "Closed");
        statusClosedGV.set("description", "Issue has been closed");
        Status statusClosed = new StatusImpl(statusClosedGV, translationManager, jiraAuthenticationContext);

        GenericValue statusOpenGV = new MockGenericValue("Status", 1l);
        statusOpenGV.set("sequence", Long.valueOf(2));
        statusOpenGV.set("name", "Open");
        statusOpenGV.set("description", "Open waiting for development");

        when(constantsManager.getStatusObject("2")).thenReturn(statusClosed);
        JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getLinkedStatuses()).thenReturn(Lists.<GenericValue>newArrayList(statusOpenGV));
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(Lists.newArrayList(jiraWorkflow));
        statusManager.removeStatus("2");
        assertTrue(removedOne.booleanValue);
    }

    @Test
    public void testRemoveStatusExistsInWorkflow() throws Exception
    {
        final BooleanHolder removedOne = new BooleanHolder();
        GenericValue statusClosedGV = new MockGenericValue("Status", 2l)
        {
            @Override
            public void remove()
            {
                removedOne.booleanValue = true;
            }

            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                return Collections.emptyList();
            }
        };
        statusClosedGV.set("sequence", Long.valueOf(2));
        statusClosedGV.set("name", "Closed");
        statusClosedGV.set("description", "Issue has been closed");
        Status statusClosed = new StatusImpl(statusClosedGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getStatusObject("2")).thenReturn(statusClosed);
        JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getName()).thenReturn("My Custom Workflow");
        when(jiraWorkflow.getLinkedStatuses()).thenReturn(Lists.<GenericValue>newArrayList(statusClosedGV));
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(Lists.newArrayList(jiraWorkflow));
        try
        {
            statusManager.removeStatus("2");
            fail("Expected failure due to status is associated with a workflow.");
        }
        catch (IllegalStateException ex)
        {
            assertEquals("Cannot delete a status which is associated with a workflow. Status is associated with workflow My Custom Workflow", ex.getMessage());
        }
    }

    class StatusFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        final String id;
        private final String name;
        private final String descpription;
        private final String iconUrl;

        StatusFieldsdArgumentMatcher(String id, String name, String descpription, String iconUrl)
        {
            this.id = id;
            this.name = name;
            this.descpription = descpription;
            this.iconUrl = iconUrl;
        }

        public boolean matches(Object o)
        {
            Map<String, Object> gv = (Map<String, Object>) o;
            return id.equals(gv.get("id")) && name.equals(gv.get("name")) && descpription.equals(gv.get("description")) && iconUrl.equals(gv.get("iconurl"));
        }
    }

}
