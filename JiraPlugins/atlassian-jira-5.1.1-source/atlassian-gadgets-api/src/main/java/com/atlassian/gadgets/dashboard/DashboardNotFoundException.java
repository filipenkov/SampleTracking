package com.atlassian.gadgets.dashboard;

/**
 * Thrown if the dashboard identified by the {@link DashboardId} does not exist.
 */
public class DashboardNotFoundException extends RuntimeException
{
    private final DashboardId dashboardId;

    public DashboardNotFoundException(DashboardId id)
    {
        dashboardId = id;
    }

    public DashboardId getDashboardId()
    {
        return dashboardId;
    }
}
