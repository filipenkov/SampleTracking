package com.atlassian.upm.rest;

/**
 * String constants for custom content types used in the UPM
 */
public class MediaTypes
{
    /**
     * Content type denoting an install task in the downloading phase
     */
    public static final String INSTALL_DOWNLOADING_JSON = "application/vnd.atl.plugins.install.downloading+json";

    /**
     * Content type denoting an install task that requires an additional HTTP request to continue the process
     */
    public static final String INSTALL_NEXT_TASK_JSON = "application/vnd.atl.plugins.install.next-task+json";

    /**
     * Content type denoting a completed install task
     */
    public static final String INSTALL_COMPLETE_JSON = "application/vnd.atl.plugins.install.complete+json";

    /**
     * Content type denoting an install task in the installing phase
     */
    public static final String INSTALL_INSTALLING_JSON = "application/vnd.atl.plugins.install.installing+json";

    /**
     * Content type denoting an install task that is in error
     */
    public static final String INSTALL_ERR_JSON = "application/vnd.atl.plugins.task.install.err+json";

    /**
     * Content type denoting an asynchronous task that is in error
     */
    public static final String TASK_ERROR_JSON = "application/vnd.atl.plugins.task.error+json";

    /**
     * Content type denoting a cancellable asynchronous test task
     */
    public static final String CANCELLABLE_TASK_JSON = "application/vnd.atl.plugins.cancellable.blocking+json";

    /**
     * Content type for errors in the UPM
     */
    public static final String ERROR_JSON = "application/vnd.atl.plugins.error+json";

    /**
     * Content type for error while reenabling a plugin while exiting safe mode
     */
    public static final String SAFE_MODE_ERROR_REENABLING_PLUGIN_JSON = "application/vnd.atl.plugins.safemode.error-reenabling-plugin+json";

    /**
     * Content type for error while reenabling a plugin module while exiting safe mode
     */
    public static final String SAFE_MODE_ERROR_REENABLING_PLUGIN_MODULE_JSON = "application/vnd.atl.plugins.safemode.error-reenabling-plugin-module+json";

    /**
     * Content type for pending tasks
     */
    public static final String PENDING_TASK_JSON = "application/vnd.atl.plugins.pending-task+json";

    /**
     * Content type for a collection of pending tasks
     */
    public static final String PENDING_TASKS_COLLECTION_JSON = "application/vnd.atl.plugins.pending-tasks+json";

    /**
     * Content type for a request to update all
     */
    public static final String UPDATE_ALL_REQUEST_JSON = "application/vnd.atl.plugins.updateall+json";

    /**
     * Content type for an update all task that is finding updates
     */
    public static final String UPDATE_ALL_FINDING_JSON = "application/vnd.atl.plugins.updateall.finding+json";

    /**
     * Content type for an update task that is downloading plugins to update
     */
    public static final String UPDATE_ALL_DOWNLOADING_JSON = "application/vnd.atl.plugins.updateall.downloading+json";

    /**
     * Content type for an update task that is performing the update
     */
    public static final String UPDATE_ALL_UPDATING_JSON = "application/vnd.atl.plugins.updateall.updating+json";

    /**
     * Content type for an update task that is complete
     */
    public static final String UPDATE_ALL_COMPLETE_JSON = "application/vnd.atl.plugins.updateall.complete+json";

    /**
     * Content type for an update task that is in error
     */
    public static final String UPDATE_ALL_ERR_JSON = "application/vnd.atl.plugins.updateall.err+json";

    /**
     * Content type for installed plugins collection
     */
    public static final String INSTALLED_PLUGINS_COLLECTION_JSON = "application/vnd.atl.plugins.installed+json";

    /**
     * Content type for a URI to install from
     */
    public static final String INSTALL_URI_JSON = "application/vnd.atl.plugins.install.uri+json";

    /**
     * Content type for compatibility info
     */
    public static final String COMPATIBILITY_JSON = "application/vnd.atl.plugins.compatibility+json";

    /**
     * Content type for installed plugins
     */
    public static final String INSTALLED_PLUGIN_JSON = "application/vnd.atl.plugins.plugin+json";

