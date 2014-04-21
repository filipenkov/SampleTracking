/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.bean.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;

public class TestFieldVisibilityBean extends com.atlassian.jira.web.bean.TestFieldVisibilityBean
{
    public TestFieldVisibilityBean(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        fieldLayoutScheme1 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("name", "Test Scheme 1"));
        fieldLayoutScheme2 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("name", "Test Scheme 2"));
    }

    private void setupLayoutAssociation(String issueTypeId) throws GenericEntityException
    {
        // Setup fieldlayoutscheme with hidden field (components) - create associations between:
        //  1. FieldLayoutSchemeAssociation
        //  2. FieldLayoutScheme
        //  3. FieldLayout
        //  4. FieldLayoutItem
        fieldLayout1 = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Field Layout 1"));
        fieldLayout2 = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Field Layout 1"));

        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout1.getLong("id"), "fieldidentifier", IssueFieldConstants.COMPONENTS, "verticalposition", new Long(1), "ishidden", Boolean.TRUE.toString(), "isrequired", Boolean.FALSE.toString()));


        UtilsForTests.getTestEntity("FieldLayoutSchemeEntity", EasyMap.build("scheme", fieldLayoutScheme1.getLong("id"), "issuetype", issueTypeId, "fieldlayout", fieldLayout1.getLong("id")));


        FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        FieldLayoutScheme fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(fieldLayoutScheme1.getLong("id"));
        fieldLayoutManager.addSchemeAssociation(project, fieldLayoutScheme.getId());
    }

    // FieldLayout scheme with a field (components) hidden and associated with a specific issue type.
    public void testIsFieldHiddenWithCustomLayoutAssocaitedWithOneIssueType() throws GenericEntityException
    {
        setupLayoutAssociation(issueType1.getString("id"));
        UtilsForTests.getTestEntity("FieldLayoutSchemeEntity", EasyMap.build("scheme", fieldLayoutScheme1.getLong("id"), "issuetype", null, "fieldlayout", fieldLayout2.getLong("id")));

        FieldVisibilityManager manager = new FieldVisibilityBean();

        // Check ALL issue types
        // Should return true (hidden) as components is hidden in a scheme associated with the project.
        assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));

        // Check each issue type individually
        Collection issuetypes = ComponentAccessor.getConstantsManager().getAllIssueTypes();
        for (Iterator iterator = issuetypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            if (issueType.getString("id").equals(issueType1.get("id")))
                assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
            else
                assertFalse(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
        }
    }

    // FieldLayout scheme with a field (components) hidden and associated with all issue types.
    public void testIsFieldHiddenWithCustomLayoutAssocaitedWithAllIssueTypes() throws GenericEntityException
    {
        setupLayoutAssociation(null);

        FieldVisibilityManager manager = new FieldVisibilityBean();

        // Check ALL issue types
        // Should return true (hidden) as components is hidden in a scheme associated with the project.
        assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));

        // Check each issue type individually
        // Should return true (hidden) as components is hidden for all issue types.
        Collection issuetypes = ComponentAccessor.getConstantsManager().getAllIssueTypes();
        for (Iterator iterator = issuetypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
        }
    }

    // Multiple FieldLayout schemes - one with a field (components) hidden and associated with one issue - other with components shown and associated with all issue types.
    public void testIsFieldHiddenWithMultipleCustomLayouts() throws GenericEntityException
    {
        setupLayoutAssociation(issueType1.getString("id"));

        // Create another scheme association where components is not hidden
        // Associated with all (other) issue types
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout2.getLong("id"), "fieldidentifier", IssueFieldConstants.COMPONENTS, "verticalposition", new Long(1), "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.FALSE.toString()));
        UtilsForTests.getTestEntity("FieldLayoutSchemeEntity", EasyMap.build("scheme", fieldLayoutScheme1.getLong("id"), "issuetype", null, "fieldlayout", fieldLayout2.getLong("id")));

        FieldVisibilityManager manager = new FieldVisibilityBean();

        // Check ALL issue types
        // Should return true (hidden) as components is hidden in a scheme associated with the project.
        assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));

        // Check each issue type individually
        Collection issuetypes = ComponentAccessor.getConstantsManager().getAllIssueTypes();
        for (Iterator iterator = issuetypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            if (issueType.getString("id").equals(issueType1.get("id")))
                assertTrue(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
            else
                assertFalse(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
        }
    }

    public void testIsFieldHiddenWithCustomLayoutWithNothingHidden()
    {
        FieldVisibilityManager manager = new FieldVisibilityBean();

        // Check ALL issue types
        assertFalse(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, FieldVisibilityManager.ALL_ISSUE_TYPES));

        // Check each issue type individually
        Collection issuetypes = ComponentAccessor.getConstantsManager().getAllIssueTypes();
        for (Iterator iterator = issuetypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            assertFalse(manager.isFieldHidden(project.getLong("id"), IssueFieldConstants.COMPONENTS, issueType.getString("id")));
        }
    }
}
