/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.apache.lucene.search.Query;
import org.ofbiz.core.entity.GenericValue;

public class TestSingleUser extends AbstractUsersIndexingTestCase
{
    private User u;
    private GenericValue projectA;
    private GenericValue projectB;
    private GenericValue permissionScheme;
    private GenericValue schemePermission;
    private GenericValue projectC;

    public TestSingleUser(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        u = UtilsForTests.getTestUser("owen");

        Group g = UtilsForTests.getTestGroup("group1");
        u.addToGroup(g);
    }

    public void testGetQueryWorksCorrectly() throws Exception
    {
        //Create two projects
        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", u.getName()));
        projectC = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", u.getName()));

        //Create a new permission scheme for this project
        permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme", "description", "Permission scheme"));

        //Associate the project with permission scheme
        PermissionSchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        psm.addSchemeToProject(projectA, permissionScheme);

        PermissionTypeManager ptm = ManagerFactory.getPermissionTypeManager();

        SchemeType user = ptm.getSchemeType("user");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", user.getType(), "parameter", u.getName()));

        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("group");
        Query query = securityType.getQuery(u, projectA, null);
        assertEquals(DocumentConstants.PROJECT_ID + ":" + projectA.getLong("id"), query.toString(""));
    }

    public void testGetQueryWorksCorrectlyWithNoProjects() throws Exception
    {
        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("group");
        Query query = securityType.getQuery(u, projectC, null);
        assertNull(query);
    }
}
