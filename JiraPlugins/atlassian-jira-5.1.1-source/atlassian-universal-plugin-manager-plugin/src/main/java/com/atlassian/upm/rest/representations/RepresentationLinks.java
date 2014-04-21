package com.atlassian.upm.rest.representations;

/**
 * A common place to put representation link relations such that we don't duplicate
 * these hard coded strings all over the place.
 */
public class RepresentationLinks
{
    //common plugin rels
    public static final String SELF_REL = "self";
    public static final String MODIFY_REL = "modify";
    public static final String DELETE_REL = "delete";
    public static final String DETAILS_REL = "details";
    public static final String BINARY_REL = "binary";
    public static final String PAC_DETAILS_REL = "pac-details";
    public static final String UPDATE_DETAILS_REL = "update-details";
    public static final String PLUGIN_DETAILS_REL = "plugin-details";
    public static final String CHANGE_REQUIRING_RESTART_REL = "change-requiring-restart";

    //plugin media rels
    public static final String PLUGIN_LOGO_REL = "plugin-logo";
    public static final String PLUGIN_ICON_REL = "plugin-icon";
    public static final String PLUGIN_BANNER_REL = "plugin-banner";

    //marketplace rels
    public static final String UPDATE_LICENSE_REL = "update-license";
    public static final String LICENSE_CALLBACK_REL = "license-callback";
    public static final String TRY_LICENSE_REL = "try";
    public static final String NEW_LICENSE_REL = "new";
    public static final String UPGRADE_LICENSE_REL = "upgrade";
    public static final String RENEW_LICENSE_REL = "renew";
    public static final String RENEW_LICENSE_REQUIRES_CONTACT_REL = "renew-requires-contact";

    //resource rels
    public static final String PLUGIN_REL = "plugin";
    public static final String AVAILABLE_REL = "available";
    public static final String FEATURED_REL = "featured";
    public static final String POPULAR_REL = "popular";
    public static final String SUPPORTED_REL = "supported";
    public static final String UPDATES_REL = "updates";
    public static final String NOTIFICATIONS_REL = "notifications";
    public static final String INSTALLED_REL = "installed";
    public static final String PRODUCT_UPDATES_REL = "product-updates";
    public static final String CHANGES_REQUIRING_RESTART_REL = "changes-requiring-restart";
    public static final String PENDING_TASKS_REL = "pending-tasks";
    public static final String INSTALL_REL = "install";
    public static final String UPDATE_ALL_REL = "update-all";

    //audit log
    public static final String AUDIT_LOG_REL = "audit-log";
    public static final String AUDIT_LOG_MAX_ENTRIES_REL = "audit-log-max-entries";
    public static final String AUDIT_LOG_PURGE_AFTER_REL = "audit-log-purge-after";
    public static final String AUDIT_LOG_PURGE_AFTER_MANAGE_REL = "audit-log-purge-after-manage";

    //safe mode rels
    public static final String EXIT_SAFE_MODE_RESTORE_REL = "exit-safe-mode-restore";
    public static final String EXIT_SAFE_MODE_KEEP_REL = "exit-safe-mode-keep";
    public static final String ENTER_SAFE_MODE_REL = "enter-safe-mode";

    //osgi rels
    public static final String OSGI_BUNDLES_REL = "osgi-bundles";
    public static final String OSGI_SERVICES_REL = "osgi-services";
    public static final String OSGI_PACKAGES_REL = "osgi-packages";
}
