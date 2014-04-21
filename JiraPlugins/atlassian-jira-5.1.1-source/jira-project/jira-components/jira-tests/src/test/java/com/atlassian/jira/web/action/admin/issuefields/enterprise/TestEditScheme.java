/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

import java.util.Collections;

public class TestEditScheme extends LegacyJiraMockTestCase
{
    private EditFieldLayoutScheme efls;
    Mock mockFieldLayoutManager;

    public TestEditScheme(String s)
    {
        super(s);
    }


    protected void setUp() throws Exception
    {
        super.setUp();
        mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.setStrict(true);
        efls = new EditFieldLayoutScheme((FieldLayoutManager) mockFieldLayoutManager.proxy());
    }

    public void testGetsSets()
    {
        Long id = new Long(1);
        efls.setId(id);
        assertEquals(id, efls.getId());
        String name = "Test Name";
        efls.setFieldLayoutSchemeName(name);
        assertEquals(name, efls.getFieldLayoutSchemeName());
        String description = "Test Description";
        efls.setFieldLayoutSchemeDescription(description);
        assertEquals(description, efls.getFieldLayoutSchemeDescription());
    }

    public void testDoDefaultNoId() throws Exception
    {
        assertEquals(Action.INPUT, efls.doDefault());
        checkSingleElementCollection(efls.getErrorMessages(), "Id is required.");
    }

    public void testDoDefaultInvalidId() throws Exception
    {
        Long id = new Long(1);
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), null);
        efls.setId(id);
        assertEquals(Action.INPUT, efls.doDefault());
        checkSingleElementCollection(efls.getErrorMessages(), "Invalid id '" + id + "'.");
        mockFieldLayoutManager.verify();
    }

    public void testDoDefault() throws Exception
    {
        Long id = new Long(1);
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        String name = "Test Name";
        mockFieldLayoutScheme.expectAndReturn("getName", name);
        String description = "Test Description";
        mockFieldLayoutScheme.expectAndReturn("getDescription", description);
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), mockFieldLayoutScheme.proxy());
        efls.setId(id);
        assertEquals(Action.INPUT, efls.doDefault());
        assertEquals(name, efls.getFieldLayoutSchemeName());
        assertEquals(description, efls.getFieldLayoutSchemeDescription());
        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoValidation() throws Exception
    {
        assertEquals(Action.INPUT, efls.execute());
        assertEquals(1, efls.getErrors().size());
        assertEquals("You must enter a valid name.", efls.getErrors().get("fieldLayoutSchemeName"));
    }

    public void testDoValidationNoId() throws Exception
    {
        efls.setFieldLayoutSchemeName("Some Name");
        assertEquals(Action.INPUT, efls.execute());
        checkSingleElementCollection(efls.getErrorMessages(), "Id is required.");
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationInvalidId() throws Exception
    {
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", Collections.EMPTY_LIST);
        Long id = new Long(1);
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), null);
        efls.setFieldLayoutSchemeName("Some Name");
        efls.setId(id);
        assertEquals(Action.INPUT, efls.execute());
        checkSingleElementCollection(efls.getErrorMessages(), "Invalid id '" + id + "'.");
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationDuplicateName() throws Exception
    {
        Mock mockFieldLayoutShceme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutShceme.setStrict(true);
        String name = "some name";
        mockFieldLayoutShceme.expectAndReturn("getName", name);
        // Return a different id to cause an error
        mockFieldLayoutShceme.expectAndReturn("getId", new Long(2));
        Long id = new Long(1);
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutShceme.proxy();
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", EasyList.build(fieldLayoutScheme));
        efls.setFieldLayoutSchemeName(name);
        efls.setId(id);
        assertEquals(Action.INPUT, efls.execute());
        assertEquals(1, efls.getErrors().size());
        assertEquals("A field configuration scheme with this name already exists.", efls.getErrors().get("fieldLayoutSchemeName"));
        mockFieldLayoutShceme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");
        Mock mockFieldLayoutShceme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutShceme.setStrict(true);
        String name = "some name";
        mockFieldLayoutShceme.expectVoid("setName", P.args(new IsEqual(name)));
        String description = "Test Description";
        mockFieldLayoutShceme.expectVoid("setDescription", P.args(new IsEqual(description)));
        mockFieldLayoutShceme.expectVoid("store");
        Long id = new Long(1);
        mockFieldLayoutShceme.expectAndReturn("getId", id);
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutShceme.proxy();
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", EasyList.build(fieldLayoutScheme));
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), fieldLayoutScheme);
        efls.setFieldLayoutSchemeName(name);
        efls.setFieldLayoutSchemeDescription(description);
        efls.setId(id);
        assertEquals(Action.NONE, efls.execute());
        response.verify();
        mockFieldLayoutShceme.verify();
        mockFieldLayoutManager.verify();
    }
}
