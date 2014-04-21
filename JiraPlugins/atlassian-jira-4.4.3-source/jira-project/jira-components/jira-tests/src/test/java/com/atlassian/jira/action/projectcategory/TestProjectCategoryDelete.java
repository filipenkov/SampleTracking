/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.projectcategory;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestProjectCategoryDelete extends LegacyJiraMockTestCase
{
    public ProjectCategoryDelete pcd;

    public TestProjectCategoryDelete(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
        pcd = new ProjectCategoryDelete();
    }

    public void testGetsSets()
    {
        assertNull(pcd.getId());

        pcd.setId(new Long(1));
        assertEquals(new Long(1), pcd.getId());
    }

    /**
     * Check that a category id is passed in.
     */
    public void testDoValidate1() throws Exception
    {
        final String result = pcd.execute();

        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(pcd.getErrorMessages(), "You must specify a project category to delete.");
    }

    /**
     * Check that given a category id, the category exists for deletion
     */
    public void testDoValidate2() throws Exception
    {
        pcd.setId(new Long(1));

        final String result = pcd.execute();

        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(pcd.getErrorMessages(), "You must specify a project category to delete.");
    }

    /**
     * Check that if a project is linked to this category, then do not allow category to be deleted.
     */
    public void testDoValidateWithLinkedProjects() throws Exception
    {
        final GenericValue project = EntityUtils.createValue("Project", EasyMap.build("id", new Long(1)));
        final GenericValue projCat = EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        final Mock mock = new Mock(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) mock.proxy());
        mock.setupResult("getProjectsFromProjectCategory", EasyList.build(project));
        mock.setupResult("getProjectCategory", projCat);

        pcd.setId(new Long(1));
        assertEquals(Action.ERROR, pcd.execute());

        checkSingleElementCollection(pcd.getErrorMessages(), "There are currently projects linked to this category.");
    }

    /**
     * Check that the category specified is actually deleted
     */
    public void testExecuteFine() throws Exception
    {
        EntityUtils.createValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "foo", "description", "bar"));

        // just to check that the category does exist
        assertEquals(1, ManagerFactory.getProjectManager().getProjectCategories().size());

        pcd.setId(new Long(1));

        final String result = pcd.execute();

        assertEquals(Action.SUCCESS, result);
        assertEquals(0, ManagerFactory.getProjectManager().getProjectCategories().size());
    }
}
