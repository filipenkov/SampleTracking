/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import org.apache.lucene.search.Query;
import org.ofbiz.core.entity.GenericValue;

public class TestGroupDropdown extends AbstractUsersIndexingTestCase
{
    private User u;
    private GenericValue projectA;
    private GenericValue projectB;
    private GenericValue permissionScheme;
    private GenericValue schemePermission;

    public TestGroupDropdown(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        u = createMockUser("owen");

        Group g = createMockGroup("group1");
        addUserToGroup(u, g);
    }

    public void testGetQueryWorksCorrectly() throws Exception
    {
        //Create two projects
        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", u.getName()));
        projectB = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", u.getName()));

        //Create a new permission scheme for this project
        permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme", "description", "Permission scheme"));

        //Associate the project with permission scheme
        PermissionSchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        psm.addSchemeToProject(projectA, permissionScheme);
        psm.addSchemeToProject(projectB, permissionScheme);

        PermissionTypeManager ptm = ManagerFactory.getPermissionTypeManager();

        //Create a Scheme Permission to browse for the type "group1"
        SchemeType type = ptm.getSchemeType("group");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", type.getType(), "parameter", "group1"));

        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("group");
        Query query = securityType.getQuery(u, projectA, null);
        assertEquals(DocumentConstants.PROJECT_ID + ":" + projectA.getLong("id"), query.toString(""));
    }

    public void testGetQueryWorksCorrectlyWithNoProjects() throws Exception
    {
        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("group");
        Query query = securityType.getQuery(u, projectB, null);
        assertNull(query);
    }
}
