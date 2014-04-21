/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;

//This class has been updated to reflect the changes made in permissions from a project level to a scheme level
public class TestGlobalPermissionsCache extends LegacyJiraMockTestCase
{
    private GlobalPermissionsCache pc;

    public TestGlobalPermissionsCache(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        pc = new GlobalPermissionsCache();
    }

    public void testRefreshHasGetPermission()
    {
        GenericValue perm1 = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("id", new Long(1), "permission", new Long(2), "parameter", "Test Group"));
        assertTrue(!pc.hasPermission(new JiraPermission(perm1)));

        pc.refresh();
        assertTrue(pc.hasPermission(new JiraPermission(perm1)));
        assertEquals(perm1, pc.getPermission(new JiraPermission(perm1)));

        GenericValue perm2 = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("id", new Long(2), "permission", new Long(1), "parameter", "Test Group"));
        assertTrue(!pc.hasPermission(new JiraPermission(perm2)));

        pc.refresh();
        assertTrue(pc.hasPermission(new JiraPermission(perm1)));
        assertTrue(pc.hasPermission(new JiraPermission(perm2)));
        assertEquals(perm1, pc.getPermission(new JiraPermission(perm1)));
        assertEquals(perm2, pc.getPermission(new JiraPermission(perm2)));
    }
}
