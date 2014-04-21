package com.atlassian.gadgets.dashboard.spi;

import javax.annotation.Nullable;

import com.atlassian.gadgets.dashboard.DashboardId;

/**
 * Provide a way to determine if a user has permission to perform the given operations on a dashboard.
 * 
 * @since 2.0
 */
public interface DashboardPermissionService
{
    /**
     * Returns {@code true} if the user identified by {@code username} has permission to view the dashboard identified
     * by the {@code DashboardId}, {@code false} otherwise. This method should also return false if the dashboard
     * specified by the {@code DashboardId} doesn't exist.
     *
     * @param dashboardId the {@code DashboardId} of the dashboard the user is trying to view
     * @param username the name of the user logged in, {@code null} if no user is currently logged in
     * @return {@code true} if the user identified by {@code username} has permission to view the dashboard identified
     *         by the {@code DashboardId}, {@code false} otherwise.
     */
    boolean isReadableBy(DashboardId dashboardId, @Nullable String username);

    /**
     * Returns {@code true} if the user identified by {@code username} has permission to modify the dashboard identified
     * by the {@code DashboardId}, {@code false} otherwise. This method should also return false if the dashboard 
     * specified by the {@code DashboardId} doesn't exist.
     *
     * @param dashboardId the {@code DashboardId} of the dashboard the user is trying to modify
     * @param username the name of the user logged in, {@code null} if no user is currently logged in
     * @return {@code true} if the user identified by {@code username} has permission to modify the dashboard identified
     *         by the {@code DashboardId}, {@code false} otherwise.
     */
    boolean isWritableBy(DashboardId dashboardId, @Nullable String username);
}