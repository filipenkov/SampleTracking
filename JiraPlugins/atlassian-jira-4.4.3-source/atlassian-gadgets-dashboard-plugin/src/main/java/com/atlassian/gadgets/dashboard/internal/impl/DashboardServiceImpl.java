package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardService;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;

import com.google.common.collect.ImmutableList;

/**
 * Default implementation of {@code DashboardService}.
 */
public class DashboardServiceImpl implements DashboardService
{
    private final DashboardStateStore stateStore;
    private final DashboardPermissionService permissionService;

    /**
     * Constructor.
     * @param stateStore the dashboard state store to use
     * @param permissionService the permissions to validate users by
     */
    public DashboardServiceImpl(TransactionalDashboardStateStoreImpl stateStore, DashboardPermissionService permissionService)
    {
        this.stateStore = stateStore;
        this.permissionService = permissionService;
    }

    public DashboardState get(DashboardId id, String username) throws PermissionException
    {
        if (!permissionService.isReadableBy(id, username))
        {
            throw new PermissionException();
        }
        return stateStore.retrieve(id);
    }

    public DashboardState save(DashboardState state, String username) throws PermissionException
    {
        if (!permissionService.isWritableBy(state.getId(), username))
        {
            throw new PermissionException();
        }
        return stateStore.update(state, ImmutableList.<DashboardChange>of());
    }
}
