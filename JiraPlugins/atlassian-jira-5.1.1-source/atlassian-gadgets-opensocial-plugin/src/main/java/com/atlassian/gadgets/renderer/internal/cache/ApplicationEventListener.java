package com.atlassian.gadgets.renderer.internal.cache;

import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.event.ClearHttpCacheEvent;
import org.springframework.beans.factory.InitializingBean;

/**
 * Applinks aware event listener that clears shindig's cache whenever an applink is registered or deleted.
 *
 * @since v3.0
 */
public class ApplicationEventListener implements InitializingBean
{

    /**
     * The EventPublisher.
     */
    private final EventPublisher eventPublisher;
    private final ClearableCacheProvider cacheProvider;

    /**
     * Creates a new ApplicationEventListener.
     *
     * @param eventPublisher an EventPublisher
     * @param cacheProvider a cachprovider that can have its cache cleared.
     */
    public ApplicationEventListener(EventPublisher eventPublisher, ClearableCacheProvider cacheProvider)
    {
        this.eventPublisher = eventPublisher;
        this.cacheProvider = cacheProvider;
    }

    /**
     * When the host applicaiton fires a clear shindig cache event this cache should be cleared!
     *
     * @param clearShindigCacheEvent a clear cache event
     */
    @EventListener
    public void onClearCache(ClearHttpCacheEvent clearShindigCacheEvent)
    {
        cacheProvider.clear();
    }

    /**
     * Clears shindig's cache when a new applink has been created in UAL.
     *
     * @param applicationLinkEvent a ApplicationLinkAddedEvent
     */
    @EventListener
    public void onCreate(ApplicationLinkAddedEvent applicationLinkEvent)
    {
        cacheProvider.clear();
    }

    /**
     * Clears shindig's cache when an applink has been deleted in UAL.
     *
     * @param applicationLinkEvent a ApplicationLinkDeletedEvent
     */
    @EventListener
    public void onDelete(ApplicationLinkDeletedEvent applicationLinkEvent)
    {
        cacheProvider.clear();
    }

    /**
     * Clears shindig's cache when an applink id has been modified in UAL.
     *
     * @param applicationLinkEvent an ApplicationLinksIDChangedEvent
     */
    @EventListener
    public void onUpdateId(ApplicationLinksIDChangedEvent applicationLinkEvent)
    {
        cacheProvider.clear();
    }

    /**
     * Registers this event listener in the event publisher.
     */
    public void afterPropertiesSet()
    {
        eventPublisher.register(this);
    }
}
