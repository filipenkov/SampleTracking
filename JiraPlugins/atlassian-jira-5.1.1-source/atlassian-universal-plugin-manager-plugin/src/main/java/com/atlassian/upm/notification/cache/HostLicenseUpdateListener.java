package com.atlassian.upm.notification.cache;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.upm.license.internal.HostLicenseEventReader;
import com.atlassian.upm.license.internal.PluginLicenseRepository;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public class HostLicenseUpdateListener implements InitializingBean, DisposableBean
{
    private final NotificationCacheUpdater notificationCacheUpdater;
    private final EventPublisher eventPublisher;
    private final HostLicenseEventReader hostLicenseEventReader;
    private final PluginLicenseRepository licenseRepository;

    public HostLicenseUpdateListener(EventPublisher eventPublisher,
                                     NotificationCacheUpdater notificationCacheUpdater,
                                     HostLicenseEventReader hostLicenseEventReader,
                                     PluginLicenseRepository licenseRepository)
    {
        this.hostLicenseEventReader = hostLicenseEventReader;
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.notificationCacheUpdater = checkNotNull(notificationCacheUpdater, "notificationCacheUpdate");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void handleHostLicenseUpdatedEvent(Object event)
    {
        if (hostLicenseEventReader.isHostLicenseUpdated(event))
        {
            // Clear the cache so that the notifications get updated immediately.
            licenseRepository.invalidateCache();
            // An updated host license can only affect USER_MISMATCH plugin license errors.
            notificationCacheUpdater.updateUserMismatchPluginLicenseNotification();
        }
    }
}
