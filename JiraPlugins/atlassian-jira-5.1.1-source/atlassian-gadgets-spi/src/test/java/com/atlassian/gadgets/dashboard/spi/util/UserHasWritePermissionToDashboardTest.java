package com.atlassian.gadgets.dashboard.spi.util;

import java.util.Map;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserHasWritePermissionToDashboardTest
{
    @Mock DashboardPermissionService permissionService;

    Condition userHasWritePermissionToDashboard;

    final DashboardId dashboardId = DashboardId.valueOf("100");
    final String username = "fred";
    final Map<String, Object> context = ImmutableMap.of("dashboardId", (Object) dashboardId, "username", username);

    @Before
    public void setUp()
    {
        userHasWritePermissionToDashboard = new UserHasWritePermissionToDashboard(permissionService);
    }

    @Test
    public void assertThatShouldDiplayReturnsFalseWhenUserDoesNotHaveWritePermissionToDashboard()
    {
        when(permissionService.isWritableBy(dashboardId, username)).thenReturn(false);
        assertFalse("user does not have write permission", userHasWritePermissionToDashboard.shouldDisplay(context));
    }

    @Test
    public void assertThatShouldDisplayReturnsTrueWhenUserHasWritePermissionToDashboard()
    {
        when(permissionService.isWritableBy(dashboardId, username)).thenReturn(true);
        assertTrue("user has write permission", userHasWritePermissionToDashboard.shouldDisplay(context));
    }
}
