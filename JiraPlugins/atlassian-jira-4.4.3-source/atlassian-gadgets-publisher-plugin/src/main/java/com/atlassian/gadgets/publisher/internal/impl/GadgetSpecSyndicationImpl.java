package com.atlassian.gadgets.publisher.internal.impl;

import java.util.Date;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.plugins.PluginGadgetSpecEventListener;
import com.atlassian.gadgets.publisher.internal.GadgetSpecSyndication;
import com.atlassian.gadgets.util.GadgetSpecUrlBuilder;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;

public class GadgetSpecSyndicationImpl implements GadgetSpecSyndication, PluginGadgetSpecEventListener
{
    private static final String MODULE_KEY = "com.atlassian.gadgets.publisher:ajs-gadgets";
    
    private final PublishedGadgetSpecStore store;
    private final GadgetSpecUrlBuilder urlBuilder;
    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;
    
    private volatile Date lastModified = new Date();
    private final WebResourceManager webResourceManager;
    
    public GadgetSpecSyndicationImpl(PublishedGadgetSpecStore store,
            GadgetSpecUrlBuilder urlBuilder,
            ApplicationProperties applicationProperties,
            PluginAccessor pluginAccessor,
            WebResourceManager webResourceManager)
    {
        this.store = checkNotNull(store, "store");
        this.urlBuilder = checkNotNull(urlBuilder, "urlBuilder");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.pluginAccessor = checkNotNull(pluginAccessor, "pluginAccessor");
        this.webResourceManager = checkNotNull(webResourceManager, "webResourceManager");
    }
    
    public Feed getFeed()
    {
        Feed feed = new Feed();
        feed.setId(urlBuilder.buildGadgetSpecFeedUrl());
        feed.setTitle("Gadget specs published from " + applicationProperties.getBaseUrl());
        feed.setUpdated(lastModified);
        
        Person person = new Person();
        person.setName(applicationProperties.getDisplayName());
        feed.setAuthors(ImmutableList.of(person));

        feed.setIcon(webResourceManager.getStaticPluginResource(
            MODULE_KEY, "images/icons/" + applicationProperties.getDisplayName().toLowerCase() + ".png", UrlMode.ABSOLUTE));
        
        addLink(feed, applicationProperties.getBaseUrl(), "base");
        
        addGadgetSpecEntries(feed);
        return feed;
    }

    @SuppressWarnings("unchecked")
    private void addLink(Feed feed, String baseUrl, String rel)
    {
        Link link = new Link();
        link.setHref(baseUrl);
        link.setRel(rel);
        feed.getOtherLinks().add(link);
    }

    @SuppressWarnings("unchecked")
    private boolean addGadgetSpecEntries(Feed feed)
    {
        return feed.getEntries().addAll(transform(store.getAll(), toEntries()));
    }

    private Function<PluginGadgetSpec, Entry> toEntries()
    {
        return pluginGadgetSpecToEntryFunction;
    }
    
    private final Function<PluginGadgetSpec, Entry> pluginGadgetSpecToEntryFunction = new Function<PluginGadgetSpec, Entry>()
    {
        public Entry apply(PluginGadgetSpec spec)
        {
            String specUrl = Uri.resolveUriAgainstBase(applicationProperties.getBaseUrl(), urlBuilder.buildGadgetSpecUrl(spec.getPluginKey(), spec.getModuleKey(), spec.getLocation())).toASCIIString();
            
            Entry entry = new Entry();
            entry.setId(specUrl);
            entry.setUpdated(pluginAccessor.getPlugin(spec.getPluginKey()).getDateLoaded());
            entry.setTitle("Gadget spec at " + entry.getId());
            addAlternateLink(entry, specUrl);
            return entry;
        }

        @SuppressWarnings("unchecked")
        private void addAlternateLink(Entry entry, String specUrl)
        {
            Link link = new Link();
            link.setHref(specUrl);
            link.setRel("alternate");
            entry.getAlternateLinks().add(link);
        }
    };

    public void pluginGadgetSpecEnabled(PluginGadgetSpec pluginGadgetSpec) throws GadgetParsingException
    {
        checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
        if (pluginGadgetSpec.isHostedExternally())
        {
            return;
        }
        lastModified = new Date();
    }

    public void pluginGadgetSpecDisabled(PluginGadgetSpec pluginGadgetSpec)
    {
        checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
        if (pluginGadgetSpec.isHostedExternally())
        {
            return;
        }
        lastModified = new Date();
    }
}