    /**
     * Content type for plugin modules
     */
    public static final String PLUGIN_MODULE_JSON = "application/vnd.atl.plugins.plugin.module+json";

    /**
     * Content type for available / featured plugins
     */
    public static final String AVAILABLE_FEATURED_JSON = "application/vnd.atl.plugins.available.featured+json";

    /**
     * Content type for an available plugin
     */
    public static final String AVAILABLE_PLUGIN_JSON = "application/vnd.atl.plugins.available.plugin+json";

    /**
     * Content type for available plugins collection
     */
    public static final String AVAILABLE_PLUGINS_COLLECTION_JSON = "application/vnd.atl.plugins.available+json";

    /**
     * Content type for updates
     */
    public static final String UPDATES_JSON = "application/vnd.atl.plugins.updates+json";

    /**
     * Content type for product updates
     */
    public static final String PRODUCT_UPDATES_JSON = "application/vnd.atl.plugins.product.updates+json";

    /**
     * Content type for popular plugins
     */
    public static final String POPULAR_PLUGINS_JSON = "application/vnd.atl.plugins.popular+json";

    /**
     * Content type for supported plugins
     */
    public static final String SUPPORTED_PLUGINS_JSON = "application/vnd.atl.plugins.supported+json";

    /**
     * Content type for safe mode flag
     */
    public static final String SAFE_MODE_FLAG_JSON = "application/vnd.atl.plugins.safe.mode.flag+json";

    /**
     * Content type for changes requiring restart
     */
    public static final String CHANGES_REQUIRING_RESTART_JSON = "application/vnd.atl.plugins.changes.requiring.restart+json";

    /**
     * Content type for purge after policy in the audit log
     */
    public static final String AUDIT_LOG_PURGE_AFTER_JSON = "application/vnd.atl.plugins.audit.log.purge.after+json";

    /**
     * Content type for max entries in the audit log
     */
    public static final String AUDIT_LOG_MAX_ENTRIES_JSON = "application/vnd.atl.plugins.audit.log.max.entries+json";

    /**
     * Content type for entries in the audit log
     */
    public static final String AUDIT_LOG_ENTRIES_JSON = "application/vnd.atl.plugins.audit.log.entries+json";

    /**
     * Content type for OSGi bundles
     */
    public static final String OSGI_BUNDLE_JSON = "application/vnd.atl.plugins.osgi.bundle+json";

    /**
     * Content type for collections of OSGi bundles
     */
    public static final String OSGI_BUNDLE_COLLECTION_JSON = "application/vnd.atl.plugins.osgi.bundles+json";

    /**
     * Content type for OSGi services
     */
    public static final String OSGI_SERVICE_JSON = "application/vnd.atl.plugins.osgi.service+json";

    /**
     * Content type for collections of OSGi services
     */
    public static final String OSGI_SERVICE_COLLECTION_JSON = "application/vnd.atl.plugins.osgi.services+json";

    /**
     * Content type for OSGi packages
     */
    public static final String OSGI_PACKAGE_JSON = "application/vnd.atl.plugins.osgi.package+json";

    /**
     * Content type for collections of OSGi packages
     */
    public static final String OSGI_PACKAGE_COLLECTION_JSON = "application/vnd.atl.plugins.osgi.packages+json";

    /**
     * Content type for build number.
     */
    public static final String BUILD_NUMBER_JSON = "application/vnd.atl.plugins.build.number+json";

    /**
     * Content type for PAC base URL.
     */
    public static final String PAC_BASE_URL_JSON = "application/vnd.atl.plugins.pac.base.url+json";

    /**
     * Content type for PAC availability.
     */
    public static final String PAC_STATUS_JSON = "application/vnd.atl.plugins.pac.status+json";

    /**
     * Content type for PAC mode (online or offline).
     */
    public static final String PAC_MODE_JSON = "application/vnd.atl.plugins.pac.mode+json";

    /**
     * Content type for a PAC details link.
     */
    public static final String PAC_DETAILS = "application/vnd.atl.plugins.pac.details+json";

    /**
     * Content type for UPM.
     */
    public static final String UPM_JSON = "application/vnd.atl.plugins+json";
}
