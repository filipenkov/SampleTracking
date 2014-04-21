package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardService;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceImplTest
{
    @Mock TransactionalDashboardStateStoreImpl store;
    @Mock DashboardPermissionService permissionService;
    
    DashboardService service;
    
    @Before
    public void setUp()
    {
        service = new DashboardServiceImpl(store, permissionService);
    }
    
    @Test
    public void assertThatDashboardStateIsReturnedWhenGettingADashboardUserHasReadAccessTo()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        DashboardState state = dashboard(dashboardId).title("Atlassian Menagerie").build();
        String username = "user";
        
        when(permissionService.isReadableBy(dashboardId, username)).thenReturn(true);
        when(store.retrieve(dashboardId)).thenReturn(state);
        
        assertThat(service.get(dashboardId, username), is(equalTo(state)));
    }
    
    @Test(expected=PermissionException.class)
    public void assertThatPermissionExceptionIsThrownWhenTryingToGetDashboardAndUserDoesNotHaveReadPermission()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        String username = "user";
        
        when(permissionService.isReadableBy(dashboardId, username)).thenReturn(false);
        
        service.get(dashboardId, username);
    }
    
    @Test
    public void assertThatDashboardStateIsReturnedWhenUpdatingADashboardUserHasWriteAccessTo()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        DashboardState state = dashboard(dashboardId).title("Atlassian Menagerie").build();
        String username = "user";
        
        when(permissionService.isWritableBy(dashboardId, username)).thenReturn(true);
        when(store.update(state, ImmutableList.<DashboardChange>of())).thenReturn(state);
        
        assertThat(service.save(state, username), is(equalTo(state)));
    }
    
    @Test(expected=PermissionException.class)
    public void assertThatPermissionExceptionIsThrownWhenTryingToSaveDashboardAndUserDoesNotHaveWritePermission()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        DashboardState state = dashboard(dashboardId).title("Atlassian Menagerie").build();
        String username = "user";
        
        when(permissionService.isWritableBy(dashboardId, username)).thenReturn(false);
        
        service.save(state, username);
    }
}
