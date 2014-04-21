package com.atlassian.upm.rest.representations;

import java.util.Iterator;
import java.util.Locale;

import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.rest.UpmUriEscaper.escape;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class InstalledPluginCollectionRepresentationTest
{
    @Mock AsynchronousTaskManager asynchronousTaskManager;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock Plugin plugin1;
    @Mock Plugin plugin2;
    @Mock Plugin plugin3;
    @Mock PluginInformation pluginInformation;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock PacClient pacClient;
    
    private UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());
    private InstalledPluginCollectionRepresentation pluginCollectionRepresentation;
    private LinkBuilder linkBuilder;

    @Before
    public void setUp() throws Exception
    {
        when(pluginInformation.getDescription()).thenReturn("a desc");
        when(plugin1.getName()).thenReturn("Z plugin");
        when(plugin1.getKey()).thenReturn("z.key");
        when(plugin1.getPluginInformation()).thenReturn(pluginInformation);
        when(plugin1.isUpdateAvailable()).thenReturn(Option.none(Boolean.class));
        when(plugin2.getName()).thenReturn("A plugin");
        when(plugin2.getKey()).thenReturn("a.key");
        when(plugin2.getPluginInformation()).thenReturn(pluginInformation);
        when(plugin2.isUpdateAvailable()).thenReturn(Option.none(Boolean.class));
        // This plugin has the same name and differentiates on key
        when(plugin3.getName()).thenReturn("Z plugin");
        when(plugin3.getKey()).thenReturn("y.key");
        when(plugin3.getPluginInformation()).thenReturn(pluginInformation);
        when(plugin3.isUpdateAvailable()).thenReturn(Option.none(Boolean.class));
        when(pluginAccessorAndController.isSafeMode()).thenReturn(false);
        when(pluginAccessorAndController.getRestartState(plugin1)).thenReturn(PluginRestartState.NONE);
        when(pluginAccessorAndController.getRestartState(plugin2)).thenReturn(PluginRestartState.NONE);
        when(pluginAccessorAndController.getRestartState(plugin3)).thenReturn(PluginRestartState.NONE);
        when(pacClient.getUpdates()).thenReturn(ImmutableList.<PluginVersion>of());
        linkBuilder = new LinkBuilder(uriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);

        Iterable<Plugin> plugins = ImmutableList.of(plugin1, plugin2, plugin3);
        
        this.pluginCollectionRepresentation = new InstalledPluginCollectionRepresentation(pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer, Locale.ENGLISH, plugins, null, "");
    }

    @Test
    public void assertThatRepresentationReturnsPluginsSortedByNameAndKey()
    {
        final Iterator<InstalledPluginEntry> pluginIterator = pluginCollectionRepresentation.getPlugins().iterator();
        assertTrue(pluginIterator.next().getSelfLink().toString().endsWith(escape("a.key")));
        assertTrue(pluginIterator.next().getSelfLink().toString().endsWith(escape("y.key")));
        assertTrue(pluginIterator.next().getSelfLink().toString().endsWith(escape("z.key")));
    }
}
