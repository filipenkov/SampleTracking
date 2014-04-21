/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;

//This class has been updated to reflect the change in JiraPermssions from project level to scheme level
public class TestJiraPermission extends LegacyJiraMockTestCase
{
    private GenericValue perm;
    private GenericValue project;

    public TestJiraPermission(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        perm = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("id", new Long(1), "scheme", new Long(10), "permission", new Long(2), "parameter", "Test Group"));
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10)));
    }

    public void testGetsConstructors()
    {
        GenericValue nullProject = null;

        JiraPermission test1 = new JiraPermission(1);
        assertEquals(1, test1.getType());
        assertNull(test1.getGroup());
        assertNull(test1.getScheme());

        JiraPermission test2 = new JiraPermission(2, new Long(10), "Test Group", "group");
        assertEquals(2, test2.getType());
        assertEquals(new Long(10), test2.getScheme());
        assertEquals("Test Group", test2.getGroup());

        JiraPermission test3 = new JiraPermission(perm);
        assertEquals(2, test3.getType());
        assertEquals(new Long(10), test3.getScheme());
        assertEquals("Test Group", test3.getGroup());

        JiraPermission test4 = new JiraPermission(4, project, "Test Group", "group");
        assertEquals(4, test4.getType());
        assertEquals(new Long(10), test4.getScheme());
        assertEquals("Test Group", test4.getGroup());

        JiraPermission test5 = new JiraPermission(5, nullProject, "Test Group", "group");
        assertEquals(5, test5.getType());
        assertEquals(null, test5.getScheme());
        assertEquals("Test Group", test5.getGroup());
    }

    public void testEquals()
    {
        JiraPermission test1 = new JiraPermission(perm);
        JiraPermission test2 = new JiraPermission(perm);
        JiraPermission test3 = new JiraPermission(1);
        JiraPermission test4 = new JiraPermission(3, new Long(20), "Test Group", "group");
        JiraPermission test5 = new JiraPermission(3, new Long(10), "Not Test Group", "group");
        JiraPermission test6 = new JiraPermission(3, new Long(10), "Not Test Group", "reporter");
        JiraPermission test7 = new JiraPermission(3, new Long(10), "Not Test Group", "user");
        JiraPermission test8 = new JiraPermission(3, new Long(10), "Not Test Group", "user");

        assertTrue(!test1.equals(new Long(1)));
        assertTrue(!test1.equals(test3));
        assertTrue(!test1.equals(test4));
        assertTrue(!test1.equals(test5));
        assertTrue(test1.equals(test2));
        assertTrue(!test1.equals(test6));
        assertTrue(!test6.equals(test7));
        assertTrue(test7.equals(test8));
    }
}
