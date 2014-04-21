/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.enterprise;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.AbstractTestViewIssueColumns;
import com.atlassian.jira.web.action.AbstractViewIssueColumns;
import com.atlassian.jira.web.action.admin.enterprise.ViewSearchRequestIssueColumns;
import com.atlassian.query.QueryImpl;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;

public class TestViewSearchRequestIssueColumns extends AbstractTestViewIssueColumns
{
    private static final String USERNAME = "user";
    private SearchRequest searchRequest;

    public TestViewSearchRequestIssueColumns(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        user = UtilsForTests.getTestUser(USERNAME);

        searchRequest = new SearchRequest(new QueryImpl(), USERNAME, "Test Search Request", "This is a Test Search Request");
        ManagerFactory.getSearchRequestManager().create(searchRequest);
        searchRequest = ManagerFactory.getSearchRequestManager().getOwnedSearchRequestByName(user, "Test Search Request");

        // Setup user navigator columns
        final GenericValue columnLayout = UtilsForTests.getTestEntity("ColumnLayout", EasyMap.build("username", null, "searchrequest",
            searchRequest.getId()));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(0)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.SUMMARY, "horizontalposition", new Long(1)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(2)));

        viewIssueColumns = new ViewSearchRequestIssueColumns(null, null, null);

        final ViewSearchRequestIssueColumns viewSearchRequestIssueColumns = (ViewSearchRequestIssueColumns) viewIssueColumns;
        viewSearchRequestIssueColumns.setFilterId(searchRequest.getId());

        permissionManager = new Mock(PermissionManager.class);
        permissionManager.setStrict(true);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.USE)), new IsEqual(user)), Boolean.TRUE);

        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());
        JiraTestUtil.loginUser(user);

    }

    public void testSecurityBreachWithNullUser() throws Exception
    {
        // Setup null user
        JiraTestUtil.loginUser(null);

        //Change the mock for this test to ensure it fails
        permissionManager.expectNotCalled("hasPermission");

        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);

        String result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.DELETE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVELEFT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVERIGHT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.RESTORE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);

        permissionManager.verify();
    }

    public void testSecurityBreach() throws Exception
    {
        //Change the mock for this test to ensure it fails
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.USE)), new IsAnything()), Boolean.FALSE);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);

        String result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.DELETE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVELEFT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVERIGHT);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
        viewIssueColumns.setOperation(AbstractViewIssueColumns.RESTORE);
        result = viewIssueColumns.execute();
        assertEquals("securitybreach", result);
    }

    public void testExecuteNoFileterId() throws Exception
    {
        // Reset the filter id to test the error message
        final ViewSearchRequestIssueColumns viewSearchRequestIssueColumns = (ViewSearchRequestIssueColumns) viewIssueColumns;
        viewSearchRequestIssueColumns.setFilterId(null);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(viewIssueColumns.getErrorMessages(), "The filter id has to be provided.");
    }

    @Override
    public void testAddColumnNoFieldId() throws Exception
    {
        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.INPUT, result);
        assertEquals("Please select a field to add.", viewIssueColumns.getErrors().get("fieldId"));
    }

    public void testAddColumn() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.ADD);
        viewIssueColumns.setFieldId(IssueFieldConstants.STATUS);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        final GenericValue columnLayout = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout",
            EasyMap.build("searchrequest", searchRequest.getId())));
        final List defaultFieldConfGVs = columnLayout.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition asc"));
        assertFalse(defaultFieldConfGVs.isEmpty());
        assertEquals(4, defaultFieldConfGVs.size());
        assertEquals(IssueFieldConstants.STATUS, ((GenericValue) defaultFieldConfGVs.get(3)).getString("fieldidentifier"));
    }

    public void testDeleteColumn() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.DELETE);
        viewIssueColumns.setFieldPosition(new Integer(1));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        final GenericValue columnLayout = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout",
            EasyMap.build("searchrequest", searchRequest.getId())));
        final List defaultFieldConfGVs = columnLayout.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition asc"));
        assertFalse(defaultFieldConfGVs.isEmpty());
        assertEquals(2, defaultFieldConfGVs.size());
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) defaultFieldConfGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) defaultFieldConfGVs.get(1)).getString("fieldidentifier"));
    }

    public void testMoveColumnLeft() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVELEFT);
        viewIssueColumns.setFieldPosition(new Integer(1));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        final GenericValue columnLayout = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout",
            EasyMap.build("searchrequest", searchRequest.getId())));
        final List defaultFieldConfGVs = columnLayout.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition asc"));
        assertFalse(defaultFieldConfGVs.isEmpty());
        assertEquals(3, defaultFieldConfGVs.size());
        assertEquals(IssueFieldConstants.SUMMARY, ((GenericValue) defaultFieldConfGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) defaultFieldConfGVs.get(1)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) defaultFieldConfGVs.get(2)).getString("fieldidentifier"));
    }

    public void testMoveColumnRight() throws Exception
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        viewIssueColumns.setOperation(AbstractViewIssueColumns.MOVERIGHT);
        viewIssueColumns.setFieldPosition(new Integer(1));

        final String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        final GenericValue columnLayout = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout",
            EasyMap.build("searchrequest", searchRequest.getId())));
        final List defaultFieldConfGVs = columnLayout.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition asc"));
        assertFalse(defaultFieldConfGVs.isEmpty());
        assertEquals(3, defaultFieldConfGVs.size());
        assertEquals(IssueFieldConstants.ISSUE_KEY, ((GenericValue) defaultFieldConfGVs.get(0)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.ASSIGNEE, ((GenericValue) defaultFieldConfGVs.get(1)).getString("fieldidentifier"));
        assertEquals(IssueFieldConstants.SUMMARY, ((GenericValue) defaultFieldConfGVs.get(2)).getString("fieldidentifier"));
    }

    public void testRestoreDefault() throws Exception
    {
        viewIssueColumns.setOperation(AbstractViewIssueColumns.RESTORE);

        final String result = viewIssueColumns.execute();
        assertEquals(Action.SUCCESS, result);

        final GenericValue columnLayout = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("ColumnLayout",
            EasyMap.build("searchrequest", searchRequest.getId())));
        assertNull(columnLayout);
    }

    @Override
    public void testRetrieveAddableColumns()
    {
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), projectObjs);

        final List addableColumns = viewIssueColumns.getAddableColumns();

        // As Enterprise license is installed the securitylevel field is available
        assertEquals(20, addableColumns.size());
    }
}
