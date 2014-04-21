/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.workflow.MockWorkflowContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.workflow.function.misc.CreateCommentFunction;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.WorkflowContext;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class TestCreateCommentFunction extends LegacyJiraMockTestCase
{
    private Map transientVars;
    private CreateCommentFunction ccf;
    private Issue issue;

    public TestCreateCommentFunction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project, Permissions.COMMENT_ISSUE);

        GenericValue issueGV = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "project", new Long(1)));

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectAndReturn("getGenericValue", issueGV);
        mockIssue.expectAndReturn("getId", new Long(1));

        this.issue = (Issue) mockIssue.proxy();
        transientVars = EasyMap.build("issue", this.issue, "comment", "Test Comment", "commentLevel", "Test Level");

        ccf = new CreateCommentFunction();
    }


    protected void tearDown() throws Exception
    {
        transientVars = null;
        ccf = null;
        issue = null;
        super.tearDown();
    }

    public void testCreateCommentFunction() throws GenericEntityException
    {
        // Add the user to the workflow context
        UtilsForTests.getTestUser("Test User");
        WorkflowContext wfc = new MockWorkflowContext("Test User");
        transientVars.put("context", wfc);

        final MockControl mockCommentManagerControl = MockControl.createStrictControl(CommentManager.class);
        final CommentManager mockCommentManager = (CommentManager) mockCommentManagerControl.getMock();
        mockCommentManager.create(this.issue, "Test User", "Test Comment", "Test Level", null, false);
        mockCommentManagerControl.setReturnValue(null);
        mockCommentManagerControl.replay();

        ManagerFactory.addService(CommentManager.class, mockCommentManager);

        ccf.execute(transientVars, null, null);
        mockCommentManagerControl.verify();
    }

    public void testCreateCommentFunctionAnonUser() throws GenericEntityException
    {
        // Add a workflow context that indicates a null user
        WorkflowContext wfc = new MockWorkflowContext(null);
        transientVars.put("context", wfc);

        final MockControl mockCommentManagerControl = MockControl.createStrictControl(CommentManager.class);
        final CommentManager mockCommentManager = (CommentManager) mockCommentManagerControl.getMock();
        mockCommentManager.create(this.issue, null, "Test Comment", "Test Level", null, false);
        mockCommentManagerControl.setReturnValue(null);
        mockCommentManagerControl.replay();

        CommentManager oldCommentManager = (CommentManager) ManagerFactory.addService(CommentManager.class, mockCommentManager).getComponentInstance();
        try
        {
            ManagerFactory.addService(CommentManager.class, mockCommentManager);

            ccf.execute(transientVars, null, null);
            mockCommentManagerControl.verify();
        }
        finally
        {
            ManagerFactory.addService(CommentManager.class, oldCommentManager);
        }
    }
}
