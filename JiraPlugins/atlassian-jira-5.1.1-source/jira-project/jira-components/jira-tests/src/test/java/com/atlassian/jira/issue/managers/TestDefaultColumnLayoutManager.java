/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.DefaultFieldManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.column.AbstractColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.ofbiz.OfBizFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class TestDefaultColumnLayoutManager extends AbstractUsersTestCase
{
    protected DefaultFieldManager defaultFieldManager;

    protected FieldLayoutManager flm;
    protected ColumnLayoutManager clm;
    protected GenericValue cf1;
    protected GenericValue cf2;
    protected GenericValue cf3;
    protected GenericValue cf4;
    protected GenericValue project;
    protected GenericValue project2;
    protected GenericValue issueType;
    protected GenericValue issueType2;
    protected User testUser;
    protected Mock permissionManager;
    protected GenericValue fieldLayout;
    protected List projects;
    protected List<Project> projectObjs;
    protected GenericValue columnLayout;

    private PermissionManager oldPermissionManager;
    private boolean licenceSetup = false;

    public TestDefaultColumnLayoutManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        defaultFieldManager = new DefaultFieldManager(null);

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "BCD"));
        projects = EasyList.build(project, project2);
        projectObjs = EasyList.build(new ProjectImpl(project), new ProjectImpl(project2));
        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "iType1", "sequence", new Long(1)));
        issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "iType2", "sequence", new Long(2)));

        cf1 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Custom Field 1"));
        cf2 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(2), "name", "Custom Field 2", "project", project.getLong("id")));
        cf3 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(3), "name", "Custom Field 3", "issuetype",
            issueType.getString("id")));
        cf4 = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(4), "name", "Custom Field 4", "issuetype",
            issueType2.getString("id")));

        permissionManager = new Mock(PermissionManager.class);

        testUser = createMockUser("testUser");
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(project),
            new IsAnything()), Boolean.TRUE);
        permissionManager.setStrict(true);
        oldPermissionManager = (PermissionManager) ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy()).getComponentInstance();

        clm = new AbstractColumnLayoutManager(defaultFieldManager, OfBizFactory.getOfBizDelegator())
        {

            @Override
            public boolean hasColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException
            {
                return false;
            }

            @Override
            public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(com.atlassian.crowd.embedded.api.User user, SearchRequest searchRequest)
                    throws ColumnLayoutStorageException
            {
                return null;
            }

            @Override
            public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout)
                    throws ColumnLayoutStorageException
            {
            }

            @Override
            public void restoreSearchRequestColumnLayout(SearchRequest searchRequest)
                    throws ColumnLayoutStorageException
            {
            }
        };
        flm = defaultFieldManager.getFieldLayoutManager();
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        defaultFieldManager = null;
        clm = null;
        flm = null;
        super.tearDown();
    }

    protected int setupDefaultFieldLayout(int position)
    {
        fieldLayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("type", FieldLayoutManager.TYPE_DEFAULT));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ISSUE_TYPE, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.TRUE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.DUE_DATE, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.TRUE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.PRIORITY, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.COMPONENTS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.AFFECTED_VERSIONS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.FIX_FOR_VERSIONS, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "verticalposition", new Long(position++), "ishidden", Boolean.TRUE.toString(), "isrequired",
            Boolean.TRUE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ENVIRONMENT, "verticalposition", new Long(position++), "ishidden", Boolean.TRUE.toString(), "isrequired",
            Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.SUMMARY, "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired",
            Boolean.TRUE.toString()));
        return position;
    }

    protected int setupCustomFieldLayoutItems(int position)
    {
        position = setupDefaultFieldLayout(position) + 1;
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf1.getLong("id")).getId(), "verticalposition", new Long(position++),
                "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
                ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf2.getLong("id")).getId(), "verticalposition", new Long(position++),
                "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier",
            ComponentAccessor.getCustomFieldManager().getCustomFieldObject(cf3.getLong("id")).getId(), "verticalposition", new Long(position++),
            "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
        return position;
    }

    protected int setupObsoleteFieldLayoutItems(int position)
    {
        position = setupCustomFieldLayoutItems(position);
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier", "random_string",
            "verticalposition", new Long(position++), "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        return position;
    }

    protected int setupObsoleteCustomFieldColumnItems(final User user, int position)
    {
        position = setupColumnLayoutWithHiddenColumn(user, position);
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            FieldManager.CUSTOM_FIELD_PREFIX + "1000", "horizontalposition", new Long(position++)));
        return position;
    }

    protected int setupColumnLayoutWithHiddenColumn(final User user, int position)
    {
        final String username = user != null ? user.getName() : null;

        // Setup user navigator columns with one hidden column
        columnLayout = UtilsForTests.getTestEntity("ColumnLayout", EasyMap.build("username", username));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ISSUE_KEY, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.SUMMARY, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ENVIRONMENT, "horizontalposition", new Long(position++)));
        UtilsForTests.getTestEntity("ColumnLayoutItem", EasyMap.build("columnlayout", columnLayout.getLong("id"), "fieldidentifier",
            IssueFieldConstants.ASSIGNEE, "horizontalposition", new Long(position++)));
        return position;
    }

    public void testObsoleteCustomFieldLayoutItemsAreIgnored() throws ColumnLayoutStorageException
    {
        // Setup permissions
        permissionManager.expectAndReturn("getProjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(testUser)), projects);

        // Setup fields
        setupObsoleteCustomFieldColumnItems(null, 0);

        final List columnLayoutItems = clm.getEditableDefaultColumnLayout().getColumnLayoutItems();
        assertEquals(4, columnLayoutItems.size());
    }

    public void testHiddenFieldsNotShown() throws ColumnLayoutStorageException
    {
        // Setup permissions
        permissionManager.expectAndReturn("getProjectObjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(testUser)), projectObjs);

        // Setup fields with environment hiodden
        setupDefaultFieldLayout(0);
        setupColumnLayoutWithHiddenColumn(testUser, 0);

        final List columnLayoutItems = clm.getColumnLayout(testUser).getColumnLayoutItems();
        _testFieldMissing(defaultFieldManager.getField(IssueFieldConstants.ENVIRONMENT), columnLayoutItems);
    }

    private void _testFieldMissing(final Field field, final List columnLayoutItems)
    {
        for (int i = 0; i < columnLayoutItems.size(); i++)
        {
            final ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) columnLayoutItems.get(i);
            if (columnLayoutItem.getNavigableField().equals(field))
            {
                fail("The field '" + field.getNameKey() + "' should not be present in the list.");
            }
        }
    }

}
