package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestManageSubTasks extends LegacyJiraMockTestCase
{
    ManageSubTasks mst;
    private Mock mockSubTaskManager;

    public TestManageSubTasks(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);

        mst = new ManageSubTasks((SubTaskManager) mockSubTaskManager.proxy(), null);
    }

    public void testIsSubTasksEnabled()
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        assertTrue(mst.isSubTasksEnabled());
        mockSubTaskManager.verify();

        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);
        assertFalse(mst.isSubTasksEnabled());
        mockSubTaskManager.verify();
    }

    public void testDoEnableSubTasksSubTasksEnabled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = mst.doEnableSubTasks();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "Sub-Tasks are already enabled.");

        mockSubTaskManager.verify();
    }

    public void testDoEnableSubTasks() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = setupRedirectResponse();
        mockSubTaskManager.expectVoid("enableSubTasks");
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = mst.doEnableSubTasks();
        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
        mockSubTaskManager.verify();
    }

    public void testGettersSetters()
    {
        String id = "1";
        String name = "test name";
        String description = "test description";
        String iconurl = "test icon url";

        mst.setId(id);
        assertEquals(id, mst.getId());
        mst.setName(name);
        assertEquals(name, mst.getName());
        mst.setDescription(description);
        assertEquals(description, mst.getDescription());
        mst.setIconurl(iconurl);
        assertEquals(iconurl, mst.getIconurl());
    }

    public void testValidateAddInputSubTasksDisbaled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = mst.doAddSubTaskIssueType();
        assertEquals(Action.ERROR, result);

        checkSingleElementCollection(mst.getErrorMessages(), "Sub-Tasks are disabled.");
        mockSubTaskManager.verify();
    }

    public void testValidateAddInputNameIconurlNotSet() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = mst.doAddSubTaskIssueType();
        assertEquals(Action.ERROR, result);

        final Map errors = mst.getErrors();
        assertEquals(1, errors.size());
        assertEquals("You must specify a name for this new sub-task issue type.", errors.get("name"));

        mockSubTaskManager.verify();
    }

    public void testValidateAddInputNameExists() throws Exception
    {
        String name = "test name";
        String iconurl = "test icon url";

        mockSubTaskManager.expectAndReturn("issueTypeExistsByName", P.args(new IsEqual(name)), Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        mst.setName(name);
        mst.setIconurl(iconurl);

        final String result = mst.doAddSubTaskIssueType();
        assertEquals(Action.ERROR, result);

        final Map errors = mst.getErrors();
        assertEquals(1, errors.size());
        assertEquals("An issue type with this name already exists.", errors.get("name"));

        mockSubTaskManager.verify();
    }

    public void testValidatedoAddSubTaskIssueType() throws Exception
    {
        String name = "test name";
        String description = "test description";
        String iconurl = "test icon url";

        final MockHttpServletResponse mockHttpServletResponse = setupRedirectResponse();
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("issueTypeExistsByName", P.args(new IsEqual(name)), Boolean.FALSE);
        final List list = EasyList.build("something");
        mockSubTaskManager.expectAndReturn("getSubTaskIssueTypeObjects", list);
        mockSubTaskManager.expectAndReturn("createSubTaskIssueType", new Constraint[]{new IsEqual(name), new IsEqual(new Long(list.size())), new IsEqual(description), new IsEqual(iconurl)}, null);

        mst.setName(name);
        mst.setDescription(description);
        mst.setIconurl(iconurl);

        final String result = mst.doAddSubTaskIssueType();
        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeUpNoId() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = mst.doMoveSubTaskIssueTypeUp();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "No sub-task issue type id specified.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeUpSubTasksDisbaled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = mst.doMoveSubTaskIssueTypeUp();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "Sub-Tasks are disabled.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeUpIdDoesNotExist() throws Exception
    {
        String id = "1";
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("issueTypeExistsById", P.args(new IsEqual(id)), Boolean.FALSE);
        mst.setId(id);
        final String result = mst.doMoveSubTaskIssueTypeUp();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "No sub-task issue type with id '" + id + "' exists.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeUp() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = setupRedirectResponse();
        String id = "1";
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("issueTypeExistsById", P.args(new IsEqual(id)), Boolean.TRUE);
        mockSubTaskManager.expectVoid("moveSubTaskIssueTypeUp", P.args(new IsEqual(id)));
        mst.setId(id);
        final String result = mst.doMoveSubTaskIssueTypeUp();
        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeDownSubTasksDisbaled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = mst.doMoveSubTaskIssueTypeDown();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "Sub-Tasks are disabled.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeDownNoId() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = mst.doMoveSubTaskIssueTypeDown();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "No sub-task issue type id specified.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeDownIdDoesNotExist() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        String id = "1";

        mockSubTaskManager.expectAndReturn("issueTypeExistsById", P.args(new IsEqual(id)), Boolean.FALSE);
        mst.setId(id);
        final String result = mst.doMoveSubTaskIssueTypeDown();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(mst.getErrorMessages(), "No sub-task issue type with id '" + id + "' exists.");

        mockSubTaskManager.verify();
    }

    public void testDoMoveSubTaskIssueTypeDown() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = setupRedirectResponse();
        String id = "1";
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("issueTypeExistsById", P.args(new IsEqual(id)), Boolean.TRUE);
        mockSubTaskManager.expectVoid("moveSubTaskIssueTypeDown", P.args(new IsEqual(id)));
        mst.setId(id);
        final String result = mst.doMoveSubTaskIssueTypeDown();
        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
        mockSubTaskManager.verify();
    }

    private MockHttpServletResponse setupRedirectResponse() throws IOException
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ManageSubTasks.jspa");
        return response;
    }
}
