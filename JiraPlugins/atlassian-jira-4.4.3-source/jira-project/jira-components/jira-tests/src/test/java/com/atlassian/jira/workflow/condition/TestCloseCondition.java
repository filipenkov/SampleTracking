/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.workflow.MockWorkflowContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.workflow.condition.CloseCondition;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class TestCloseCondition extends AbstractUsersTestCase
{
    private GenericValue project;
    private Long workflowId;

    public TestCloseCondition(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        workflowId = new Long(2);
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", new Long(1), "workflowId", workflowId));
        UtilsForTests.getTestUser("bob");
    }

    public void testCloseFalse()
    {
        CloseCondition condition = new CloseCondition();
        assertTrue(!condition.passesCondition(EasyMap.build("close", Boolean.FALSE), null, null));
    }

    public void testCannotCloseNoPermissions()
    {
        CloseCondition condition = new CloseCondition();

        Map inputs = EasyMap.build("close", Boolean.FALSE);
        inputs.put("context", new MockWorkflowContext("bob"));
        assertTrue(!condition.passesCondition(inputs, null, null));
    }

    public void testCanClose() throws CreateException, GenericEntityException
    {
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project, Permissions.CLOSE_ISSUE);

        Mock mockWorkflowEntry = new Mock(WorkflowEntry.class);
        mockWorkflowEntry.setStrict(true);
        mockWorkflowEntry.expectAndReturn("getId", workflowId);
        
        CloseCondition condition = new CloseCondition();

        Map inputs = EasyMap.build("close", Boolean.TRUE, "entry", mockWorkflowEntry.proxy());
        inputs.put("context", new MockWorkflowContext("bob"));
        assertTrue(condition.passesCondition(inputs, null, null));
        mockWorkflowEntry.verify();
    }

    public void testCannotCloseNoAnonymousPermissions()
    {
        CloseCondition condition = new CloseCondition();

        Map inputs = EasyMap.build("close", Boolean.TRUE);
        inputs.put("context", new MockWorkflowContext(null));
        assertTrue(!condition.passesCondition(inputs, null, null));
    }
}
