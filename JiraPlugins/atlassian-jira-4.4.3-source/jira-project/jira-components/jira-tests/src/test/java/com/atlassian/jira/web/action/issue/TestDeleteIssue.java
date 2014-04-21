package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class TestDeleteIssue extends LegacyJiraMockTestCase
{
    private DeleteIssue di;
    private Mock mockSubTaskManager;
    private GenericValue issue;
    private Mock mockList;
    private Mock mockPermissionManager;

    @Override
    protected void tearDown() throws Exception
    {
        di = null;
        mockSubTaskManager = null;
        issue = null;
        mockList = null;
        mockPermissionManager = null;
        super.tearDown();
    }

    public TestDeleteIssue(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);

        mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);

        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue"));

        di = new DeleteIssue(null, (SubTaskManager) mockSubTaskManager.proxy(), null);

        di.setId(issue.getLong("id"));
    }

    public void testGetNumberOfSubTasks()
    {
        mockPermissionManager.expectAndReturn("hasPermission",
            P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsAnything(), new IsNull()), Boolean.TRUE);

        final int expectedNumberOfSubTasks = 6;
        mockList = new Mock(List.class);
        mockList.expectAndReturn("size", new Integer(expectedNumberOfSubTasks));
        mockSubTaskManager.expectAndReturn("getSubTaskIssueLinks", P.args(new IsEqual(issue.getLong("id"))), mockList.proxy());

        final int result = di.getNumberOfSubTasks();
        assertEquals(expectedNumberOfSubTasks, result);

        mockList.verify();
        verifyMocks();
    }

    private void verifyMocks()
    {
        mockSubTaskManager.verify();
        mockPermissionManager.verify();
    }
}
