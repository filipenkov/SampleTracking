package com.atlassian.gadgets.publisher.internal.impl;

import java.util.Date;
import java.util.List;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.internal.GadgetSpecSyndication;
import com.atlassian.gadgets.util.GadgetSpecUrlBuilder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecSyndicationImplTest
{
    @Mock PublishedGadgetSpecStore store;
    @Mock GadgetSpecUrlBuilder urlBuilder;
    @Mock ApplicationProperties applicationProperties;
    @Mock PluginAccessor pluginAccessor;
    @Mock WebResourceManager webResourceManager;
    
    GadgetSpecSyndication syndication;
    
    @Before
    public void setUp()
    {
        when(applicationProperties.getBaseUrl()).thenReturn("http://localhost");
        when(applicationProperties.getDisplayName()).thenReturn("Test");
        
        syndication = new GadgetSpecSyndicationImpl(store, urlBuilder, applicationProperties, pluginAccessor, webResourceManager);
    }
    
    @Test
    public void assertThatFeedIsNotNull()
    {
        assertThat(syndication.getFeed(), is(not(nullValue())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void assertThatFeedContainsApplicationAsAuthor()
    {
        assertThat((List<Person>) syndication.getFeed().getAuthors(), contains(person(applicationProperties.getDisplayName())));
    }
    
    @Test
    public void assertThatFeedContainsGadgetSpecInStore()
    {
        PluginGadgetSpec spec = newPluginGadgetSpec("gadget.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec));

        assertThat(syndication.getFeed().getEntries().size(), is(equalTo(1)));
    }
    
    @Test
    public void assertThatFeedContainsAsManyEntriesAsGadgetsInStore()
    {
        PluginGadgetSpec spec1 = newPluginGadgetSpec("gadget.xml");
        PluginGadgetSpec spec2 = newPluginGadgetSpec("gadget2.xml");
        PluginGadgetSpec spec3 = newPluginGadgetSpec("gadget3.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec1, spec2, spec3));
        
        assertThat(syndication.getFeed().getEntries().size(), is(equalTo(3)));
    }
    
    @Test
    public void assertThatFeedEntryIdIsGadgetSpecUrl()
    {
        PluginGadgetSpec spec = newPluginGadgetSpec("gadget.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec));
        Entry entry = (Entry) syndication.getFeed().getEntries().get(0);
        
        assertThat(entry.getId(), is(equalTo(urlOf(spec))));
    }
    
    @Test
    public void assertThatFeedEntryTitleContainsGadgetSpecUrl()
    {
        PluginGadgetSpec spec = newPluginGadgetSpec("gadget.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec));
        Entry entry = (Entry) syndication.getFeed().getEntries().get(0);
        
        assertThat(entry.getTitle(), containsString(urlOf(spec)));
    }
    
    @Test
    public void assertThatFeedEntryUpdatedIsEqualToWhenPluginWasUploaded()
    {
        PluginGadgetSpec spec = newPluginGadgetSpec("gadget.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec));
        Entry entry = (Entry) syndication.getFeed().getEntries().get(0);
        
        assertThat(entry.getUpdated(), is(equalTo(pluginContaining(spec).getDateLoaded())));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertThatFeedEntryContainsAlternateLinkToGadgetSpec()
    {
        PluginGadgetSpec spec = newPluginGadgetSpec("gadget.xml");
        when(store.getAll()).thenReturn(ImmutableSet.of(spec));
        Entry entry = (Entry) syndication.getFeed().getEntries().get(0);
        
        assertThat((List<Link>) entry.getAlternateLinks(), contains(alternateLinkWith(urlOf(spec))));
    }

    private Person person(String name)
    {
        Person p = new Person();
        p.setName(name);
        return p;
    }
    
    private Link alternateLinkWith(String url)
    {
        Link link = new Link();
        link.setHref(url);
        link.setRel("alternate");
        return link;
    }

    private int pluginId = 1;
    private PluginGadgetSpec newPluginGadgetSpec(String location)
    {
        Plugin plugin = mock(Plugin.class);
        String pluginKey = "plugin-" + (pluginId++);
        when(plugin.getKey()).thenReturn(pluginKey);
        when(plugin.getDateLoaded()).thenReturn(new Date(1234567890L * 1000));
        when(pluginAccessor.getPlugin(pluginKey)).thenReturn(plugin);
        
        PluginGadgetSpec spec = new PluginGadgetSpec(plugin, location, location, ImmutableMap.<String, String>of());
        when(urlBuilder.buildGadgetSpecUrl(plugin.getKey(), location, location)).thenReturn(urlOf(pluginKey, location));
        return spec;
    }

    private String urlOf(PluginGadgetSpec spec)
    {
        return urlOf(spec.getPluginKey(), spec.getLocation());
    }
    
    private String urlOf(String pluginKey, String location)
    {
        return "http://localhost/" + pluginKey + "/" + location;
    }
    
    private Plugin pluginContaining(PluginGadgetSpec spec)
    {
        return pluginAccessor.getPlugin(spec.getPluginKey());
    }
}
