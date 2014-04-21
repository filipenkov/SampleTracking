/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.projectcategory;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.List;

public class TestEditProjectCategory extends LegacyJiraMockTestCase
{
    public EditProjectCategory epc;

    public TestEditProjectCategory(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
        epc = new EditProjectCategory();
    }

    public void testGetsSets()
    {
        assertNull(epc.getName());
        assertNull(epc.getId());
        assertNull(epc.getDescription());

        epc.setName("foo");
        epc.setDescription("bar");
        epc.setId(new Long(1));

        assertEquals("foo", epc.getName());
        assertEquals("bar", epc.getDescription());
        assertEquals(new Long(1), epc.getId());
    }

    /**
     * make sure the id is passed in on default
     * @throws Exception
     */
    public void testDoDefaultCheck() throws Exception
    {
        epc.doDefault();
        checkSingleElementCollection(epc.getErrorMessages(), "The project category does not exist.");

        epc.setId(new Long(1));
        checkSingleElementCollection(epc.getErrorMessages(), "The project category does not exist.");
    }

    /**
     * make sure the name and description is set when the default is run if an id is passed in and the category exists
     * @throws Exception
     */
    public void testDoDefaultRun() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        epc.setId(new Long(1));
        epc.doDefault();

        assertEquals("foo", epc.getName());
        assertEquals("bar", epc.getDescription());
    }

    /**
     * make sure validate fails when no id is set
     * @throws Exception
     */
    public void testValdiate() throws Exception
    {
        String result = epc.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(epc.getErrorMessages(), "The project category does not exist.");
    }

    /**
     * Validate should fail if id is set but the category with that id does not exist.
     * @throws Exception
     */
    public void testValidate2() throws Exception
    {
        epc.setId(new Long(1));
        epc.setName("foo");
        epc.setDescription("bar");

        String result = epc.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(epc.getErrorMessages(), "The project category does not exist.");
    }

    /**
     * Make sure a valid name is entered for editing
     * @throws Exception
     */
    public void testValidateName() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        epc.setId(new Long(1));

        String result = epc.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, epc.getErrors().size());
        assertEquals("Please specify a name.", epc.getErrors().get("name"));

        epc.setName("");
        result = epc.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, epc.getErrors().size());
        assertEquals("Please specify a name.", epc.getErrors().get("name"));
    }

    /**
     * Confirm that back-end action is called
     */
    public void testExecuteFine() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        List params = EasyList.build(EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewProjectCategories!default.jspa");

        epc.setId(new Long(1));
        epc.setName("foo");
        epc.setDescription("bar");

        String result = epc.execute();
        assertEquals(Action.NONE, result);

        List actionsCalled = mad.getActionsCalled();
        assertEquals(1, actionsCalled.size());
        assertTrue(actionsCalled.contains(ActionNames.PROJECTCATEGORY_EDIT));
        assertEquals(params, mad.getParametersCalled());

        response.verify();
    }

    /**
     * Confirm that the edit actually works.
     */
    public void testExecuteFineComplete() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewProjectCategories!default.jspa");

        epc.setId(new Long(1));
        epc.setName("new name");
        epc.setDescription("new desc");
        assertEquals(Action.NONE, epc.execute());

        GenericValue retrievedPC = ManagerFactory.getProjectManager().getProjectCategory(new Long(1));
        assertEquals("new name", retrievedPC.getString("name"));
        assertEquals("new desc", retrievedPC.getString("description"));

        response.verify();
    }

    /**
     * Check for duplicates.
     */
    public void testExecuteWithDuplicate() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(2), "name", "a funny name", "description", "a funny description"));

        epc.setId(new Long(2));
        epc.setName("foo");

        assertEquals(Action.INPUT, epc.execute());
        assertEquals(1, epc.getErrors().size());
        assertEquals(0, epc.getErrorMessages().size());
        assertEquals("The project category 'foo' already exists.", epc.getErrors().get("name"));
    }
}
