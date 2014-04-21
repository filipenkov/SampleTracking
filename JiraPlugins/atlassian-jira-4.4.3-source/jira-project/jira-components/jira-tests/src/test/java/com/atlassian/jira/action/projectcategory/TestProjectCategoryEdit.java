/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.projectcategory;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestProjectCategoryEdit extends LegacyJiraMockTestCase
{
    public ProjectCategoryEdit pce;

    public TestProjectCategoryEdit(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
        pce = new ProjectCategoryEdit();
    }

    protected void tearDown() throws Exception
    {
        pce = null;
        ManagerFactory.removeService(ProjectManager.class);
        super.tearDown();
    }

    public void testGetsSets()
    {
        assertNull(pce.getName());
        assertNull(pce.getDescription());
        assertNull(pce.getId());

        pce.setName("foo");
        pce.setDescription("bar");
        pce.setId(new Long(1));

        assertEquals("foo", pce.getName());
        assertEquals("bar", pce.getDescription());
        assertEquals(new Long(1), pce.getId());
    }

    public void testEdit() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        // Take project category 1, and supply a new name and new description
        pce.setId(new Long(1));
        pce.setName("bob");
        pce.setDescription("a new different more exciting creature.");

        // do the transformation
        pce.execute();

        GenericValue bob = ManagerFactory.getProjectManager().getProjectCategory(new Long(1));
        assertEquals("bob", bob.getString("name"));
        assertEquals("a new different more exciting creature.", bob.getString("description"));
    }

    /**
     * Check that an error is created when execute is called without an id, or one which is not valid.
     *
     * @throws Exception
     */
    public void testValidation() throws Exception
    {
        pce.setName("foo");
        pce.setDescription("bar");
        pce.setId(new Long(1));

        String result = pce.execute();

        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(pce.getErrorMessages(), "The project category does not exist.");

        pce.setId(null);
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(pce.getErrorMessages(), "The project category does not exist.");
    }

    /**
     * Check that the name must be a valid string
     */
    public void testValidateName() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        pce.setId(new Long(1));

        String result = pce.execute();
        assertEquals(Action.ERROR, result);
        assertEquals(1, pce.getErrors().size());
        assertEquals("You must enter a valid name.", pce.getErrors().get("name"));
    }

    /**
     * Check for duplicates
     */
    public void testExecuteWithDuplicates() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(2), "name", "another name", "description", "another desc"));

        pce.setId(new Long(2));
        pce.setName("foo");

        String result = pce.execute();
        assertEquals(Action.ERROR, result);

        assertEquals(1, pce.getErrors().size());
        assertEquals("The project category 'foo' already exists.", pce.getErrors().get("name"));
    }
}
