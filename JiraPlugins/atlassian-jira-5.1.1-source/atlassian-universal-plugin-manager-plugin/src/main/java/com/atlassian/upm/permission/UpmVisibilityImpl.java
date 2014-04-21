package com.atlassian.upm.permission;

import com.atlassian.upm.Sys;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;

/**
 * Used to determine visible UPM tabs based on a users permissions.
 */
public class UpmVisibilityImpl implements UpmVisibility
{
    private final PermissionEnforcer permissionEnforcer;

    public UpmVisibilityImpl(final PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = permissionEnforcer;
    }

    public boolean isManageExistingVisible()
    {
        // We have decided that if you can use UPM then you can see the manage existing tab
        return permissionEnforcer.isAdmin();
    }

    public boolean isUpdateVisible()
    {
        return permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS);
    }

    public boolean isInstallVisible()
    {
        return permissionEnforcer.hasPermission(GET_AVAILABLE_PLUGINS);
    }

    public boolean isCompatibilityVisible()
    {
        return permissionEnforcer.hasPermission(GET_PRODUCT_UPDATE_COMPATIBILITY);
    }

    public boolean isOsgiVisible()
    {
        return permissionEnforcer.hasPermission(GET_OSGI_STATE);
    }

    public boolean isAuditLogVisible()
    {
        return permissionEnforcer.hasPermission(GET_AUDIT_LOG);
    }

    public boolean isDevModeEnabled()
    {
        return Sys.isDevModeEnabled();
    }
}
