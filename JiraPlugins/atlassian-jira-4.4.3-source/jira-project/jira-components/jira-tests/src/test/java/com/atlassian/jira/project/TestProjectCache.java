/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class TestProjectCache extends LegacyJiraMockTestCase
{
    private ProjectCache pCache;
    private GenericValue project1;
    private GenericValue project2;
    public GenericValue projectCat;

    public TestProjectCache(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        pCache = new ProjectCache(true);
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10), "key", "ABC-123", "name", "This Project", "counter", new Long(10)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(11), "key", "ABC-124", "name", "This Project 2", "counter", new Long(10)));
        UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(10), "name", "com1", "project", new Long(10)));
        UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(11), "name", "com2", "project", new Long(10)));

        projectCat = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("id", new Long(30), "name", "cat1", "description", "cat1Description"));

        pCache.refresh();
    }

    public void testUpdateCache() throws GenericEntityException
    {
        GenericValue oldProject = pCache.getProject(project1.getLong("id"));
        pCache.refresh();

        GenericValue newProject = pCache.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);
    }

    public void testCachingProjectManager() throws GenericEntityException
    {
        GenericValue oldProject = pCache.getProject(project1.getLong("id"));

        pCache.refreshProjectDependencies(project1);

        GenericValue newProject = pCache.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);
    }

    public void testGetProject() throws GenericEntityException
    {
        GenericValue project = pCache.getProject(new Long(10));
        assertTrue(project == pCache.getProject(new Long(10)));
        assertTrue(project == pCache.getProjectByKey("ABC-123"));
        assertTrue(project == pCache.getProjectByName("This Project"));
        assertNull(pCache.getProject(new Long(9)));
        assertNull(pCache.getProjectByKey("ABC-1231"));
        assertNull(pCache.getProjectByName("This Project1"));
    }

    public void testGetProjects() throws GenericEntityException
    {
        Collection projects = pCache.getProjects();
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));
    }

    public void testGetProjectCategories()
    {
        Collection projectCategories = pCache.getProjectCategories();
        assertNotNull(projectCategories);
        assertEquals(1, projectCategories.size());
        assertTrue(projectCategories.contains(projectCat));
    }

    public void testRefreshRefreshesCategories()
    {
        final VerifyObject verifyObject = new VerifyObject();

        ProjectCache projectCache = new ProjectCache(false)
        {
            protected void refreshProjectCategories()
            {
                verifyObject.setCalled();
                super.refreshProjectCategories();
            }
        };

        projectCache.refresh();
        assertTrue(verifyObject.verify());
    }

    public void testRefreshProjectCategoryMaps() throws GenericEntityException
    {
        // want to make sure that the refresh actually does work.
        // on BOTH the maps within the cache;
        // - the one which maps project to projectCategory
        // - and the one which maps projectCategory to projects
        CoreFactory.getAssociationManager().createAssociation(project1, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);
        pCache.refresh();
        assertEquals(projectCat, pCache.getProjectCategoryFromProject(project1));
        assertEquals(EasyList.build(project1), pCache.getProjectsFromProjectCategory(projectCat));
    }

    /**
     * Test get ProjectCategoriesFromProject
     * Essentially the same test as the one above which checks that the refresh works
     */
    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        assertNull(pCache.getProjectCategoryFromProject(project1));
        assertTrue(pCache.getProjectsFromProjectCategory(projectCat).isEmpty());

        CoreFactory.getAssociationManager().createAssociation(project1, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);
        CoreFactory.getAssociationManager().createAssociation(project2, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);

        pCache.refresh();
        assertEquals(projectCat, pCache.getProjectCategoryFromProject(project1));
        assertEquals(projectCat, pCache.getProjectCategoryFromProject(project2));

        Collection projects = pCache.getProjectsFromProjectCategory(projectCat);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));
    }

    private class VerifyObject
    {
        private boolean called;

        public void setCalled()
        {
            this.called = true;
        }

        public boolean verify()
        {
            return called;
        }
    }

    public void testGetCategory()
    {
        assertEquals(projectCat, pCache.getProjectCategory(new Long(30)));
    }
}
