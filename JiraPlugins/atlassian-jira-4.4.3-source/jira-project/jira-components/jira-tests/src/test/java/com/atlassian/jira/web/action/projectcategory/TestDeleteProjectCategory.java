/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;

import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

public class TestDeleteProjectCategory extends LegacyJiraMockTestCase
{
    public DeleteProjectCategory dpc;
    private final MockControl ctrlCustomFieldManager = MockControl.createControl(CustomFieldManager.class);
    private final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) ctrlCustomFieldManager.getMock();

    public TestDeleteProjectCategory(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
        dpc = new DeleteProjectCategory(null, null, mockCustomFieldManager);
    }

    public void testGetsSets()
    {
        assertFalse(dpc.isConfirm());
        assertNull(dpc.getId());

        dpc.setConfirm(true);
        dpc.setId(new Long(1));

        assertTrue(dpc.isConfirm());
        assertEquals(new Long(1), dpc.getId());
    }

    /**
     * Test to make sure that confirm has been clicked.
     */
    public void testValidation1() throws Exception
    {
        setupValidData();

        final String result = dpc.execute();

        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(dpc.getErrorMessages(), "You must confirm deleting a project category.");
    }

    /**
     * Test to make sure that an id is passed in
     */
    public void testValidation2() throws Exception
    {
        dpc.setConfirm(true);

        final String result = dpc.execute();

        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(dpc.getErrorMessages(), "You must specify a project category to delete.");
    }

    /**
     * Test validation for when an id passed in but no project category with that id
     */
    public void testValidation3() throws Exception
    {
        dpc.setId(new Long(1));
        dpc.setConfirm(true);

        final String result = dpc.execute();

        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(dpc.getErrorMessages(), "You must specify a project category to delete.");
    }

    /**
     * Test to make sure that project category is not deleted when projects are still linked to it.
     */
    public void testValidation4() throws Exception
    {
        final GenericValue project = EntityUtils.createValue("Project", EasyMap.build("id", new Long(1)));
        final GenericValue projCat = EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        final Mock mock = new Mock(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mock.proxy());
        mock.setupResult("getProjectsFromProjectCategory", EasyList.build(project));
        mock.setupResult("getProjectCategory", projCat);

        dpc.setId(new Long(1));
        dpc.setConfirm(true);
        assertEquals(Action.INPUT, dpc.execute());

        checkSingleElementCollection(dpc.getErrorMessages(), "There are currently projects linked to this category.");
    }

    /**
     * Executes fine and removes the project Category
     */
    public void testExecuteFine() throws Exception
    {
        setupValidData();
        dpc.setConfirm(true);

        final List params = EasyList.build(EasyMap.build("id", new Long(1)));
        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewProjectCategories!default.jspa");

        final String result = dpc.execute();
        assertEquals(Action.NONE, result);

        assertEquals(1, mad.getActionsCalled().size());
        assertTrue(mad.getActionsCalled().contains(ActionNames.PROJECTCATEGORY_DELETE));
        assertEquals(params, mad.getParametersCalled());

        response.verify();
    }

    /**
     * Actually remove the project category.
     */
    public void testExecuteFine2() throws Exception, IOException
    {
        setupValidData();
        dpc.setConfirm(true);

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewProjectCategories!default.jspa");

        assertEquals(1, ManagerFactory.getProjectManager().getProjectCategories().size());

        final String result = dpc.execute();
        assertEquals(Action.NONE, result);

        assertEquals(0, ManagerFactory.getProjectManager().getProjectCategories().size());
    }

    private void setupValidData() throws GenericEntityException
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        dpc.setId(new Long(1));
    }
}
