package com.atlassian.jira.util;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

public class TestGroupPermissionCheckerImpl extends AbstractUsersTestCase
{
    Mock mockPermissionManager;
    private User testUser;
    private Group testGroup;

    public TestGroupPermissionCheckerImpl(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);

        testUser = createMockUser("test user");
        testGroup = createMockGroup("test group");
    }

    public void testHasViewPermissionWhenAdmin()
    {
        GroupPermissionCheckerImpl groupPermissionChecker = new GroupPermissionCheckerImpl((PermissionManager) mockPermissionManager.proxy(), null);

        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(testUser)), Boolean.TRUE);
        assertTrue(groupPermissionChecker.hasViewGroupPermission(testGroup.getName(), testUser));

        mockPermissionManager.verify();
    }
    
    public void testHasViewPermissionNullUser()
    {
        GroupPermissionCheckerImpl groupPermissionChecker = new GroupPermissionCheckerImpl((PermissionManager) mockPermissionManager.proxy(), new MockGroupManager());

        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsNull()), Boolean.FALSE);
        assertFalse(groupPermissionChecker.hasViewGroupPermission(testGroup.getName(), null));

        mockPermissionManager.verify();
    }
    
    public void testHasViewPermissionNotInGroup()
    {
        GroupPermissionCheckerImpl groupPermissionChecker = new GroupPermissionCheckerImpl((PermissionManager) mockPermissionManager.proxy(), new MockGroupManager());

        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(testUser)), Boolean.FALSE);
        assertFalse(groupPermissionChecker.hasViewGroupPermission(testGroup.getName(), testUser));

        mockPermissionManager.verify();
    }

    public void testHasViewPermissionInGroup()
    {
        final MockGroupManager groupManager = new MockGroupManager();
        groupManager.addUserToGroup(testUser, testGroup);
        groupManager.addGroup(testGroup.getName());
        GroupPermissionCheckerImpl groupPermissionChecker = new GroupPermissionCheckerImpl((PermissionManager) mockPermissionManager.proxy(), groupManager);

        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(testUser)), Boolean.FALSE);
        assertTrue(groupPermissionChecker.hasViewGroupPermission(testGroup.getName(), testUser));

        mockPermissionManager.verify();
    }
}
