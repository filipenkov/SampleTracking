/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;

public class TestAbstractViewIssue extends AbstractWebworkTestCase
{
    private GenericEntity project;
    private GenericValue issue;

    public TestAbstractViewIssue(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project"));
        issue = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "JRA-52", "project", project.getLong("id")));

        final Mock permissionManager = new Mock(PermissionManager.class);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsAnything(), new IsNull()),
            Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());
    }

    public void testGetSetIdNoIssue() throws Exception
    {
        // Reset permission manager
        final Mock permissionManager = new Mock(PermissionManager.class);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsAnything(), new IsNull()),
            Boolean.FALSE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());

        final AbstractViewIssue avi = new AbstractViewIssue(null);
        avi.setId(new Long(1));
        assertEquals(new Long(1), avi.getId());
        try
        {
            avi.getIssue();
            fail("Exception should have been thrown");
        }
        catch (final Exception e)
        {}
    }

    public void testGetSetIdWithIssue() throws Exception
    {
        final AbstractViewIssue avi = new AbstractViewIssue(null);

        avi.setId(issue.getLong("id"));
        assertEquals(issue.getLong("id"), avi.getId());

        assertEquals(issue, avi.getIssue());
    }

    public void testSetKey() throws Exception
    {
        final AbstractViewIssue avi = new AbstractViewIssue(null);
        avi.setKey("JRA-52");
        assertEquals(issue, avi.getIssue());
    }

    public void testSetBullshitKey() throws Exception
    {
        final AbstractViewIssue avi = new AbstractViewIssue(null);
        avi.setKey("FOO-01");
        try
        {
            avi.getIssue();
            fail("Exception should have been thrown");
        }
        catch (final Exception e)
        {}
    }

    public void testProjectAndPossibleDependencies() throws Exception
    {
        final Version version = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("name", "foo", "project", project.getLong("id"))));

        final GenericValue component = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Component", EasyMap.build("project",
            project.getLong("id")));

        final AbstractViewIssue avi = new AbstractViewIssue(null);
        avi.setKey("JRA-52");
        assertEquals(project, avi.getProject());
        checkSingleElementCollection(avi.getPossibleComponents(), component);

    }
}
