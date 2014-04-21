package com.atlassian.gadgets.dashboard;

import javax.annotation.Nullable;

/**
 * Service that saves or fetches the {@link DashboardState}, after checking to make sure that the user has access to the
 * dashboard.
 */
public interface DashboardService
{
    /**
     * Fetch the {@code DashboardState} if the user has permission.
     * 
     * @param id Id of the dashboard to fetch.
     * @param username Username of the user to check permissions on, or null if no user is logged in
     * @return {@code DashboardState} for the given {@code DashboardId}
     * @throws PermissionException thrown if the user does not have permission to read the dashboard
     */
    DashboardState get(DashboardId id, @Nullable String username) throws PermissionException;
    
    /**
     * Save the {@code DashboardState} if the user has permission.
     *  
     * @param state {@code DashboardState} to save.
     * @param username Username of the user to check permissions on, or null if no user is logged in
     * @return refreshed {@code DashboardState} after the state object has been saved 
     * @throws PermissionException thrown if the user does not have permission to modify the dashboard
     */
    DashboardState save(DashboardState state, @Nullable String username) throws PermissionException;
}
