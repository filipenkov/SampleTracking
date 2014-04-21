package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.atlassian.gadgets.directory.internal.SubscribedGadgetFeed;
import com.atlassian.gadgets.directory.internal.SubscribedGadgetFeedStore;
import com.atlassian.gadgets.test.MapBackedPluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.directory.internal.impl.SubscribedGadgetFeedStoreImpl.KEY;
import static com.atlassian.hamcrest.DeepIsEqual.deeplyEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscribedGadgetFeedStoreImplTest
{
    private static final URI JIRA_URI = URI.create("http://example.com/jira");
    private static final URI CONF_URI = URI.create("http://example.com/conf");
    private static final URI FE_URI = URI.create("http://example.com/fisheye");
    
    private static final SubscribedGadgetFeed JIRA_FEED = new SubscribedGadgetFeed("1", JIRA_URI);
    private static final SubscribedGadgetFeed CONF_FEED = new SubscribedGadgetFeed("2", CONF_URI);
    private static final SubscribedGadgetFeed FE_FEED = new SubscribedGadgetFeed("3", FE_URI);
    
    @Mock PluginSettingsFactory factory;
    
    SubscribedGadgetFeedStore store;
    
    Map<String, Properties> settings;
    
    @Before
    public void setUp()
    {
        settings = new HashMap<String, Properties>();
        when(factory.createGlobalSettings()).thenReturn(new MapBackedPluginSettings(settings));
        
        store = new SubscribedGadgetFeedStoreImpl(factory);
    }
    
    @Test
    public void assertThatAddingFeedAddsToProperties()
    {
        settings.put(KEY, propertiesWith(JIRA_FEED, CONF_FEED));
        store.add(FE_FEED);
        
        assertThat(settings.get(KEY), is(equalTo(propertiesWith(JIRA_FEED, CONF_FEED, FE_FEED))));
    }
    
    @Test
    public void assertThatGetAllReturnsAllEntriesInTheProperties()
    {
        settings.put(KEY, propertiesWith(JIRA_FEED, CONF_FEED));
        
        assertThat(store.getAll(), contains(JIRA_FEED, CONF_FEED));
    }
    
    @Test
    public void assertThatRemoveRemovesTheFeedUri()
    {
        settings.put(KEY, propertiesWith(JIRA_FEED, CONF_FEED, FE_FEED));
        store.remove(CONF_FEED.getId());
        
        assertThat(settings.get(KEY), is(equalTo(propertiesWith(JIRA_FEED, FE_FEED))));
    }
    
    @Test
    public void assertThatGetAllReturnsEmptyIterableWhenSettingsReturnsNull()
    {
        assertThat(store.getAll(), is(Matchers.<SubscribedGadgetFeed>emptyIterable()));
    }

    private Properties propertiesWith(SubscribedGadgetFeed... feeds)
    {
        Properties props = new Properties();
        for (SubscribedGadgetFeed feed : feeds)
        {
            props.put(feed.getId(), feed.getUri().toASCIIString());
        }
        return props;
    }

    private Matcher<Iterable<SubscribedGadgetFeed>> contains(SubscribedGadgetFeed... feeds)
    {
        ImmutableList.Builder<Matcher<? super SubscribedGadgetFeed>> matchers = ImmutableList.builder();
        for (SubscribedGadgetFeed feed : feeds)
        {
            matchers.add(deeplyEqualTo(feed));
        }
        return containsInAnyOrder(matchers.build());
    }
}
