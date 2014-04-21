package com.atlassian.jira.web.action.issue.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.web.action.issue.MoveIssue;
import com.atlassian.jira.web.action.issue.TestMoveIssue;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.OSWorkflowManager;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.User;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

public class TestEnterpriseMoveIssue extends TestMoveIssue
{
    private GenericValue project3;

    public TestEnterpriseMoveIssue(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        project3 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(3), "name", "project3"));
    }

    public void testGetAllowedProjectsWorkflowRestrictions() throws Exception
    {
        MoveIssue mi = new MoveIssue(null, null, null, null, null, null, null, null, null);
        mi.setId(new Long(10));

        JiraTestUtil.loginUser(fred);

        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, null, GroupDropdown.DESC);
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project2, scheme);
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project3, scheme);

        // with default workflows, all should be allowed
        assertEquals(3, mi.getAllowedProjects().size());

        EventPublisher eventPublisher = EasyMock.createNiceMock(EventPublisher.class);
        WorkflowManager mockWorkflowManager = new OSWorkflowManager(null, eventPublisher)
        {

            public boolean isActive(JiraWorkflow workflow) throws WorkflowException
            {
                return false;
            }

            public boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
            {
                return true;
            }
        };

        // now give one of the projects a different workflow and assign it to the scheme
        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", mockWorkflowManager);
        final User testUser = new User("test", new MockProviderAccessor(), new MockCrowdService());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        GenericValue scheme = ManagerFactory.getWorkflowSchemeManager().createScheme("Test WF Scheme", null);
        ManagerFactory.getWorkflowSchemeManager().addWorkflowToScheme(scheme, workflow.getName(), "0");
        ManagerFactory.getWorkflowSchemeManager().addSchemeToProject(project3, scheme);

        // Both projects should be available - as it is possible to just change issue type
        assertEquals(3, mi.getAllowedProjects().size());
        assertTrue(mi.getAllowedProjects().contains(project2));
        assertTrue(mi.getAllowedProjects().contains(project3));
    }

    public void testDoValidationNoChanges() throws Exception
    {
        JiraTestUtil.loginUser(fred);

        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, "group3", GroupDropdown.DESC);
        ManagerFactory.getPermissionManager().addPermission(Permissions.MOVE_ISSUE, scheme, "group3", GroupDropdown.DESC);

        MoveIssue mi = new MoveIssue(null, null, null, null, null, ComponentAccessor.getFieldLayoutManager(), null, null, null);
        MoveIssueBean moveIssueBean = setupMoveIssueBean();
//        moveIssueBean.setTargetPid(project1.getLong("id"));
//        moveIssueBean.setTargetIssueType(issue.getString("type"));
        mi.setId(issue.getLong("id"));

        assertEquals(Action.INPUT, mi.execute());

        // TODO this is a NullPointerException error message, possibly not what we want to test for
        assertEquals(1, mi.getErrorMessages().size());
    }
}
