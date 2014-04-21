package com.atlassian.upm.permission;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.spi.Plugin;

import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static com.atlassian.upm.Sys.isOnDemand;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Specifies what actions are allowed based on the user's roles (sysadmin, admin, or neither)
 * and environment details (OnDemand or Behind the Firewall).
 */
public class PermissionServiceImpl implements PermissionService
{
    private final UserManager userManager;
    private final PluginAccessorAndController pluginAccessorAndController;

    static final String CONFLUENCE_MACROS_HTML = "confluence.macros.html:html";
    static final String CONFLUENCE_MACROS_HTML_INCLUDE = "confluence.macros.html:html-include";

    public PermissionServiceImpl(UserManager userManager, PluginAccessorAndController pluginAccessorAndController)
    {
        this.userManager = checkNotNull(userManager, "userManager");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    public boolean hasPermission(String username, Permission permission)
    {
        switch (permission)
        {
            //Requires either admin or sysadmin privileges (for either BTF or OnDemand)
            case GET_AUDIT_LOG:
            case GET_PLUGIN_MODULES:
            case GET_SAFE_MODE:
            case MANAGE_SAFE_MODE:
                return userManager.isSystemAdmin(username) || userManager.isAdmin(username);

            //Requires either admin or sysadmin privileges (BTF), or for OnDemand, sysadmins (true) or admins (must be determined on a plugin-specific basis)
            case MANAGE_PLUGIN_ENABLEMENT:
            case MANAGE_PLUGIN_MODULE_ENABLEMENT:
                if (!isOnDemand() || userManager.isSystemAdmin(username))
                {
                    return userManager.isSystemAdmin(username) || userManager.isAdmin(username);
                }
                else
                {
                    throw new UnsupportedOperationException("This permission depends on the particular plugin being operated on.");
                }

            //Requires sysadmin privileges (for either BTF or OnDemand)
            case GET_AVAILABLE_PLUGINS:
            case GET_PRODUCT_UPDATE_COMPATIBILITY:
            case MANAGE_PLUGIN_INSTALL:
            case MANAGE_PLUGIN_UNINSTALL:
            case MANAGE_AUDIT_LOG:
                return userManager.isSystemAdmin(username);

            //Requires either admin or sysadmin privileges (BTF), or sysadmin privileges (OnDemand)
            case GET_OSGI_STATE:
                return userManager.isSystemAdmin(username) || (userManager.isAdmin(username) && !isOnDemand());

            //Requires either admin or sysadmin privileges (BTF). Always denied when OnDemand.
            case MANAGE_PLUGIN_LICENSE: // if this is ever changed to be allowed in onDemand mode, PluginResource.put will also need to be changed
            case GET_NOTIFICATIONS:
            case MANAGE_NOTIFICATIONS:
                return (userManager.isSystemAdmin(username) || userManager.isAdmin(username)) && !isOnDemand();

            default:
                throw new IllegalArgumentException("Unhandled permission: " + permission);
        }
    }

    public boolean hasPermission(String username, Permission permission, Plugin plugin)
    {
        if (MANAGE_PLUGIN_ENABLEMENT == permission || MANAGE_PLUGIN_MODULE_ENABLEMENT == permission)
        {
            // UPM-1770 OnDemand allows sysadmins to enable and disable all plugins, but admins to only enable and disable "optional plugins".
            if (isOnDemand())
            {
                return userManager.isSystemAdmin(username) || (userManager.isAdmin(username) && pluginAccessorAndController.isUserInstalled(plugin));
            }
        }

        return hasPermission(username, permission);
    }

    public boolean hasPermission(String username, Permission permission, Plugin.Module module)
    {
        if (module != null)
        {
            if (MANAGE_PLUGIN_MODULE_ENABLEMENT == permission)
            {
                // UPM-862 - hack for Confluence so their html modules will not be disabled/enabled by those others than sys-admins
                // We will remove this before we go final and once Confluence have implemented this for themselves
                final String moduleCompleteKey = module.getCompleteKey();
                if (CONFLUENCE_MACROS_HTML.equals(moduleCompleteKey) || CONFLUENCE_MACROS_HTML_INCLUDE.equals(moduleCompleteKey))
                {
                    return userManager.isSystemAdmin(username);
                }

                // UPM-1770 OnDemand allows sysadmins to enable and disable all plugins, but admins to only enable and disable "optional plugins".
                if (isOnDemand())
                {
                    return userManager.isSystemAdmin(username) || (userManager.isAdmin(username) && pluginAccessorAndController.isUserInstalled(module.getPlugin()));
                }
            }
        }
        return hasPermission(username, permission);
    }
}
