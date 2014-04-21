package com.atlassian.upm.rest.resources.permission;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.permission.PermissionService;
import com.atlassian.upm.spi.Plugin;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls if a user has access to a given resource.
 */
public class PermissionEnforcer
{
    private final UserManager userManager;
    private final PermissionService permissionService;

    public PermissionEnforcer(UserManager userManager, PermissionService permissionService)
    {
        this.userManager = checkNotNull(userManager, "userManager");
        this.permissionService = checkNotNull(permissionService, "permissionService");
    }

    public void enforcePermission(Permission permission)
    {
        if (!hasPermission(permission))
        {
            throw new PermissionDeniedException();
        }
    }

    public void enforcePermission(Permission permission, Plugin plugin)
    {
        if (!hasPermission(permission, plugin))
        {
            throw new PermissionDeniedException();
        }
    }

    public void enforcePermission(Permission permission, Plugin.Module module)
    {
        if (!hasPermission(permission, module))
        {
            throw new PermissionDeniedException();
        }
    }

    public boolean hasPermission(Permission permission)
    {
        return permissionService.hasPermission(userManager.getRemoteUsername(), permission);
    }

    public boolean hasPermission(Permission permission, Plugin plugin)
    {
        return permissionService.hasPermission(userManager.getRemoteUsername(), permission, plugin);
    }

    public boolean hasPermission(Permission permission, Plugin.Module module)
    {
        return permissionService.hasPermission(userManager.getRemoteUsername(), permission, module);
    }

    public boolean isAdmin()
    {
        if (userManager.getRemoteUsername() == null)
        {
            return false;
        }
        return isSystemAdmin() || userManager.isAdmin(userManager.getRemoteUsername());
    }

    public void enforceAdmin()
    {
        if (!isAdmin())
        {
            throw new PermissionDeniedException();
        }
    }

    public boolean isSystemAdmin()
    {
        if (userManager.getRemoteUsername() == null)
        {
            return false;
        }
        return userManager.isSystemAdmin(userManager.getRemoteUsername());
    }

    public void enforceSystemAdmin()
    {
        if (!isSystemAdmin())
        {
            throw new PermissionDeniedException();
        }
    }
}
