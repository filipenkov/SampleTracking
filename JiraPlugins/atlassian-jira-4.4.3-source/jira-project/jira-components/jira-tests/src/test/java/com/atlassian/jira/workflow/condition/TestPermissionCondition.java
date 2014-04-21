/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.workflow.condition.PermissionCondition;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.Map;

public class TestPermissionCondition extends AbstractUsersTestCase
{
    private PermissionCondition pc;
    private GenericValue issue;
    private GenericValue project;
    private Group testgroup;
    private User bob;
    private User bill;
    private User mike;
    private Mock wfc;
    private Mock wfe;
    private Map args;
    private Map transientVars;

    public TestPermissionCondition(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        issue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "project", new Long(2), "workflowId", new Long(100)));
        project = EntityUtils.createValue("Project", EasyMap.build("id", new Long(2)));

        testgroup = UtilsForTests.getTestGroup("foo");
        bob = UtilsForTests.getTestUser("bob");
        bill = UtilsForTests.getTestUser("bill");
        mike = UtilsForTests.getTestUser("mike");
        testgroup.addUser(bob);

        GenericValue scheme = ManagerFactory.getPermissionSchemeManager().createDefaultScheme();
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project, scheme);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, testgroup.getName(), GroupDropdown.DESC);

        pc = new PermissionCondition();
        wfc = new Mock(WorkflowContext.class);
        wfe = new Mock(WorkflowEntry.class);

        args = EasyMap.build("permission", "create issue");
        transientVars = EasyMap.build("context", wfc.proxy(), "entry", wfe.proxy());
    }

    public void testPermissionConditionOk1()
    {
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(100));

        assertTrue(pc.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionOk2()
    {
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(100));

        args.put("username", "bob");

        assertTrue(pc.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionNoIssue()
    {
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(101));

        try
        {
            pc.passesCondition(transientVars, args, null);
            fail("No issue; should have thrown a DataAccessException");
        } catch (DataAccessException dae) {}
    }

    public void testPermissionConditionTrue() throws GenericEntityException
    {
        GenericValue testIssue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(20), "project", project.getLong("id"), "workflowId", new Long(200)));
        wfc.setupResult("getCaller", "bob");
        wfe.setupResult("getId", new Long(200));

        assertTrue(pc.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionFalseNoUserPerm()
    {
        wfc.setupResult("getCaller", "bill");
        wfe.setupResult("getId", new Long(100));

        assertTrue(!pc.passesCondition(transientVars, args, null));
    }

    public void testPermissionConditionFalseNoUser()
    {
        wfc.setupResult("getCaller", "mike");
        wfe.setupResult("getId", new Long(100));

        assertTrue(!pc.passesCondition(transientVars, args, null));
    }
}
