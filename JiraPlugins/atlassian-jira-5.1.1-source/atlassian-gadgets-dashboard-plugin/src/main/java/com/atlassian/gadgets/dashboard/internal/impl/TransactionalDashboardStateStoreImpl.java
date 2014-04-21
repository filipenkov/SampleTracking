package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code DashboardStateStore} implementation that adds a
 * transactional wrapper around an existing {@code DashboardStateStore}
 */
public class TransactionalDashboardStateStoreImpl implements DashboardStateStore
{
    private final DashboardStateStore stateStore;
    private final TransactionTemplate txTemplate;

    /**
     * Constructor.
     * @param stateStore the dashboard state store to use
     * @param txTemplate the transaction wrapper for persistence operations
     */
    public TransactionalDashboardStateStoreImpl(DashboardStateStore stateStore, TransactionTemplate txTemplate)
    {
        this.stateStore = checkNotNull(stateStore, "stateStore");
        this.txTemplate = checkNotNull(txTemplate, "txTemplate");
    }

    public DashboardState retrieve(final DashboardId dashboardId) throws DashboardNotFoundException
    {
        checkNotNull(dashboardId, "dashboardId");
        return (DashboardState) txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                return stateStore.retrieve(dashboardId);
            }
        });
    }

    public DashboardState update(final DashboardState state, final Iterable<DashboardChange> changes) throws DashboardStateStoreException
    {
        checkNotNull(state, "state");
        checkNotNull(changes, "changes");
        return (DashboardState) txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                return stateStore.update(state, changes);
            }
        });
    }

    public void remove(final DashboardId dashboardId) throws DashboardStateStoreException
    {
        checkNotNull(dashboardId, "dashboardId");
        txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                stateStore.remove(dashboardId);
                return null;
            }
        });
    }

    public DashboardState findDashboardWithGadget(final GadgetId gadgetId) throws DashboardNotFoundException
    {
        checkNotNull(gadgetId, "gadgetId");
        return (DashboardState) txTemplate.execute(new TransactionCallback()
        {
           public Object doInTransaction()
           {
               return stateStore.findDashboardWithGadget(gadgetId);
           }
        });
    }
}
