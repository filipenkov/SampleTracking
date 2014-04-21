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
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
import org.apache.lucene.search.Query;
import org.ofbiz.core.entity.GenericValue;

public class TestCurrentAssignee extends AbstractUsersIndexingTestCase
{
    private User u;

    public TestCurrentAssignee(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        u = UtilsForTests.getTestUser("owen");
    }

    public void testGetQueryWorksCorrectlyWithPermissions() throws Exception
    {
        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("assignee");

        //Setup permissions so that a query is created
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project"));
        PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = permissionSchemeManager.createScheme("Scheme", "scheme");
        permissionSchemeManager.addSchemeToProject(project, scheme);

        SchemeEntity schemeEntity = new SchemeEntity(securityType.getType(), null, new Long(Permissions.BROWSE));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);

        Query query = securityType.getQuery(u, project, null);
        assertEquals("(+" + DocumentConstants.PROJECT_ID + ":" + project.getLong("id") + " +" + DocumentConstants.ISSUE_ASSIGNEE + ":owen)", query.toString(""));
    }

    public void testGetQueryWorksCorrectlyWithNoPermissions() throws Exception
    {
        SecurityType securityType = (SecurityType) ManagerFactory.getPermissionTypeManager().getSchemeType("assignee");
        Query query = securityType.getQuery(u, null, null);
        assertNull(query);
    }
}
