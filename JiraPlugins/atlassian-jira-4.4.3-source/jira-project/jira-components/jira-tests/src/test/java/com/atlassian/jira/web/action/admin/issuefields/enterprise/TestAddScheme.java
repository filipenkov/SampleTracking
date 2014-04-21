/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;


import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

import java.util.LinkedList;


public class TestAddScheme extends LegacyJiraMockTestCase
{
    private ViewSchemes vs;
    private Mock mockFieldLayoutManager;

    public TestAddScheme(String s)
    {
        super(s);
    }


    protected void setUp() throws Exception
    {
        super.setUp();
        mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.setStrict(true);
        vs = new ViewSchemes((FieldLayoutManager) mockFieldLayoutManager.proxy());
    }

    public void testGetsAndSets()
    {
        String name = "Test Name";
        vs.setFieldLayoutSchemeName(name);
        assertEquals(name, vs.getFieldLayoutSchemeName());
        String description = "Test Description";
        vs.setFieldLayoutSchemeDescription(description);
        assertEquals(description, vs.getFieldLayoutSchemeDescription());
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationNoName() throws Exception
    {
        String name = "Test Name";
        mockFieldLayoutManager.expectAndReturn("fieldConfigurationSchemeExists", P.args(new IsEqual(name)), true);
        vs.setFieldLayoutSchemeName(name);
        assertEquals(Action.ERROR, vs.doAddScheme());
        assertEquals(1, vs.getErrors().size());
        assertEquals("A Field Configuration Scheme with this name already exists.", vs.getErrors().get("fieldLayoutSchemeName"));
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationDuplicateName() throws Exception
    {
        assertEquals(Action.ERROR, vs.doAddScheme());
        assertEquals(1, vs.getErrors().size());
        assertEquals("You must enter a valid name.", vs.getErrors().get("fieldLayoutSchemeName"));
        mockFieldLayoutManager.verify();
    }

    public void testDoAdd() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");
        String name = "Test Name";
        mockFieldLayoutManager.expectAndReturn("fieldConfigurationSchemeExists", P.args(new IsEqual(name)), false);
        mockFieldLayoutManager.expectAndReturn("createFieldLayoutScheme", P.args(new IsAnything()), null);
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemeEntities", P.args(new IsAnything()), new LinkedList());
        mockFieldLayoutManager.expectVoid("createFieldLayoutSchemeEntity", P.args(new IsAnything()));
        vs.setFieldLayoutSchemeName(name);
        assertEquals(Action.NONE, vs.doAddScheme());
        response.verify();
        mockFieldLayoutManager.verify();
    }
}
