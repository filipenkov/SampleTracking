package com.atlassian.upm.spi;

/**
 * Permissions that guard secure UPM actions.
 */
public enum Permission
{
    // Available (and upgradeable) plugins
    GET_AVAILABLE_PLUGINS,
    MANAGE_PLUGIN_INSTALL,

    // Installed plugins
    MANAGE_PLUGIN_UNINSTALL,
    MANAGE_PLUGIN_ENABLEMENT,

    // Plugin modules
    GET_PLUGIN_MODULES,
    MANAGE_PLUGIN_MODULE_ENABLEMENT,

    // OSGi
    GET_OSGI_STATE,

    // Compatibility checking
    GET_PRODUCT_UPGRADE_COMPATIBILITY,

    // Audit log
    GET_AUDIT_LOG,
    MANAGE_AUDIT_LOG,

    // Safe mode
    GET_SAFE_MODE,
    MANAGE_SAFE_MODE;
}
