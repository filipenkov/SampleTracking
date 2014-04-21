/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractTestViewIssueColumns extends AbstractUsersTestCase
{
    protected AbstractViewIssueColumns viewIssueColumns;
    protected Mock permissionManager;
    protected User adminUser;
    protected List<GenericValue> projects;
    protected List<Project> projectObjs;
    protected User user;
    private PermissionManager oldPermissionManager;

    public AbstractTestViewIssueColumns(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        final GenericValue columnLayout = UtilsForTests.getTestEntity("ColumnLayout", EasyMap.build("username", null));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(0)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.SUMMARY, "horizontalposition", new Long(1)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(2)));
        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug"));

        adminUser = UtilsForTests.getTestUser("adminuser");

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "browsable project."));
        projects = new ArrayList<GenericValue>();
        projects.add(project);
        projectObjs = new ArrayList<Project>();
        projectObjs.add(new ProjectImpl(project));
        permissionManager = new Mock(PermissionManager.class);
        permissionManager.setStrict(true);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(adminUser)),
            Boolean.TRUE);
        oldPermissionManager = ManagerFactory.getPermissionManager();
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());
        JiraTestUtil.loginUser(adminUser);
    }

    @Override
    protected void tearDown() throws Exception
    {
        adminUser = null;
        projects = null;
        permissionManager = null;
        ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        oldPermissionManager = null;
        super.tearDown();
    }

    public void testAddColumnNoFieldId() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        assertEquals("Please select a field to add.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testAddColumnAgain() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);
        viewIssueColumns.setFieldId(IssueFieldConstants.ISSUE_KEY);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        assertEquals("The column Key already exists in the list.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testRetrieveAddableColumns() throws ColumnLayoutStorageException
    {
        final List addableColumns = viewIssueColumns.getAddableColumns();
        assertEquals(20, addableColumns.size());
    }

    public void testRetrieveAddableColumnsWithHiddenColumn() throws FieldException
    {
        // Setup hidden column
        final GenericValue fieldlayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("type", FieldLayoutManager.TYPE_DEFAULT));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldlayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "verticalposition", new Long(0), "ishidden", Boolean.TRUE.toString(), "isrequired",
            Boolean.FALSE.toString()));

        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(adminUser)), projectObjs);

        // Get addable column list.
        final Set defaultColumns = ManagerFactory.getFieldManager().getAvailableNavigableFields(adminUser);
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        final List addableColumns = viewIssueColumns.getAddableColumns();
        assertEquals(defaultColumns.size() - 2, addableColumns.size());
    }

    public void testAddNonExistantColumn() throws Exception
    {
        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);
        viewIssueColumns.setFieldId("FieldDoesNotExist");

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        assertEquals("The column with id 'FieldDoesNotExist' does not exist.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testAddInvalidColumn() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);
        // try to add timetracking, this is not allowed as timetracking is a composite field.
        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);
        viewIssueColumns.setFieldId(IssueFieldConstants.TIMETRACKING);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        assertEquals("The column Time Tracking cannot be shown in Issue Navigator.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testDeleteColumnNoFieldId() throws Exception
    {
          permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.DELETE);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to delete.");
    }

    public void testDeleteNonExistantColumn() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.DELETE);
        viewIssueColumns.setFieldPosition(new Integer(20));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not delete field at position '21'.");
    }

    public void testMoveColumnLeftNoFieldId() throws Exception
    {
          permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVELEFT);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to move left.");
    }

    public void testMoveInvalidColumnLeft() throws Exception
    {
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVELEFT);
        viewIssueColumns.setFieldPosition(new Integer(0));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not move column at position '1' left.");
    }

    public void testMoveColumnRightNoFieldId() throws Exception
    {
          permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVERIGHT);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to move right.");
    }

    public void testMoveInvalidColumnRight() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVERIGHT);
        viewIssueColumns.setFieldPosition(new Integer(2));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not move column at position '3' right.");
    }
}
