package com.atlassian.jira.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.plugin.manager.PluginPersistentState;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import net.jcip.annotations.GuardedBy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class JiraPluginPersistentStateStore implements PluginPersistentStateStore, Startable
{
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private volatile Cache cache;

    public JiraPluginPersistentStateStore(final ApplicationProperties applicationProperties, final EventPublisher eventPublisher)
    {
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        reloadKeys();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        reloadKeys();
    }

    private void reloadKeys()
    {
        cache = new Cache(applicationProperties.getStringsWithPrefix(APKeys.GLOBAL_PLUGIN_STATE_PREFIX));
    }

    /**
     * This method is synchronised so that you don't have two people trying to save plugin state
     * at the same time, and over-writing each others changes (if the {@link #keys} has been partially changed.
     */
    public synchronized void save(final PluginPersistentState state)
    {
        // this effectively 'resets' all the keys in the database
        cache.resetDatabase();

        // now set all the new state values and save configuration
        final Map<String, Boolean> map = state.getMap();
        for (final Map.Entry<String, Boolean> entry : map.entrySet())
        {
            applicationProperties.setString(APKeys.GLOBAL_PLUGIN_STATE_PREFIX + "." + entry.getKey(), entry.getValue().toString());
        }

        reloadKeys();
    }

    public PluginPersistentState load()
    {
        return cache.getState();
    }

    class Cache
    {
        private final Set<String> keys;
        private final PluginPersistentState state;

        Cache(final Collection<String> keys)
        {
            final int statePrefixIndex = APKeys.GLOBAL_PLUGIN_STATE_PREFIX.length() + 1;

            this.keys = (keys == null) ? Collections.<String> emptySet() : unmodifiableSet(new HashSet<String>(keys));
            final Map<String, Boolean> stateMap = new HashMap<String, Boolean>();

            for (final String key : this.keys)
            {
                stateMap.put(key.substring(statePrefixIndex), Boolean.valueOf(applicationProperties.getString(key)));
            }
            state = PluginPersistentState.Builder.create().addState(stateMap).toState();
        }

        public PluginPersistentState getState()
        {
            return state;
        }

        @GuardedBy("JiraPluginPersistentStateStore.this")
        void resetDatabase()
        {
            for (final String element : keys)
            {
                applicationProperties.setString(element, null);
            }
        }
    }
}
