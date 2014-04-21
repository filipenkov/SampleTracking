/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public class TestViewSchemes extends LegacyJiraMockTestCase
{
    GenericValue project1;
    GenericValue project2;
    GenericValue fieldLayoutScheme1;
    GenericValue fieldLayoutScheme2;
    GenericValue association;
    Mock mockFieldLayoutManager;

    public TestViewSchemes(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();


        mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.setStrict(true);

        // Project
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "CDE"));

        // Create a copy of project scheme
        fieldLayoutScheme1 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("id", new Long(1), "name", "Scheme 1", "description", "Desc 1"));
        fieldLayoutScheme2 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("id", new Long(2), "name", "Scheme 2", "description", "Desc 2"));
    }

    public void testGetIssueFieldSchemes()
    {
        ViewSchemes viewSchemes = new ViewSchemes((FieldLayoutManager) mockFieldLayoutManager.proxy());
        List fieldLayoutSchemes = EasyList.build(new Object(), new Object());
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", fieldLayoutSchemes);

        List issueFieldSchemes = viewSchemes.getFieldLayoutScheme();
        assertEquals(2, issueFieldSchemes.size());
        assertEquals(fieldLayoutSchemes, issueFieldSchemes);

        mockFieldLayoutManager.verify();
    }

    public void testGetSchemeProjects()
    {
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        mockFieldLayoutScheme.expectAndReturn("getId", new Long(1));

        List projects = EasyList.build(new MockGenericValue("Project"), new MockGenericValue("Project"));
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutScheme.proxy();
        mockFieldLayoutManager.expectAndReturn("getProjects", P.args(new IsEqual(fieldLayoutScheme)), projects);

        ViewSchemes viewSchemes = new ViewSchemes((FieldLayoutManager) mockFieldLayoutManager.proxy());

        Collection schemeProjects = viewSchemes.getSchemeProjects(fieldLayoutScheme);
        assertEquals(projects, schemeProjects);
    }
}
