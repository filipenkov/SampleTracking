/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import org.ofbiz.core.entity.GenericValue;

public class TestProjectActionSupport extends AbstractUsersTestCase
{
    public TestProjectActionSupport(String s)
    {
        super(s);
    }

    public void testGetProjectManager()
    {
        ProjectActionSupport pas = new ProjectActionSupport();
        assertTrue(pas.getProjectManager() instanceof ProjectManager);
    }

    public void testBrowseableProjects() throws Exception
    {
        GenericValue scheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("id", new Long(10), "name", "Test Scheme"));
        GenericValue project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(4)));
        ComponentAccessor.getPermissionSchemeManager().addSchemeToProject(project1, scheme);

        GenericValue project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(5)));

        ProjectActionSupport pas = new ProjectActionSupport();

        assertEquals(0, pas.getBrowseableProjects().size());

        pas = new ProjectActionSupport();

        ComponentAccessor.getPermissionManager().addPermission(Permissions.BROWSE, scheme, null, GroupDropdown.DESC);
        assertEquals(1, pas.getBrowseableProjects().size());
        assertTrue(pas.getBrowseableProjects().contains(project1));

        pas = new ProjectActionSupport();

        ComponentAccessor.getPermissionSchemeManager().addSchemeToProject(project2, scheme);
        assertEquals(2, pas.getBrowseableProjects().size());
        assertTrue(pas.getBrowseableProjects().contains(project1));
        assertTrue(pas.getBrowseableProjects().contains(project2));
    }

    public void testBrowsableProjects() throws Exception
    {
        GenericValue scheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("id", new Long(10), "name", "Test Scheme"));
        GenericValue gvProject1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(4)));
        Project project1 = new ProjectImpl(gvProject1);
        ComponentAccessor.getPermissionSchemeManager().addSchemeToProject(gvProject1, scheme);

        GenericValue gvProject2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(5)));
        Project project2 = new ProjectImpl(gvProject2);

        ProjectActionSupport pas = new ProjectActionSupport();

        assertEquals(0, pas.getBrowsableProjects().size());

        pas = new ProjectActionSupport();

        ComponentAccessor.getPermissionManager().addPermission(Permissions.BROWSE, scheme, null, GroupDropdown.DESC);
        assertEquals(1, pas.getBrowsableProjects().size());
        assertTrue(pas.getBrowsableProjects().contains(project1));

        pas = new ProjectActionSupport();

        ComponentAccessor.getPermissionSchemeManager().addSchemeToProject(gvProject2, scheme);
        assertEquals(2, pas.getBrowsableProjects().size());
        assertTrue(pas.getBrowsableProjects().contains(project1));
        assertTrue(pas.getBrowsableProjects().contains(project2));
    }

}
