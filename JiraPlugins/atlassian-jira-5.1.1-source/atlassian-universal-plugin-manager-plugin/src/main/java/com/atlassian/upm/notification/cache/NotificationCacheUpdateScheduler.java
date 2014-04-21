package com.atlassian.upm.notification.cache;

import java.util.Date;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.atlassian.upm.UpmScheduler;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.notification.NotificationType;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Executes a periodic task to update data made available to plugin notifications.
 */
public class NotificationCacheUpdateScheduler extends UpmScheduler
{
    private static final String LICENSE_REPOSITORY = "licenseRepository";
    private static final String NOTIFICATION_UPDATER = "notificationCacheUpdater";
    private static final String NOTIFICATION_CACHE = "notificationCache";

    private final NotificationCacheUpdater notificationCacheUpdater;
    private final NotificationCache cache;
    private final PluginLicenseRepository licenseRepository;

    public NotificationCacheUpdateScheduler(PluginScheduler pluginScheduler,
                                            NotificationCacheUpdater notificationCacheUpdater,
                                            NotificationCache cache,
                                            PluginLicenseRepository licenseRepository,
                                            EventPublisher eventPublisher)
    {
        super(pluginScheduler, eventPublisher);
        this.notificationCacheUpdater = checkNotNull(notificationCacheUpdater, "notificationCacheUpdater");
        this.cache = checkNotNull(cache, "cache");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
    }

    @Override
    public Map<String, Object> getJobData()
    {
        return ImmutableMap.of(
            NOTIFICATION_UPDATER, notificationCacheUpdater,
            NOTIFICATION_CACHE, cache,
            LICENSE_REPOSITORY, licenseRepository
        );
    }

    @Override
    public PluginJob getPluginJob()
    {
        return new NotificationCacheUpdateJob();
    }

    /**
     * Schedule-able job to check for plugin notification data
     */
    public static final class NotificationCacheUpdateJob implements PluginJob
    {
        private static final Logger log = getLogger(NotificationCacheUpdateJob.class);

        public void execute(final Map<String, Object> jobDataMap)
        {
            NotificationCacheUpdater notificationCacheUpdater = (NotificationCacheUpdater) get(jobDataMap, NOTIFICATION_UPDATER);
            NotificationCache cache = (NotificationCache) get(jobDataMap, NOTIFICATION_CACHE);
            PluginLicenseRepository licenseRepository = (PluginLicenseRepository) get(jobDataMap, LICENSE_REPOSITORY);

            // Clear the cache so that the notifications get updated immediately.
            licenseRepository.invalidateCache();

            //update all notification caches
            notificationCacheUpdater.updateAllNotifications();

            //un-dismiss all "evaluations are nearly expired" notifications such that
            //they show up again every day for their last week before expiring
            cache.resetNotificationTypeDismissal(NotificationType.NEARLY_EXPIRED_EVALUATION_PLUGIN_LICENSE);
            cache.resetNotificationTypeDismissal(NotificationType.MAINTENANCE_NEARLY_EXPIRED_PLUGIN_LICENSE);

            log.debug("Plugin notification job has been executed at: " + new Date());
        }

        private static Object get(Map<String, Object> jobDataMap, String dataName)
        {
            return checkNotNull(jobDataMap.get(dataName), dataName);
        }
    }
}
