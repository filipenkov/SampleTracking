package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import com.atlassian.gadgets.directory.internal.SubscribedGadgetFeed;
import com.atlassian.gadgets.directory.internal.SubscribedGadgetFeedStore;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.transformValues;

public class SubscribedGadgetFeedStoreImpl implements SubscribedGadgetFeedStore
{
    static final String KEY = "com.atlassian.gadgets.directory.SubscribedGadgetFeedStore";
    
    private final PluginSettingsFactory factory;

    public SubscribedGadgetFeedStoreImpl(PluginSettingsFactory factory)
    {
        this.factory = checkNotNull(factory, "factory");
    }
    
    public void add(SubscribedGadgetFeed feed)
    {
        put(ImmutableMap.<String, SubscribedGadgetFeed>builder().putAll(feeds()).put(feed.getId(), feed).build());
    }

    public boolean contains(String feedId)
    {
        return feeds().containsKey(feedId);
    }

    public SubscribedGadgetFeed get(String feedId)
    {
        return feeds().get(feedId);
    }

    public Iterable<SubscribedGadgetFeed> getAll()
    {
        return feeds().values();
    }
    
    public void remove(String feedId)
    {
        put(Maps.filterKeys(feeds(), Predicates.not(Predicates.equalTo(feedId))));
    }

    private void put(Map<String, SubscribedGadgetFeed> feeds)
    {
        PluginSettings settings = factory.createGlobalSettings();
        Properties properties = new Properties();
        properties.putAll(transformValues(feeds, serialize()));
        settings.put(KEY, properties);
    }

    private Map<String, SubscribedGadgetFeed> feeds()
    {
        PluginSettings settings = factory.createGlobalSettings();
        Properties serializedFeeds = (Properties) settings.get(KEY);
        if (serializedFeeds == null)
        {
            return ImmutableMap.of();
        }
        
        ImmutableMap.Builder<String, SubscribedGadgetFeed> feeds = ImmutableMap.builder();
        for (Map.Entry<Object, Object> entry : serializedFeeds.entrySet())
        {
            String id = (String) entry.getKey();
            feeds.put(id, new SubscribedGadgetFeed(id, URI.create((String) entry.getValue())));
        }
        return feeds.build();
    }
    
    private Function<SubscribedGadgetFeed, String> serialize()
    {
        return Serializer.INSTANCE;
    }
    
    private static enum Serializer implements Function<SubscribedGadgetFeed, String>
    {
        INSTANCE;

        public String apply(SubscribedGadgetFeed feed)
        {
            return feed.getUri().toASCIIString();
        }
    }
}
