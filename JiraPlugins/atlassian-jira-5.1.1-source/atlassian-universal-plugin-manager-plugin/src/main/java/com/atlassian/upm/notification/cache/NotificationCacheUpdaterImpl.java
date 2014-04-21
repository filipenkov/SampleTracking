package com.atlassian.upm.notification.cache;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.pac.PacClient;

import com.google.common.base.Predicate;

import com.google.common.collect.ImmutableList;
import org.joda.time.Days;
import org.slf4j.Logger;

import static com.atlassian.upm.license.PluginLicenses.RECENTLY_EXPIRED_DAYS;
import static com.atlassian.upm.license.PluginLicenses.getDaysSinceMaintenanceExpiry;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.getMaintenanceNearlyExpiredPluginLicenses;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.getMaintenanceRecentlyExpiredPluginLicenses;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.getNearlyExpiredEvaluationPluginLicenses;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.getRecentlyExpiredEvaluationPluginLicenses;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.getUserMismatchPluginLicenses;
import static com.atlassian.upm.license.internal.PluginLicensesInternal.toPluginKey;
import static com.atlassian.upm.pac.PluginVersions.notUpm;
import static com.atlassian.upm.pac.PluginVersions.toPluginKeys;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.slf4j.LoggerFactory.getLogger;

public class NotificationCacheUpdaterImpl implements NotificationCacheUpdater
{
    private static final Logger log = getLogger(NotificationCacheUpdaterImpl.class);

    private final NotificationCache cache;
    private final PacClient pacClient;
    private final PluginLicenseRepository licenseRepository;
    private final PluginAccessorAndController pluginAccessorAndController;

    public NotificationCacheUpdaterImpl(
        NotificationCache cache,
        PacClient pacClient,
        PluginLicenseRepository licenseRepository,
        PluginAccessorAndController pluginAccessorAndController)
    {
        this.cache = checkNotNull(cache, "cache");
        this.pacClient = checkNotNull(pacClient, "pacClient");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    public void updateAllNotifications()
    {
        updatePluginUpdateNotification();
        updateUserMismatchPluginLicenseNotification();
        updateExpiredEvaluationPluginLicenseNotification();
        updateNearlyExpiredEvaluationPluginLicenseNotification();
        updateMaintenanceExpiredPluginLicenseNotification();
        updateMaintenanceNearlyExpiredPluginLicenseNotification();
    }

    public void updatePluginUpdateNotification()
    {
        try
        {
            //filter out UPM from update list since we aren't displaying it in the update UI.
            //also filter out non-recent incompatible updates (because of maintenance expiry)
            Iterable<PluginVersion> updatablePluginVersions = filter(pacClient.getUpdates(),
                    and(notUpm(pluginAccessorAndController), not(nonRecentIncompatibleUpdate())));
            cache.setNotifications(NotificationType.PLUGIN_UPDATE_AVAILABLE, toPluginKeys(updatablePluginVersions));
        }
        catch (Exception e)
        {
            log.warn("Automatic plugin update check failed", e);
            //since PAC is unreachable (or other error), we have no idea what plugins are updatable.
            //as a result, let's not display any notifications for them until we know for sure.
            //displaying notifications for plugins that have actually been uninstalled will cause NPEs.
            cache.setNotifications(NotificationType.PLUGIN_UPDATE_AVAILABLE, ImmutableList.<String>of());
        }
        // Discard any cached PAC-availability state, because we don't want to stop checking for updates
        // forever just because PAC was unavailable right now
        pacClient.forgetPacReachableState();
    }

    public void updateUserMismatchPluginLicenseNotification()
    {
        Iterable<String> pluginKeys = transform(getUserMismatchPluginLicenses(licenseRepository), toPluginKey());
        cache.setNotifications(NotificationType.USER_MISMATCH_PLUGIN_LICENSE, pluginKeys);
    }

    public void updateExpiredEvaluationPluginLicenseNotification()
    {
        Iterable<String> pluginKeys = transform(getRecentlyExpiredEvaluationPluginLicenses(licenseRepository, pluginAccessorAndController), toPluginKey());
        cache.setNotifications(NotificationType.EXPIRED_EVALUATION_PLUGIN_LICENSE, pluginKeys);
    }

    public void updateNearlyExpiredEvaluationPluginLicenseNotification()
    {
        Iterable<String> pluginKeys = transform(getNearlyExpiredEvaluationPluginLicenses(licenseRepository, pluginAccessorAndController), toPluginKey());
        cache.setNotifications(NotificationType.NEARLY_EXPIRED_EVALUATION_PLUGIN_LICENSE, pluginKeys);
    }

    public void updateMaintenanceExpiredPluginLicenseNotification()
    {
        Iterable<String> pluginKeys = transform(getMaintenanceRecentlyExpiredPluginLicenses(licenseRepository, pluginAccessorAndController), toPluginKey());
        cache.setNotifications(NotificationType.MAINTENANCE_EXPIRED_PLUGIN_LICENSE, pluginKeys);
    }

    public void updateMaintenanceNearlyExpiredPluginLicenseNotification()
    {
        Iterable<String> pluginKeys = transform(getMaintenanceNearlyExpiredPluginLicenses(licenseRepository, pluginAccessorAndController), toPluginKey());
        cache.setNotifications(NotificationType.MAINTENANCE_NEARLY_EXPIRED_PLUGIN_LICENSE, pluginKeys);
    }

    public void handlePluginUninstallation(String pluginKey)
    {
        for (NotificationType type : NotificationType.values())
        {
            cache.resetNotificationDismissal(type, pluginKey);
        }
        updateAllNotifications();
    }

    private Predicate<PluginVersion> nonRecentIncompatibleUpdate()
    {
        return new Predicate<PluginVersion>()
        {
            @Override
            public boolean apply(PluginVersion pluginVersion)
            {
                for (PluginLicense pluginLicense : licenseRepository.getPluginLicense(pluginVersion.getPlugin().getPluginKey()))
                {
                    for (Days days : getDaysSinceMaintenanceExpiry(pluginLicense))
                    {
                        return days.getDays() >= RECENTLY_EXPIRED_DAYS;
                    }
                }

                return false;
            }
        };
    }
}
