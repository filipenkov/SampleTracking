/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.User;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class TestMoveIssueUpdateWorkflow extends AbstractUsersTestCase
{
    MoveIssueUpdateWorkflow miuw;

    private Mock issueLinkManager;
    private Mock subtaskManager;
    private Mock constantsManager;
    private Mock workflowManager;
    private Mock fieldLayoutManager;
    private Mock projectManager;
    private GenericValue issue1;
    private User testUser;
    private GenericValue project1;

    public TestMoveIssueUpdateWorkflow(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        issueLinkManager = new Mock(IssueLinkManager.class);
        subtaskManager = new Mock(SubTaskManager.class);
        constantsManager = new Mock(ConstantsManager.class);
        workflowManager = new Mock(WorkflowManager.class);
        fieldLayoutManager = new Mock(FieldLayoutManager.class);
        projectManager = new Mock(ProjectManager.class);

        // Cant mock out IssueManager
        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test source summary", "key", "TST-1", "id", new Long(10001), "project", new Long(1)));

        miuw = new MoveIssueUpdateWorkflow((IssueLinkManager) issueLinkManager.proxy(), (SubTaskManager) subtaskManager.proxy(), (ConstantsManager) constantsManager.proxy(),
                (WorkflowManager) workflowManager.proxy(), null, (FieldLayoutManager) fieldLayoutManager.proxy(), null, null, null);

        testUser = UtilsForTests.getTestUser("Test USer");
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
    }

    private MoveIssueBean setupMoveIssueBean()
    {
        MoveIssueBean moveIssueBean = new MoveIssueBean((ConstantsManager) constantsManager.proxy(), (ProjectManager) projectManager.proxy());
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, moveIssueBean);
        return moveIssueBean;
    }

    public void testDoValidationNoTargetPid() throws GenericEntityException, CreateException
    {
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.BROWSE);

        Map sessionMap = new HashMap();
        ActionContext.setSession(sessionMap);
        setupMoveIssueBean();
        JiraTestUtil.loginUser(testUser);

        miuw.setId(issue1.getLong("id"));
//        moveIssueBean.setTargetPid(null);
        miuw.doValidation();

        assertTrue(miuw.getHasErrorMessages());
        assertEquals(1, miuw.getErrorMessages().size());
    }

    public void testDoValidation() throws GenericEntityException, CreateException
    {
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.BROWSE);

        Map sessionMap = new HashMap();
        ActionContext.setSession(sessionMap);
        MoveIssueBean moveIssueBean = setupMoveIssueBean();
        JiraTestUtil.loginUser(testUser);

        miuw.setId(issue1.getLong("id"));
//        moveIssueBean.setTargetPid(new Long(1));
        moveIssueBean.setTargetStatusId("1");
        miuw.doValidation();

        assertTrue(miuw.getHasErrorMessages());
        assertEquals(1, miuw.getErrorMessages().size());
    }
}
