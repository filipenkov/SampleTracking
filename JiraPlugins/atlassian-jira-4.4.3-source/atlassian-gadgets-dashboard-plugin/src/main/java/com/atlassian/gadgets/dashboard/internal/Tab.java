package com.atlassian.gadgets.dashboard.internal;

import java.net.URI;

import com.atlassian.gadgets.dashboard.DashboardId;

/**
 * Internal representation of a {@link com.atlassian.gadgets.dashboard.DashboardTab}.  Has all the same properties as
 * well as a writable field to determine if a user has write permissions for the dashboard this tab represents.
 *
 * This mainly exists so the DashboardTab class doesn't get polluted with the writable property.  Clients should not have
 * to concern themselves with this.
 */
public interface Tab
{
    /**
     * @return The id for the dashboard this tab represents
     */
    DashboardId getDashboardId();

    /**
     * @return The title of this tab
     */
    String getTitle();

    /**
     * @return The url to navigate to when clicking this tab
     */
    URI getTabUri();

    /**
     * @return true if the current user has write permission for the tab this dashbaord represents
     */
    boolean isWritable();
}
