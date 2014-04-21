/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build47;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.List;

public class TestUpgradeTask_Build47 extends AbstractUsersTestCase
{
    PermissionSchemeManager psm;
    PermissionManager pm;
    ProjectManager projm;
    GenericValue projectNoPerms;
    GenericValue projectPerms;
    final int PROJECTS_WITH_PERMS = 5;
    final int PROJECTS_WITHOUT_PERMS = 3;
    UpgradeTask_Build47 task;


    public TestUpgradeTask_Build47(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        psm = ManagerFactory.getPermissionSchemeManager();
        pm = ManagerFactory.getPermissionManager();
        projm = ManagerFactory.getProjectManager();
        task = new UpgradeTask_Build47(psm, new SchemePermissions());

    }

    public void testGetBuildNumber()
    {
        assertEquals("47", task.getBuildNumber());
    }

    public void testDoUpgrade() throws Exception
    {
        for (int i = 1; i <= PROJECTS_WITH_PERMS; i++)
        {
            if (i > Permissions.MAX_PERMISSION)
                break;
            projectPerms = EntityUtils.createValue("Project", EasyMap.build("name", "Project" + i));
            EntityUtils.createValue("Permission", EasyMap.build("project", projectPerms.getLong("id"), "type", new Long(10 + i), "group", "test"));
        }

        for (int i = 1; i <= PROJECTS_WITHOUT_PERMS; i++)
        {
            projectNoPerms = EntityUtils.createValue("Project", EasyMap.build("name", "No Perms" + i));
        }

        task.doUpgrade(false);

        //check that the default permission scheme was added
        assertNotNull(psm.getDefaultScheme());

        //All projects with no permissions should be added to default scheme
        assertEquals(psm.getProjects(psm.getDefaultScheme()).size(), PROJECTS_WITHOUT_PERMS);

        //There should be a scheme for all . The default and the one for project with permissions
        assertEquals(psm.getSchemes().size(), PROJECTS_WITH_PERMS + 1);
    }

    public void testCreateProjectSchemes() throws Exception
    {
        for (int i = 1; i <= PROJECTS_WITH_PERMS; i++)
        {
            if (i > Permissions.MAX_PERMISSION)
                break;
            projectPerms = EntityUtils.createValue("Project", EasyMap.build("name", "Project" + i));
            EntityUtils.createValue("Permission", EasyMap.build("project", projectPerms.getLong("id"), "type", new Long(10 + i), "group", "test"));
        }

        for (int i = 1; i <= PROJECTS_WITHOUT_PERMS; i++)
        {
            projectNoPerms = EntityUtils.createValue("Project", EasyMap.build("name", "No Perms" + i));
        }

        task.createProjectSchemes(psm, psm.createDefaultScheme());

        assertNotNull(psm.getDefaultScheme());

        //All projects with no permissions should be added to default scheme
        assertEquals(psm.getProjects(psm.getDefaultScheme()).size(), PROJECTS_WITHOUT_PERMS);

        //There should be a scheme for projects with permissions and the default
        assertEquals(psm.getSchemes().size(), PROJECTS_WITH_PERMS + 1);
    }

    public void testaddOldGlobalPermissionsToScheme() throws Exception
    {
        GenericValue scheme = psm.createScheme("test", "test");

        EntityUtils.createValue("Permission", EasyMap.build("project", null, "type", new Long(99), "group", "test"));

        task.addOldGlobalPermissionsToScheme(psm, 99, scheme);

        assertNotNull(psm.getScheme("test"));

        List perms = psm.getEntities(scheme, new Long(99), "test");

        assertTrue(perms.size() > 0);

        assertTrue(perms.size() == 1);

        GenericValue p = (GenericValue) perms.get(0);
        assertEquals(p.getString("parameter"), "test");
    }

    public void testIsDuplicate() throws Exception
    {

        GenericValue schemeA = psm.createScheme("testA", "test");
        GenericValue schemeB = psm.createScheme("testB", "test");
        GenericValue schemeC = psm.createScheme("testC", "test");

        SchemeEntity schemeEntity = new SchemeEntity(GroupDropdown.DESC, "test", new Long(2));
        SchemeEntity schemeEntity2 = new SchemeEntity(GroupDropdown.DESC, "test", new Long(3));

        psm.createSchemeEntity(schemeA, schemeEntity);
        psm.createSchemeEntity(schemeB, schemeEntity);
        psm.createSchemeEntity(schemeC, schemeEntity2);

        assertTrue(task.isDuplicate(psm, schemeA, schemeB));
        assertTrue(task.isDuplicate(psm, schemeB, schemeA));
        assertTrue(!task.isDuplicate(psm, schemeA, schemeC));
        assertTrue(!task.isDuplicate(psm, schemeB, schemeC));
    }

    public void testMergeDuplicateSchemes() throws Exception
    {

        for (int i = 1; i <= PROJECTS_WITH_PERMS; i++)
        {
            //if (i > Permissions.MAX_PERMISSION) break;
            projectPerms = EntityUtils.createValue("Project", EasyMap.build("name", "Test Project" + i));
            EntityUtils.createValue("Permission", EasyMap.build("project", projectPerms.getLong("id"), "type", new Long(10), "group", "test"));
        }

        task.createProjectSchemes(psm, psm.createDefaultScheme());

        task.mergeDuplicateSchemes(psm);

        //There should be a scheme for projects with permissions and the default
        assertEquals(psm.getSchemes().size(), 2);

        List schemes = psm.getSchemes();
        for (int i = 0; i < schemes.size(); i++)
        {
            GenericValue scheme = (GenericValue) schemes.get(i);

            List projects = psm.getProjects(scheme);
            for (int j = 0; j < projects.size(); j++)
            {
                GenericValue project = (GenericValue) projects.get(j);
            }
        }
    }
}
