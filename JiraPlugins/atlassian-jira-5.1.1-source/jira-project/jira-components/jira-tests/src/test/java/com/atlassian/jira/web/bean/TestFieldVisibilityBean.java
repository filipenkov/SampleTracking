/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.bean;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.local.AbstractUsersTestCase;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;

public class TestFieldVisibilityBean extends AbstractUsersTestCase
{
    protected GenericValue project;
    protected GenericValue fieldLayoutScheme1;
    protected GenericValue fieldLayoutScheme2;
    protected GenericValue fieldLayout1;
    protected GenericValue fieldLayout2;
    protected GenericValue fieldLayoutItem;
    protected GenericValue issueType1;
    protected GenericValue issueType2;

    public TestFieldVisibilityBean(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));        

        issueType1 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "sequence", new Long(1)));
        issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "New Feature", "sequence", new Long(2)));
    }

    public void testIsFieldHiddenWithDefaultLayout()
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
