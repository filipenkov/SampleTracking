package com.atlassian.gadgets.dashboard.spi.util;

import java.util.Map;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

/**
 * A {@code Condition} that can be used when creating web-items that allow the user to modify the dashboard in some
 * way, such as by adding a gadget to it.
 * 
 * @since 2.0
 */
public class UserHasWritePermissionToDashboard implements Condition
{
    private final DashboardPermissionService permissionService;

    public UserHasWritePermissionToDashboard(DashboardPermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public boolean shouldDisplay(Map<String, Object> context)
    {
        DashboardId dashboardId = (DashboardId) context.get("dashboardId");
        String username = (String) context.get("username");
        return permissionService.isWritableBy(dashboardId, username);
    }

    public void init(Map<String, String> params) throws PluginParseException {}
}
