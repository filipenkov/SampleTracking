package com.atlassian.gadgets.dashboard;

import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;

import org.junit.Test;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static org.apache.commons.collections.CollectionUtils.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DashboardStateBuilderTest
{
    @Test
    public void assertThatColumnsInBuiltStateArePresentIfNotProvidedToBuilder()
    {
        DashboardState state = dashboard(DashboardId.valueOf("1")).title("Menagerie").build();
        assertThat(size(state.getGadgetsInColumn(ColumnIndex.ZERO)), is(equalTo(0)));
        assertThat(size(state.getGadgetsInColumn(ColumnIndex.ONE)), is(equalTo(0)));
    }

    @Test
    public void assertThatCreatingDashboardFromStateUsesVersion()
    {
        long version = 5L;
        DashboardState fromState = dashboard(DashboardId.valueOf("1")).title("Menagerie").version(version).build();
        DashboardState state = dashboard(fromState).build();
        assertThat(state.getVersion(), is(equalTo(version)));
    }
}
