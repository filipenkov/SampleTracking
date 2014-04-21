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

import java.io.IOException;
import java.util.Collections;

public class TestDeleteScheme extends LegacyJiraMockTestCase
{
    private Mock mockFieldLayoutManager;
    private EditFieldLayoutScheme efls;

    public TestDeleteScheme(String s)
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
        String confirm = "confirm";
        efls.setId(id);
        efls.setConfirm(confirm);
        assertEquals(id, efls.getId());
        assertEquals(confirm, efls.getConfirm());
    }

    public void testDoDeleteNoId()
    {
        assertEquals(Action.ERROR, efls.doDeleteScheme());
        checkSingleElementCollection(efls.getErrorMessages(), "Id is required.");
    }

    public void testDoDeleteInvalidId()
    {
        Long id = new Long(1);
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), null);
        efls.setId(id);
        assertEquals(Action.ERROR, efls.doDeleteScheme());
        checkSingleElementCollection(efls.getErrorMessages(), "Invalid id '" + id + "'.");
        mockFieldLayoutManager.verify();
    }

    public void testDoDeleteHasAssociatedProjects()
    {
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        Long id = new Long(1);
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutScheme.proxy();
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), fieldLayoutScheme);
        mockFieldLayoutManager.expectAndReturn("getProjects", P.args(new IsEqual(fieldLayoutScheme)), EasyList.build(new Object()));
        efls.setId(id);
        assertEquals(Action.ERROR, efls.doDeleteScheme());
        checkSingleElementCollection(efls.getErrorMessages(), "Cannot delete field configuration scheme as it is used by at least one project.");
        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoDeleteConfirm()
    {
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        Long id = new Long(1);
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutScheme.proxy();
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), fieldLayoutScheme);
        mockFieldLayoutManager.expectAndReturn("getProjects", P.args(new IsEqual(fieldLayoutScheme)), Collections.EMPTY_LIST);
        efls.setId(id);
        assertEquals("confirm", efls.doDeleteScheme());
        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }

    public void testDoDelete() throws IOException
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");
        Mock mockFieldLayoutScheme = new Mock(FieldLayoutScheme.class);
        mockFieldLayoutScheme.setStrict(true);
        mockFieldLayoutScheme.expectVoid("remove");
        Long id = new Long(1);
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) mockFieldLayoutScheme.proxy();
        mockFieldLayoutManager.expectAndReturn("getMutableFieldLayoutScheme", P.args(new IsEqual(id)), fieldLayoutScheme);
        mockFieldLayoutManager.expectAndReturn("getProjects", P.args(new IsEqual(fieldLayoutScheme)), Collections.EMPTY_LIST);
        efls.setId(id);
        efls.setConfirm("true");
        assertEquals(Action.NONE, efls.doDeleteScheme());
        response.verify();
        mockFieldLayoutScheme.verify();
        mockFieldLayoutManager.verify();
    }
}
