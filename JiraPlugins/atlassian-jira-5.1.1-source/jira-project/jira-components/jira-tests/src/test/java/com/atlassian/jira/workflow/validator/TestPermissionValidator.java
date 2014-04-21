/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.WorkflowContext;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.workflow.MockWorkflowContext;

import java.util.HashMap;
import java.util.Map;

public class TestPermissionValidator extends AbstractUsersTestCase
{
    private User testuser1;
    private User testuser2;
    private PermissionValidator pv;
    private Map args;
    private GenericValue projectGV;
    private Project project;
    private Group testgroup;
    private Map transientVars;
    private GenericValue scheme;
    private Mock mockIssue;

    public TestPermissionValidator(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        testuser1 = createMockUser("Test User1");
        testuser2 = createMockUser("Test User2");
        testgroup = createMockGroup("foo");
        addUserToGroup(testuser1, testgroup);
        addUserToGroup(testuser2, testgroup);

        projectGV = EntityUtils.createValue("Project", EasyMap.build("id", new Long(1)));
        project = new ProjectImpl(projectGV);
        transientVars = new HashMap();
        pv = new PermissionValidator();

        WorkflowContext wfc = new MockWorkflowContext("Test User1");
        transientVars.put("context", wfc);
        args = EasyMap.build("permission", "create issue");
        scheme = ManagerFactory.getPermissionSchemeManager().createDefaultScheme();
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(projectGV, scheme);
        mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
    }

    public void testValidatehasPermission1() throws CreateException
    {
        mockIssue.expectAndReturn("getProjectObject", project);
        mockIssue.expectAndReturn("getGenericValue", null);
        transientVars.put("issue", mockIssue.proxy());
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);
        args.put("username", "Test User2");
        doTestValidate(true, null);
        mockIssue.verify();
    }

    public void testValidatehasPermissionWithIssue1() throws CreateException
    {
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", projectGV.getLong("id")));
        mockIssue.expectAndReturn("getGenericValue", issue);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);
        transientVars.put("issue", mockIssue.proxy());
        args.put("username", "Test User2");
        doTestValidate(true, null);
        mockIssue.verify();
    }

    public void testValidatehasPermission2() throws CreateException
    {
        mockIssue.expectAndReturn("getProjectObject", project);
        mockIssue.expectAndReturn("getGenericValue", null);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);
        transientVars.put("issue", mockIssue.proxy());
        doTestValidate(true, null);
        mockIssue.verify();
    }

    public void testValidatehasPermission3() throws CreateException
    {
        mockIssue.expectAndReturn("getProjectObject", project);
        mockIssue.expectAndReturn("getGenericValue", null);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);
        transientVars.put("issue", mockIssue.proxy());
        doTestValidate(true, null);
        mockIssue.verify();
    }

    public void testValidateNoPermissionSetup()
    {
        mockIssue.expectAndReturn("getProjectObject", project);
        mockIssue.expectAndReturn("getGenericValue", null);
        transientVars.put("issue", mockIssue.proxy());
        doTestValidate(false, "User 'Test User1' doesn't have the 'create issue' permission");
        mockIssue.verify();
    }

    public void testValidateNoPermissionUserNotFound() throws CreateException
    {
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);
        transientVars.put("issue", mockIssue.proxy());
        args.put("username", "non-existent user");
        doTestValidate(false, "You don't have the correct permissions - user (non-existent user) not found");
        mockIssue.verify();
    }

    private void doTestValidate(boolean expectedTrue, String errorMsg)
    {
        boolean permitted;
        String error = null;

        try
        {
            pv.validate(transientVars, args, null);
            permitted = true;
        }
        catch (InvalidInputException e)
        {
            permitted = false;
            error = e.getMessage();
        }

        if (expectedTrue)
            assertTrue(permitted);
        else
        {
            assertTrue(!permitted);
            assertEquals(errorMsg, error);
        }
    }
}
