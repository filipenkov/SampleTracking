package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

import java.util.HashSet;


public class TestDisableSubTasks extends LegacyJiraMockTestCase
{
    private DisableSubTasks dst;
    private Mock mockSubTaskManager;

    public TestDisableSubTasks(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);

        dst = new DisableSubTasks((SubTaskManager) mockSubTaskManager.proxy());
    }

    public void testDoDefaultNoSubTasks() throws Exception
    {
        final MockHttpServletResponse expectedRedirect = JiraTestUtil.setupExpectedRedirect("ManageSubTasks.jspa");
        mockSubTaskManager.expectAndReturn("getAllSubTaskIssueIds", new HashSet());
        mockSubTaskManager.expectVoid("disableSubTasks");
        assertEquals(Action.NONE, dst.doDefault());
        expectedRedirect.verify();
        verifyMocks();
    }

    public void testDoDefaultSubTasksExist() throws Exception
    {
        final HashSet issueIds = new HashSet();
        issueIds.add(new Long(1000));
        mockSubTaskManager.expectAndReturn("getAllSubTaskIssueIds", issueIds);
        assertEquals(Action.INPUT, dst.doDefault());
        assertEquals(issueIds.size(), dst.getSubTaskCount());
        verifyMocks();
    }

    public void testDoExecute() throws Exception
    {
        final MockHttpServletResponse expectedRedirect = JiraTestUtil.setupExpectedRedirect("ManageSubTasks.jspa");
        assertEquals(Action.NONE, dst.execute());
        expectedRedirect.verify();
        verifyMocks();
    }

    private void verifyMocks()
    {
        mockSubTaskManager.verify();
    }
}
