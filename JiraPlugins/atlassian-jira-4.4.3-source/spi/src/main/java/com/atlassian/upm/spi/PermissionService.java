package com.atlassian.upm.spi;

/**
 * An SPI interface that products can use to specify fine-grained permissions for access to UPM.
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
