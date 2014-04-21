package com.atlassian.gadgets.dashboard.internal;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;

public interface DashboardRepository
{
    Dashboard get(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext) throws DashboardNotFoundException;
    void save (Dashboard dashboard);
    DashboardId findDashboardByGadgetId(GadgetId gadgetId) throws DashboardNotFoundException;
}
