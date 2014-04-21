/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.AbstractPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;

import java.util.Collection;

public class TestFilterUtils extends AbstractUsersTestCase
{
    public TestFilterUtils(String s)
    {
        super(s);
    }

    /**
    * Test that normal users can only see their groups, but admins can see all groups.
    * @throws com.atlassian.jira.exception.CreateException
    */
    public void testAdminCanSeeAllGroups() throws CreateException
    {

        final MockPermissionManager mockPermissionManager = new MockPermissionManager();
        ManagerFactory.addService(PermissionManager.class, mockPermissionManager);

        //create admin user
        User admin = UtilsForTests.getTestUser("admin user");

        //create 2 groups that the admin is a member of
        Group adminGroup = UtilsForTests.getTestGroup("admin group");
        Group group1 = UtilsForTests.getTestGroup("admin group2");
        admin.addToGroup(adminGroup);
        admin.addToGroup(group1);

        JiraTestUtil.loginUser(admin);

        Group otherGroup = UtilsForTests.getTestGroup("other group");

        Collection groups = FilterUtils.getGroups(admin);
        assertTrue("Should get their own groups", groups.contains(adminGroup.getName()));
        assertTrue("Should get their own groups", groups.contains(group1.getName()));
        assertFalse("Should not get other groups", groups.contains(otherGroup.getName()));
        assertEquals(2, groups.size());

        //give the admin admin permissions
        mockPermissionManager.setAdministartorAuthority(true);

        //get another filter to ensure there is no caching
        groups = FilterUtils.getGroups(admin);
        assertTrue("Should get their own groups", groups.contains(adminGroup.getName()));
        assertTrue("Should get their own groups", groups.contains(group1.getName()));
        assertTrue("Admins should get all groups", groups.contains(otherGroup.getName()));
        assertEquals(3, groups.size());
    }

    private class MockPermissionManager extends AbstractPermissionManager
    {
        private boolean adminAuthority = false;

        @Override
        public boolean hasPermission(final int permissionsId, final com.atlassian.crowd.embedded.api.User user)
        {
            return adminAuthority;
        }

        public void setAdministartorAuthority(boolean adminAuthority)
        {
            this.adminAuthority = adminAuthority;
        }

    }
}
