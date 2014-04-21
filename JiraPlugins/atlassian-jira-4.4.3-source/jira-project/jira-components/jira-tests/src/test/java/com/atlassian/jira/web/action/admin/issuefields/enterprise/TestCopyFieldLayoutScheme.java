/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.LinkedList;

public class TestCopyFieldLayoutScheme extends LegacyJiraMockTestCase
{
    private GenericValue scheme;
    GenericValue fieldLayout;
    GenericValue fieldLayoutItem;

    private Mock mockFieldLayoutManager;
    private CopyFieldLayoutScheme cfls;
    private String existingName;

    public TestCopyFieldLayoutScheme(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.setStrict(true);

        cfls = new CopyFieldLayoutScheme((FieldLayoutManager) mockFieldLayoutManager.proxy());
        existingName = "Name";
        scheme = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("name", existingName, "description", "Description"));

        fieldLayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("layoutscheme", scheme.getLong("id")));
        fieldLayoutItem = UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "fieldidentifier", IssueFieldConstants.DESCRIPTION, "verticalposition", new Long(0), "ishidden", Boolean.TRUE.toString(), "isrequired", Boolean.FALSE.toString()));
    }

    protected void tearDown() throws Exception
    {
        // Null references to consume less memory while tests a running, especially when the whole test quite is executed in one JVM
        cfls = null;
        mockFieldLayoutManager = null;
        super.tearDown();
    }

    public void testGetsAndSets()
    {
        assertNull(cfls.getId());
        Long id = new Long(1);
        String name = "Test Name";
        String description = "Test Description";

        cfls.setId(id);
        cfls.setFieldLayoutSchemeName(name);
        cfls.setFieldLayoutSchemeDescription(description);
        assertEquals(id, cfls.getId());
        assertEquals(name, cfls.getFieldLayoutSchemeName());
        assertEquals(description, cfls.getFieldLayoutSchemeDescription());
    }

    public void testDoDefaultNoId() throws Exception
    {
        _testNoId(false);
    }

    public void testDoDefaultInvalidId() throws Exception
    {
        _testInvalidId(false);
    }

    public void testDoDefault() throws Exception
    {
        Long id = scheme.getLong("id");
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        mockFieldLayoutScheme.expectAndReturn("getName", existingName);
        String description = "Test Description";
        mockFieldLayoutScheme.expectAndReturn("getDescription", description);

        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), mockFieldLayoutScheme.proxy());
        cfls.setId(id);

        assertEquals(Action.INPUT, cfls.doDefault());
        assertEquals("Copy of " + existingName , cfls.getFieldLayoutSchemeName());
        assertEquals(description , cfls.getFieldLayoutSchemeDescription());

        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationNoName() throws Exception
    {
        String result = cfls.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, cfls.getErrors().size());
        assertEquals("You must enter a valid name.", cfls.getErrors().get("fieldLayoutSchemeName"));
    }

    public void testDoValidationNoId() throws Exception
    {
        _testNoId(true);
    }

    private void _testNoId(boolean execute) throws Exception
    {
        String name = "Test Scheme";
        cfls.setFieldLayoutSchemeName(name);

        if (execute)
            assertEquals(Action.INPUT, cfls.execute());
        else
            assertEquals(Action.INPUT, cfls.doDefault());

        assertEquals(1, cfls.getErrorMessages().size());
        assertEquals("Id is required.", cfls.getErrorMessages().iterator().next());
    }

    public void testDoValidationExistingName() throws Exception
    {
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        mockFieldLayoutScheme.expectAndReturn("getName", existingName);
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", EasyList.build(mockFieldLayoutScheme.proxy()));

        cfls.setFieldLayoutSchemeName(existingName);
        cfls.setId(new Long(1));
        String result = cfls.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, cfls.getErrors().size());
        assertEquals("A Field Configuration Scheme with this name already exists.", cfls.getErrors().get("fieldLayoutSchemeName"));

        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoValidationInvalidId() throws Exception
    {
        _testInvalidId(true);
    }

    private void _testInvalidId(boolean execute) throws Exception
    {
        Long id = new Long(1);
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);

        if (execute)
            mockFieldLayoutScheme.expectAndReturn("getName", existingName);

        if (execute)
            mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", EasyList.build(mockFieldLayoutScheme.proxy()));

        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), null);
        cfls.setFieldLayoutSchemeName("Test Name");

        cfls.setId(id);
        if (execute)
            assertEquals(Action.INPUT, cfls.execute());
        else
            assertEquals(Action.INPUT, cfls.doDefault());
        assertEquals(1, cfls.getErrorMessages().size());
        assertEquals("Invalid id '" + id + "'.", cfls.getErrorMessages().iterator().next());

        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoExecute() throws Exception
    {
        Long id = scheme.getLong("id");

        Mock mockFieldLayoutSchemeEntity = new Mock(FieldLayoutSchemeEntity.class);
        mockFieldLayoutSchemeEntity.setStrict(true);
        mockFieldLayoutSchemeEntity.expectAndReturn("getIssueTypeId", null);
        mockFieldLayoutSchemeEntity.expectAndReturn("getFieldLayoutId", null);

        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        mockFieldLayoutScheme.expectAndReturn("getName", existingName);
        mockFieldLayoutScheme.expectAndReturn("getEntities", EasyList.build(mockFieldLayoutSchemeEntity.proxy()));
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemes", EasyList.build(mockFieldLayoutScheme.proxy()));
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), mockFieldLayoutScheme.proxy());
        mockFieldLayoutManager.expectAndReturn("createFieldLayoutScheme", P.args(new IsAnything()), null);
        mockFieldLayoutManager.expectAndReturn("getFieldLayoutSchemeEntities", P.args(new IsAnything()), new LinkedList());
        mockFieldLayoutManager.expectVoid("createFieldLayoutSchemeEntity", P.args(new IsAnything()));
        cfls.setFieldLayoutSchemeName("Test Name");

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");

        cfls.setId(id);
        String result = cfls.execute();
        assertEquals(Action.NONE, result);
        mockFieldLayoutSchemeEntity.verify();
        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
        response.verify();
    }
}
