package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
import org.easymock.MockControl;

public class TestUserIsSysAdminCondition extends ListeningTestCase
{
    @Test
    public void testAnonymousUserIsNotSystemAdmin() throws Exception
    {
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, null), false);
        mockControl.replay();
        assertFalse(new UserIsSysAdminCondition(mock).shouldDisplay(null, null));
        mockControl.verify();
    }

    @Test
    public void testAnonymousUserIsSystemAdmin() throws Exception
    {
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, null), true);
        mockControl.replay();
        assertTrue(new UserIsSysAdminCondition(mock).shouldDisplay(null, null));
        mockControl.verify();
    }

    @Test
    public void testUserIsNotSystemAdmin() throws Exception
    {
        final User user = new User("name", new MockProviderAccessor(), new MockCrowdService());
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, user), false);
        mockControl.replay();
        assertFalse(new UserIsSysAdminCondition(mock).shouldDisplay(user, null));
        mockControl.verify();
    }

    @Test
    public void testUserIsSystemAdmin() throws Exception
    {
        final User user = new User("name", new MockProviderAccessor(), new MockCrowdService());
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, user), true);
        mockControl.replay();
        assertTrue(new UserIsSysAdminCondition(mock).shouldDisplay(user, null));
        mockControl.verify();
    }

}
