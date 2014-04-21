/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers.enterprise;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.DefaultColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayoutImpl;
import com.atlassian.jira.issue.managers.TestDefaultColumnLayoutManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.query.QueryImpl;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class TestEnterpriseColumnLayoutManager extends TestDefaultColumnLayoutManager
{
    private SearchRequest searchRequest;

    public TestEnterpriseColumnLayoutManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Refresh license details and ComponentManager (so as FieldManager is available)
        ManagerFactory.quickRefresh();
        // Add the permission manager as a refresh has just been completed.
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());
        ManagerFactory.addService(SharedEntityIndexer.class, new MockSharedEntityIndexer());

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "testproject"));
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(testUser)),
            EasyList.build(new ProjectImpl(project)));
        clm = new DefaultColumnLayoutManager(defaultFieldManager, null);
        searchRequest = new SearchRequest(new QueryImpl(), testUser.getName(), "Test Search Request", "This is a Test Search Request");
        ManagerFactory.getSearchRequestManager().create(searchRequest);
        searchRequest = ManagerFactory.getSearchRequestManager().getOwnedSearchRequestByName(testUser, "Test Search Request");
    }

    protected int setupSearchRequestColumnLayoutWithHiddenColumn(final SearchRequest searchRequest, int position)
    {
        // Setup search request navigator columns with one hidden column
        columnLayout = UtilsForTests.getTestEntity("ColumnLayout", EasyMap.build("searchrequest", searchRequest.getId()));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.SUMMARY, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.COMPONENTS, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(position++)));
        return position;
    }

    //      The method tests that the column layou returns the default column layout when the search
    //      request does not have a column layout, and the logged in user does not have a custom
    //      column layout
    public void testGetColumnLayoutDefaultColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        final ColumnLayout columnLayout = clm.getColumnLayout(testUser, searchRequest);

        // 10 should be returned - all of the 11 which are the default setup in the AbstractColumnLayoutManager,
        // method without assignee as it hidden by the setupDefaultFieldLayout method invoked above.
        assertEquals(10, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    public void testGetColumnLayoutUserColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the testUser
        setupColumnLayoutWithHiddenColumn(testUser, 0);

        final ColumnLayout columnLayout = clm.getColumnLayout(testUser, searchRequest);

        // 2 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(2, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    //    The method tests that the column layout returns the user column layout when the search
    //    request does not have a column layout, but the user does
    public void testGetColumnLayoutSearchRequestColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the testUser
        setupSearchRequestColumnLayoutWithHiddenColumn(searchRequest, 0);

        final ColumnLayout columnLayout = clm.getColumnLayout(testUser, searchRequest);

        // 3 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(3, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    public void testGetEditableSearchRequestColumnLayoutNullUser() throws ColumnLayoutStorageException
    {
        try
        {
            clm.getEditableSearchRequestColumnLayout(null, searchRequest);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("User cannot be null.", e.getMessage());
        }
    }

    public void testGetEditableSearchRequestColumnLayoutNullSearchRequest() throws ColumnLayoutStorageException
    {
        try
        {
            clm.getEditableSearchRequestColumnLayout(testUser, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("SearchRequest cannot be null.", e.getMessage());
        }
    }

    //     The method tests that the editable column layout returns the default column layout when the search
    //     request does not have a column layout, and the logged in user does not have a custom
    //     column layout

    public void testGetEditableColumnLayoutDefaultColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        final ColumnLayout columnLayout = clm.getEditableSearchRequestColumnLayout(testUser, searchRequest);

        // 10 should be returned - all of the 11 which are the default setup in the AbstractColumnLayoutManager,
        // method without assignee as it hidden by the setupDefaultFieldLayout method invoked above.
        assertEquals(10, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    public void testEditableGetColumnLayoutUserColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the testUser
        setupColumnLayoutWithHiddenColumn(testUser, 0);

        final ColumnLayout columnLayout = clm.getEditableSearchRequestColumnLayout(testUser, searchRequest);

        // 2 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(2, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    //     The method tests that the column layout returns the user column layout when the search
    //     request does not have a column layout, but the user does

    public void testGetEditableColumnLayoutSearchRequestColumns() throws ColumnLayoutStorageException
    {
        setupDefaultFieldLayout(0);

        // Setup layout for the testUser
        setupSearchRequestColumnLayoutWithHiddenColumn(searchRequest, 0);

        final ColumnLayout columnLayout = clm.getEditableSearchRequestColumnLayout(testUser, searchRequest);

        // 3 should be returned - the ones that are setup by the setupColumnLayoutWithHiddenColumn method, invoked
        // above, except the ASSIGNEE and ENVIRONMENT fields that are hidden by the setupDefaultFieldLayout invoked above
        assertEquals(3, columnLayout.getColumnLayoutItems().size());
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ASSIGNEE)));
        assertFalse(columnLayout.contains(defaultFieldManager.getNavigableField(IssueFieldConstants.ENVIRONMENT)));
    }

    public void testStoreEditableSearchRequestColumnLayoutNoOriginalEntries() throws ColumnLayoutStorageException, GenericEntityException
    {
        // Make a Column Layout
        int position = 0;
        final List columnLayoutItems = new ArrayList();
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_KEY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_TYPE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.PRIORITY), position++));

        final EditableSearchRequestColumnLayout editableSearchRequestColumnLayout = new EditableSearchRequestColumnLayoutImpl(columnLayoutItems,
            testUser, searchRequest);
        clm.storeEditableSearchRequestColumnLayout(editableSearchRequestColumnLayout);

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final GenericValue columnLayoutGV = EntityUtil.getOnly(genericDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null,
            "searchrequest", searchRequest.getId())));
        assertNotNull(columnLayoutGV);

        final List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        assertEquals(3, columnLayoutItemGVs.size());

        GenericValue columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(0);
        assertEquals(IssueFieldConstants.ISSUE_KEY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(0L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(1);
        assertEquals(IssueFieldConstants.ISSUE_TYPE, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(1L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(2);
        assertEquals(IssueFieldConstants.PRIORITY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(2L, columnLayoutItemGV.getLong("horizontalposition").longValue());
    }

    public void testStoreEditableSearchRequestColumnLayoutWithOriginalEntries() throws ColumnLayoutStorageException, GenericEntityException
    {
        // Create exiting search request
        int position = 0;
        final Long filterId = searchRequest.getId();
        GenericValue columnLayoutGV = EntityUtils.createValue("ColumnLayout", EasyMap.build("username", null, "searchrequest", filterId));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.DESCRIPTION, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.RESOLUTION, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.REPORTER, "horizontalposition", new Long(position++)));

        // Make a Column Layout
        position = 0;

        final List columnLayoutItems = new ArrayList();
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_KEY), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.ISSUE_TYPE), position++));
        columnLayoutItems.add(new ColumnLayoutItemImpl(defaultFieldManager.getNavigableField(IssueFieldConstants.PRIORITY), position++));

        final EditableSearchRequestColumnLayout editableSearchRequestColumnLayout = new EditableSearchRequestColumnLayoutImpl(columnLayoutItems,
            testUser, searchRequest);
        clm.storeEditableSearchRequestColumnLayout(editableSearchRequestColumnLayout);

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        columnLayoutGV = EntityUtil.getOnly(genericDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest",
            searchRequest.getId())));
        assertNotNull(columnLayoutGV);

        final List columnLayoutItemGVs = columnLayoutGV.getRelatedOrderBy("ChildColumnLayoutItem", EasyList.build("horizontalposition ASC"));
        assertEquals(3, columnLayoutItemGVs.size());

        GenericValue columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(0);
        assertEquals(IssueFieldConstants.ISSUE_KEY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(0L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(1);
        assertEquals(IssueFieldConstants.ISSUE_TYPE, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(1L, columnLayoutItemGV.getLong("horizontalposition").longValue());
        columnLayoutItemGV = (GenericValue) columnLayoutItemGVs.get(2);
        assertEquals(IssueFieldConstants.PRIORITY, columnLayoutItemGV.getString("fieldidentifier"));
        assertEquals(2L, columnLayoutItemGV.getLong("horizontalposition").longValue());
    }

    public void testStoreEditableSearchRequestColumnLayoutNoEntries() throws ColumnLayoutStorageException, GenericEntityException
    {
        clm.restoreSearchRequestColumnLayout(searchRequest);

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final GenericValue columnLayoutGV = EntityUtil.getOnly(genericDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null,
            "searchrequest", searchRequest.getId())));
        assertNull(columnLayoutGV);
    }

    public void testStoreEditableSearchRequestColumnLayoutWithEntries() throws ColumnLayoutStorageException, GenericEntityException
    {
        // Create exiting search request
        int position = 0;
        final Long filterId = searchRequest.getId();
        GenericValue columnLayoutGV = EntityUtils.createValue("ColumnLayout", EasyMap.build("username", null, "searchrequest", filterId));
        final Long columnLayoutId = columnLayoutGV.getLong("id");
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.DESCRIPTION, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.RESOLUTION, "horizontalposition", new Long(position++)));
        EntityUtils.createValue("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayoutGV.getLong("id"), "fieldidentifier",
            IssueFieldConstants.REPORTER, "horizontalposition", new Long(position++)));
        clm.restoreSearchRequestColumnLayout(searchRequest);

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();

        columnLayoutGV = EntityUtil.getOnly(genericDelegator.findByAnd("ColumnLayout", EasyMap.build("username", null, "searchrequest",
            searchRequest.getId())));
        assertNull(columnLayoutGV);

        final List columnLayoutItemGVs = genericDelegator.findByAnd("ColumnLayoutItem", EasyMap.build("id", columnLayoutId));
        assertTrue(columnLayoutItemGVs.isEmpty());
    }

}
