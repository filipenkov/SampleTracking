package com.atlassian.upm.notification.cache;

/**
 * Provides methods to update the cached notification data.
 */
public interface NotificationCacheUpdater
{
    /**
     * Update all cached notifications.
     */
    void updateAllNotifications();

    /**
     * Update plugin updates available notifications.
     */
    void updatePluginUpdateNotification();

    /**
     * Update user mismatch notifications.
     */
    void updateUserMismatchPluginLicenseNotification();

    /**
     * Update expired evaluation license notifications.
     */
    void updateExpiredEvaluationPluginLicenseNotification();

    /**
     * Update nearly expired evaluation license notifications.
     */
    void updateNearlyExpiredEvaluationPluginLicenseNotification();

    /**
     * Update maintenance expired license notifications.
     */
    void updateMaintenanceExpiredPluginLicenseNotification();

    /**
     * Update maintenance nearly expired license notifications.
     */
    void updateMaintenanceNearlyExpiredPluginLicenseNotification();

    /**
     * Cleans up the cache after a plugin uninstallation.
     *
     * @param pluginKey the uinstalled plugin
     */
    void handlePluginUninstallation(String pluginKey);
}
