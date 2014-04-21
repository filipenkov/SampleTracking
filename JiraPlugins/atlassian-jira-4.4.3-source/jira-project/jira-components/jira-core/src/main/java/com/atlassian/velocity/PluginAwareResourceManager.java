package com.atlassian.velocity;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.plugin.event.events.PluginUpgradedEvent;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;

/**
 * Listens for Plugin Uninstall and Upgraded events, and clears the velocity resource cache accordingly. <p>
 * The resource cache contains the velocity templates that have already been rendered by the VelocityEngine.</p>
 */
public class PluginAwareResourceManager extends ResourceManagerImpl
{
    /**
     * Registers this class with the EventPublisher, and delegates to the super class initialize method.
     * @param rs RuntimeServices instance
     * @throws Exception
     */
    public void initialize(final RuntimeServices rs) throws Exception
    {
        super.initialize(rs);
        getEventPublisher().register(this);
    }

    EventPublisher getEventPublisher()
    {
        return ComponentManager.getComponentInstanceOfType(EventPublisher.class);
    }

    /**
     * Clears the velocity resource cache when a PluginUninstalledEvent is fired.
     * @param event PluginUninstalledEvent
     */
    @EventListener
    public void clearCacheOnUninstall(PluginUninstalledEvent event)
    {
        globalCache.clear();
    }

    /**
     * Clears the velocity resource cache when a PluginUpgradedEvent is fired.
     * @param event PluginUpgradedEvent
     */
    @EventListener
    public void clearCacheOnUpgrade(PluginUpgradedEvent event)
    {
        globalCache.clear();
    }
}