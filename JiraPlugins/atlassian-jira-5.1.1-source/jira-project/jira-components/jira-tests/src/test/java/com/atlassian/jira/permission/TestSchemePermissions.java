/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.Permissions;

public class TestSchemePermissions extends LegacyJiraMockTestCase
{

    private SchemePermissions schemePermissions;

    public TestSchemePermissions(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        schemePermissions = new SchemePermissions();
        schemePermissions.getSchemePermissions();
    }

    public void testEventExists()
    {
        assertTrue(schemePermissions.schemePermissionExists(new Integer(Permissions.PROJECT_ADMIN)));
    }

    public void testEventDoesntExists()
    {
        assertTrue(!schemePermissions.schemePermissionExists(new Integer(237878)));
    }
}
