package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.internal.StateConverter;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;

import static com.google.common.base.Objects.equal;

/**
 * Default implementation of {@code DashboardRepository} which delegates
 * retrieval and save operations to a {@code DashboardStateStore}.
 */
public class DashboardRepositoryImpl implements DashboardRepository
{
    private final DashboardStateStore stateStore;
    private final StateConverter converter;

    /**
     * Constructor.
     * @param stateStore the {@code DashboardStateStore} to use for state storage
     * @param converter the {@code StateConverter} for translating states to dashboards
     */
    public DashboardRepositoryImpl(TransactionalDashboardStateStoreImpl stateStore, StateConverter converter)
    {
        this.stateStore = stateStore;
        this.converter = converter;
    }
    
    public Dashboard get(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext) throws DashboardNotFoundException
    {
        return converter.convertStateToDashboard(stateStore.retrieve(dashboardId), gadgetRequestContext);
    }

    public void save(Dashboard dashboard)
    {
        DashboardState state = dashboard.getState();
        if (!equal(stateStore.update(state, dashboard.getChanges()), state))
        {
            throw new InconsistentDashboardStateException("Dashboard state after store does not match state provided to store");
        }
        dashboard.clearChanges();
    }

    public DashboardId findDashboardByGadgetId(GadgetId gadgetId) throws DashboardNotFoundException
    {
        return stateStore.findDashboardWithGadget(gadgetId).getId();
    }
}
