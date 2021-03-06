package com.atlassian.upm.permission;

import com.atlassian.upm.spi.Plugin;

/**
 * Service to determine whether or not a specific action is permitted for a given user.
 *
 * As of UPM 2.0.1, this service is no longer a part of UPM's SPI. Now it only has a single implementation.
 */
public interface PermissionService
{
    /**
     * Checks if the specified user has the specified permission.
     *
     * @param username the user whose permissions to check
     * @param permission the permission to check for
     * @return {{true}} if the user has the permission, {{false}} otherwise
     */
    public boolean hasPermission(String username, Permission permission);

    /**
     * Checks if the specified user has the specified permission, in the context of the specified plugin.
     * <p/>
     * This method differs from {@link #hasPermission(String, Permission)} in that it allows the SPI implementer to
     * use plugin information to inform the permission decision.
     *
     * @param username the user whose permissions to check
     * @param permission the permission to check for
     * @param plugin specifies the context of the permission check, can be null.
     * @return {{true}} if the user has the permission in context of the plugin, {{false}} otherwise
     */
    public boolean hasPermission(String username, Permission permission, Plugin plugin);

    /**
     * Checks if the specified user has the specified permission, in the context of the specified module.
     * <p/>
     * This method differs from {@link #hasPermission(String, Permission)} in that it allows the SPI implementer to
     * use module information to inform the permission decision.
     *
     * @param username the user whose permissions to check
     * @param permission the permission to check for
     * @param module specifies the context of the permission check, can be null.
     * @return {{true}} if the user has the permission in context of the module, {{false}} otherwise
     */
    public boolean hasPermission(String username, Permission permission, Plugin.Module module);
}
