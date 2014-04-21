package com.atlassian.jira.multitenant;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.multitenant.MultiTenantComponentMap;

/**
 * Destroys the event publisher
 */
public class EventPublisherDestroyer
{
    private final MultiTenantComponentMap<EventPublisher> eventPublisherMap;

    public EventPublisherDestroyer(MultiTenantComponentMap<EventPublisher> eventPublisherMap)
    {
        this.eventPublisherMap = eventPublisherMap;
    }

    public void destroyEventPublisher()
    {
        eventPublisherMap.destroy();
    }

}
