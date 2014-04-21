/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.project.MockCachingProjectManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;

public class TestCachingProjectManager extends LegacyJiraMockTestCase
{
    private MockCachingProjectManager cpm;
    private GenericValue project1;
    private GenericValue project2;
    public GenericValue projectCat;
    private GenericValue projectCat2;
    private ProjectComponent component1;
    private ProjectComponent component2;

    public TestCachingProjectManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        cpm = new MockCachingProjectManager(new DefaultProjectManager());
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10), "key", "ABC-123", "name", "This Project", "counter", new Long(10)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(11), "key", "ABC-124", "name", "This Project 2", "counter", new Long(10)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(20), "name", "ver1", "project", new Long(10), "released", "true", "sequence", new Long(1)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(21), "name", "ver2", "project", new Long(10), "released", "true", "sequence", new Long(2)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(22), "name", "ver3", "project", new Long(10), "sequence", new Long(3)));
        projectCat = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("id", new Long(30), "name", "cat1", "description", "cat1Description"));
        projectCat2 = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("id", new Long(31), "name", "cat2", "description", "cat2Description"));

        ProjectComponentManager pcm = (ProjectComponentManager) ComponentManager.getComponentInstanceOfType(ProjectComponentManager.class);
        component1 = pcm.create("com1", null, null, 0, new Long(10));
        component2 = pcm.create("com2", null, null, 0, new Long(10));

        cpm.updateCache();
    }

    public void testUpdateCache() throws GenericEntityException
    {
        GenericValue oldProject = cpm.getProject(project1.getLong("id"));
        cpm.updateCache();

        GenericValue newProject = cpm.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);
        oldProject = cpm.getProject(project1.getLong("id"));
        cpm.refresh();
        newProject = cpm.getProject(project1.getLong("id"));

        assertEquals(11, cpm.getNextId(cpm.getProjectObj(project1.getLong("id"))));
    }

    public void testCachingProjectManager() throws GenericEntityException
    {
        GenericValue oldProject = cpm.getProject(project1.getLong("id"));
        Collection oldComponents = cpm.getComponents(oldProject);
        assertNotNull(oldComponents);
        assertEquals(2, oldComponents.size());

        GenericValue newProject = cpm.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);

        Collection newComponents = cpm.getComponents(newProject);
        assertEquals(oldComponents, newComponents);
//        assertEquals(oldVersions, cpm.getVersions(newProject));
    }

    public void testGetProject() throws GenericEntityException
    {
        GenericValue project = cpm.getProject(new Long(10));
        assertTrue(project == cpm.getProject(new Long(10)));
        assertTrue(project == cpm.getProjectByKey("ABC-123"));
        assertTrue(project == cpm.getProjectByName("This Project"));
    }

    public void testGetComponents() throws GenericEntityException
    {
        Collection oldComponents = cpm.getComponents(project1);
        assertNotNull(oldComponents);
        assertEquals(2, oldComponents.size());

        GenericValue oldComponent = cpm.getComponent(component1.getId());
        assertTrue(oldComponents.contains(oldComponent));
        oldComponent = cpm.getComponent(component2.getId());
        assertTrue(oldComponents.contains(oldComponent));
    }

    public void testGetProjects() throws GenericEntityException
    {
        Collection projects = cpm.getProjects();
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));
    }

    public void testGetProjectCategories() throws GenericEntityException
    {
        Collection projectCategories = cpm.getProjectCategories();
        assertNotNull(projectCategories);
        assertEquals(2, projectCategories.size());
        assertTrue(projectCategories.contains(projectCat));
    }

    public void testNotNull()
    {
        assertEquals(Collections.EMPTY_LIST, cpm.noNull(null));
    }

    public void testGetProjectCategory() throws GenericEntityException
    {
        assertEquals(projectCat, cpm.getProjectCategory(new Long(30)));
    }

    public void testGetProjectCategoryFromProject() throws GenericEntityException
    {
        // null project id
        GenericValue projectCategory = cpm.getProjectCategoryFromProject(null);
        assertNull(projectCategory);

        //valid project id but no association set
        projectCategory = cpm.getProjectCategoryFromProject(project1);
        assertNull(projectCategory);

        //valid project id and association exists.. return the projectCategory
        CoreFactory.getAssociationManager().createAssociation(project1, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);
        cpm.refresh();
        projectCategory = cpm.getProjectCategoryFromProject(project1);
        assertEquals(projectCat, projectCategory);
    }

    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();

        // test null projectCategory id
        Collection projects = cpm.getProjectsFromProjectCategory((GenericValue) null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = cpm.getProjectsFromProjectCategory(projectCat);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        cpm.setProjectCategory(project1, projectCat);
        projects = cpm.getProjectsFromProjectCategory(projectCat);
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertTrue(projects.contains(project1));
    }

    public void testGetProjectObjectsFromProjectCategory() throws GenericEntityException
    {
        // test null projectCategory id
        Collection projects = cpm.getProjectObjectsFromProjectCategory(null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = cpm.getProjectObjectsFromProjectCategory(30L);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        cpm.setProjectCategory(project1, projectCat);
        projects = cpm.getProjectObjectsFromProjectCategory(30L);
        assertNotNull(projects);
        assertEquals(1, projects.size());
        final Project project = (Project) projects.iterator().next();
        assertEquals(new Long(10L), project.getId());
        assertEquals(projectCat, project.getProjectCategory());
    }

    public void testSetProjectCategory() throws GenericEntityException
    {
        //test null project
        try
        {
            cpm.setProjectCategory((GenericValue) null, null);
            fail("Should have thrown IllegalArgumentException if null project");
        }
        catch (IllegalArgumentException e)
        {
            //this is what we want to happen
        }

        //test setting up a relation with a project that has no categories
        assertNull(cpm.getProjectCategoryFromProject(null));
        cpm.setProjectCategory(project1, projectCat);
        assertEquals(projectCat, cpm.getProjectCategoryFromProject(project1));
        assertEquals(1, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());

        //test setting up a relation with a project that has one category already
        cpm.setProjectCategory(project1, projectCat2);
        assertEquals(projectCat2, cpm.getProjectCategoryFromProject(project1));
        assertEquals(1, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());

        //test setting up a relation with a null category (ie no project category)
        cpm.setProjectCategory(project1, null);
        assertEquals(null, cpm.getProjectCategoryFromProject(project1));
        assertEquals(0, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());
    }

    public void testGetProjectObj()
    {
        ProjectManager pm = cpm;

        // non-existing project - ID is null - not a requirement at the moment
        Project project = pm.getProjectObj(null);
        assertNull(project);

        // non-existing project
        project = pm.getProjectObj(new Long(666));
        assertNull(project);

        // existing project
        Long projectId1 = project1.getLong("id");
        project = pm.getProjectObj(projectId1);
        assertNotNull(project);
        assertEquals(projectId1, project.getId());

        Long projectId2 = project2.getLong("id");
        project = pm.getProjectObj(projectId2);
        assertNotNull(project);
        assertEquals(projectId2, project.getId());
    }

    public void testGetAllProjectObjects() throws Exception
    {
        ProjectManager pm = cpm;
        final Collection<Project> projects = pm.getProjectObjects();
        assertNotNull(projects);
        assertEquals(2, projects.size());

        boolean proj1Found = false, proj2Found = false;
        for (Project project : projects)
        {
            if (project1.getLong("id").equals(project.getId()))
            {
                proj1Found = true;
            }
            else if (project2.getLong("id").equals(project.getId()))
            {
                proj2Found = true;
            }
        }

        assertTrue(proj1Found && proj2Found);
    }

}
