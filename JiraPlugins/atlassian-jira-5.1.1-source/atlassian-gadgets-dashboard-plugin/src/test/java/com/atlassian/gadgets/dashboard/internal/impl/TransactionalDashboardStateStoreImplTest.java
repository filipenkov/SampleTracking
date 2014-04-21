package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalDashboardStateStoreImplTest
{
    @Mock DashboardStateStore store;
    @Mock TransactionTemplate txTemplate;
    
    TransactionalDashboardStateStoreImpl transactionalStore;

    @Before
    public void setUp()
    {
        transactionalStore = new TransactionalDashboardStateStoreImpl(store, txTemplate);
    }

    @Test
    public void verifyRetrieveDoneInTransaction()
    {
        transactionalStore.retrieve(DashboardId.valueOf("1"));
        verify(txTemplate).execute(isA(TransactionCallback.class));
    }

    @Test
    public void verifyUpdateDoneInTransaction()
    {
        transactionalStore.update(DashboardState.dashboard(DashboardId.valueOf("1")).title("Menagerie").build(), ImmutableList.<DashboardChange>of());
        verify(txTemplate).execute(isA(TransactionCallback.class));
    }

    @Test
    public void verifyRemoveDoneInTransaction()
    {
        transactionalStore.remove(DashboardId.valueOf("1"));
        verify(txTemplate).execute(isA(TransactionCallback.class));
    }
}
