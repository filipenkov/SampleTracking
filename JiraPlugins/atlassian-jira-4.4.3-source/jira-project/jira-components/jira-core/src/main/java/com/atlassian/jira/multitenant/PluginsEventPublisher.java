package com.atlassian.jira.multitenant;

import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.multitenant.event.PeeringEventPublisher;
import com.atlassian.multitenant.event.PeeringEventPublisherManager;

/**
 * This is an event publisher specifically for the plugins system.  The reason this class exists is to distinguish it 
 * in PICO from the one
 *
 * @since v4.3
 */
public class PluginsEventPublisher extends PeeringEventPublisher
{
    public PluginsEventPublisher(PeeringEventPublisherManager manager, EventDispatcher eventDispatcher,
            ListenerHandlersConfiguration listenerHandlersConfiguration)
    {
        super(manager, new EventPublisherImpl(eventDispatcher, listenerHandlersConfiguration));
    }
    
}
