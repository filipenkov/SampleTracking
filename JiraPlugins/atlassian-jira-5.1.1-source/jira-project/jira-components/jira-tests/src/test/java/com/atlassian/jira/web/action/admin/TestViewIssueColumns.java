/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.AbstractTestViewIssueColumns;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;

public class TestViewIssueColumns extends AbstractTestViewIssueColumns
{
    public TestViewIssueColumns(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        viewIssueColumns = new ViewIssueColumns(null, null, null, null, null);
    }

    public void testSecurityBreach() throws Exception
    {
        //Change the mock for this test to ensure it fails
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsAnything()), Boolean.FALSE);

        viewIssueColumns.setOperation(ViewIssueColumns.ADD);

        String result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.DELETE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.MOVELEFT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.MOVERIGHT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.RESTORE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
    }

    public void testSecurityBreachWithNullUser() throws Exception
    {
        JiraTestUtil.loginUser(null);

        //Change the mock for this test to ensure it fails
        permissionManager.expectNotCalled("hasPermission");

        viewIssueColumns.setOperation(ViewIssueColumns.ADD);

        String result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.DELETE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.MOVELEFT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.MOVERIGHT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(ViewIssueColumns.RESTORE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);

        permissionManager.verify();
    }

    public void testAddColumn() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.ADD);
        viewIssueColumns.setFieldId(IssueFieldConstants.STATUS);

        String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        GenericValue columnLayoutGV = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout", EasyMap.build("username", null)));
        List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        assertFalse(columnLayoutItemGVs.isEmpty());
        assertEquals(4, columnLayoutItemGVs.size());
        assertEquals(IssueFieldConstants.STATUS, ((GenericValue) columnLayoutItemGVs.get(3)).getString("fieldidentifier"));
    }

    public void testAddNonExistantColumn() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.ADD);
        viewIssueColumns.setFieldId("FieldDoesNotExist");

        String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        assertEquals("The column with id 'FieldDoesNotExist' does not exist.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testAddInvalidColumn() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.ADD);
        viewIssueColumns.setFieldId(IssueFieldConstants.TIMETRACKING);

        String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        assertEquals("The column Time Tracking cannot be shown in Issue Navigator.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testDeleteColumnNoFieldId() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.DELETE);

        String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to delete.");
    }

    public void testDeleteColumn() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.DELETE);
        viewIssueColumns.setFieldPosition(new Integer(1));

        String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        GenericValue columnLayoutGV = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout", EasyMap.build("username", null)));
        List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));

        assertFalse(columnLayoutItemGVs.isEmpty());
        assertEquals(2, columnLayoutItemGVs.size());
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) columnLayoutItemGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) columnLayoutItemGVs.get(1)).getString("fieldidentifier"));
    }

    public void testDeleteInvalidColumn() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.DELETE);
        viewIssueColumns.setFieldPosition(new Integer(30));

        String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not delete field at position '31'.");
    }

    public void testMoveColumnLeftNoFieldId() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVELEFT);

        String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to move left.");
    }

    public void testMoveColumnLeft() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVELEFT);
        viewIssueColumns.setFieldPosition(new Integer(1));

        String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        GenericValue columnLayoutGV = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout", EasyMap.build("username", null)));
        List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        assertFalse(columnLayoutItemGVs.isEmpty());
        assertEquals(3, columnLayoutItemGVs.size());
        assertEquals(IssueFieldConstants.SUMMARY, ((GenericValue) columnLayoutItemGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) columnLayoutItemGVs.get(1)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) columnLayoutItemGVs.get(2)).getString("fieldidentifier"));
    }

    public void testMoveInvalidColumnLeft() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVELEFT);
        viewIssueColumns.setFieldPosition(new Integer(0));

        String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not move column at position '1' left.");
    }

    public void testMoveColumnRightNoFieldId() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVERIGHT);

        String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Please select a column to move right.");
    }

    public void testMoveColumnRight() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVERIGHT);
        viewIssueColumns.setFieldPosition(new Integer(1));

        String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        GenericValue columnLayoutGV = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout", EasyMap.build("username", null)));
        List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        assertFalse(columnLayoutItemGVs.isEmpty());
        assertEquals(3, columnLayoutItemGVs.size());
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) columnLayoutItemGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) columnLayoutItemGVs.get(1)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.SUMMARY, ((GenericValue) columnLayoutItemGVs.get(2)).getString("fieldidentifier"));
    }

    public void testMoveInvalidColumnRight() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.MOVERIGHT);
        viewIssueColumns.setFieldPosition(new Integer(2));

        String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "Can not move column at position '3' right.");
    }

    public void testRestoreDefault() throws Exception
    {
        viewIssueColumns.setOperation(ViewIssueColumns.RESTORE);

        String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        GenericValue columnLayoutGV = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout", EasyMap.build("username", null)));
        assertNull(columnLayoutGV);
    }
}
