/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;

public class TestDefaultPermissionManager extends AbstractUsersTestCase
{
    public TestDefaultPermissionManager(String s)
    {
        super(s);
    }

    public void testAddPermission() throws Exception
    {
        DefaultPermissionManager pm = new DefaultPermissionManager();
        GlobalPermissionManager gpm = ManagerFactory.getGlobalPermissionManager();

        // test admin permission with no scheme
        gpm.addPermission(Permissions.ADMINISTER, null);
        assertTrue(pm.hasPermission(Permissions.ADMINISTER, null));

        // test group filter permission with no scheme
        gpm.addPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, null);
        assertTrue(pm.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, null));

        // test group filter permission with no scheme
        gpm.addPermission(Permissions.CREATE_SHARED_OBJECTS, null);
        assertTrue(pm.hasPermission(Permissions.CREATE_SHARED_OBJECTS, null));

        // test use permission with no scheme
        try {
            gpm.addPermission(Permissions.USE, null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }


        // test that non admin/use permission with no scheme throws exception
        try
        {
            gpm.addPermission(Permissions.BROWSE, null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
}
