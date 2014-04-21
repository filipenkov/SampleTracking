package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.internal.StateConverter;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardRepositoryImplTest
{
    @Mock
    TransactionalDashboardStateStoreImpl store;
    @Mock StateConverter converter;
    
    DashboardRepository repository;
    
    @Before
    public void setUp()
    {
        repository = new DashboardRepositoryImpl(store, converter);
    }
    
    @Test(expected = InconsistentDashboardStateException.class)
    public void verifyInconsistentDashboardStateExceptionIsThrownWhenRetrievedStateDoesNotMatchStoredState()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        DashboardState state = dashboard(dashboardId).title("Atlassian Menagerie").build();
        Dashboard dashboard = mock(Dashboard.class);

        when(dashboard.getId()).thenReturn(dashboardId);
        when(dashboard.getState()).thenReturn(state);
        when(store.update(state, ImmutableList.<DashboardChange>of())).thenReturn(dashboard(dashboardId).title("Changed State").build());

        try
        {
            repository.save(dashboard);
        }
        finally
        {
            verify(store).update(state, ImmutableList.<DashboardChange>of());
            verifyNoMoreInteractions(store);
        }
    }
}
