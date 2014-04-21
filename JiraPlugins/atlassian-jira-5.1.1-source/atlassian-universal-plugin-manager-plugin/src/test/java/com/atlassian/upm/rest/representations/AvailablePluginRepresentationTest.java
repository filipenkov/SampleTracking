package com.atlassian.upm.rest.representations;

import java.net.URI;

import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.category.Category;
import com.atlassian.plugins.domain.model.plugin.Plugin;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.plugins.domain.model.plugin.PluginSystemVersion.TWO;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvailablePluginRepresentationTest
{
    private final String BASE_PAC_URL = System.getProperty("pac.website", "https://plugins.atlassian.com");
    
    @Mock Category category;
    @Mock Plugin plugin;
    @Mock PluginVersion pluginVersion;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock com.atlassian.upm.spi.Plugin installedPlugin;
    @Mock LinkBuilder linkBuilder;
    @Mock PermissionEnforcer permissionEnforcer;

    private UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());
    private AvailablePluginRepresentation pluginRepresentation;
    private static final String PLUGIN_KEY = "PLUGIN_KEY";
    private static final String UPM_PLUGIN_KEY = "UPM_PLUGIN_KEY";
    private static final String PLUGIN_VERSION_ID = "PLUGIN_VERSION_ID";
    private static final String INSTALLED_VERSION = "INSTALLED_VERSION";

    @Before
    public void setupCommon()
    {
        when(category.getName()).thenReturn("CATEGORY_NAME");
        when(plugin.getId()).thenReturn("PLUGIN_ID");
        when(pluginVersion.getBinaryUrl()).thenReturn("http://example.com");      
        when(pluginVersion.getId()).thenReturn(PLUGIN_VERSION_ID);
        when(pluginVersion.getLicense()).thenReturn(category);
        when(pluginVersion.getPlugin()).thenReturn(plugin);
        when(pluginVersion.getPluginSystemVersion()).thenReturn(TWO);
        when(permissionEnforcer.hasPermission(any(Permission.class), any(com.atlassian.upm.spi.Plugin.class))).thenReturn(true);
        when(linkBuilder.buildLinkForSelf(any(URI.class))).thenReturn(new LinkBuilder.LinksMapBuilder(permissionEnforcer));
        when(pluginAccessorAndController.getUpmPluginKey()).thenReturn(UPM_PLUGIN_KEY);
    }

    @Test
    public void assertThatRepresentationReturnsCorrectDetailsLink()
    {
        setupInstalledPlugin();
        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);
        assertThat(pluginRepresentation.getDetailsLink().toString(), is(equalTo(BASE_PAC_URL + "/plugin/details/PLUGIN_ID?versionId=" + PLUGIN_VERSION_ID)));
    }

    @Test
    public void assertThatRepresentationReturnsCorrectInstalledPluginVersion()
    {
        setupInstalledPlugin();
        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);
        assertThat(pluginRepresentation.getInstalledVersion(), is(equalTo(INSTALLED_VERSION)));
    }

    @Test
    public void assertThatNullBinaryUrlDoesNotThrowNullPointerException()
    {
        setupInstalledPlugin();
        when(pluginVersion.getBinaryUrl()).thenReturn(null);
        when(linkBuilder.buildLinkForSelf(any(URI.class))).thenReturn(new LinkBuilder.LinksMapBuilder(permissionEnforcer));

        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);

        assertThat(pluginRepresentation.getBinaryLink(), is(nullValue()));
    }

    @Test
    public void assertThatNonUpmPluginCanBeDeployable()
    {
        setupInstalledPlugin();
        when(plugin.isDeployable()).thenReturn(true);
        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);
        assertTrue(pluginRepresentation.isDeployable());
    }

    @Test
    public void assertThatNonUpmPluginCanBeNonDeployable()
    {
        setupInstalledPlugin();
        when(plugin.isDeployable()).thenReturn(false);
        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);
        assertFalse(pluginRepresentation.isDeployable());
    }
    
    @Test
    public void assertThatUpmIsAlwaysDeployable()
    {
        setupUpmPlugin();
        pluginRepresentation = new AvailablePluginRepresentation(pluginVersion, uriBuilder, linkBuilder, pluginAccessorAndController);
        assertTrue(pluginRepresentation.isDeployable());
    }
    
    private void setupInstalledPlugin()
    {
        when(plugin.getPluginKey()).thenReturn(PLUGIN_KEY);
        when(pluginAccessorAndController.isPluginInstalled(PLUGIN_KEY)).thenReturn(true);
        when(pluginAccessorAndController.getPlugin(PLUGIN_KEY)).thenReturn(installedPlugin);
        when(pluginAccessorAndController.getRestartState(installedPlugin)).thenReturn(PluginRestartState.NONE);
        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(installedPlugin.getPluginInformation()).thenReturn(pluginInformation);
        when(pluginInformation.getVersion()).thenReturn(INSTALLED_VERSION);
        when(permissionEnforcer.hasPermission(any(Permission.class), any(com.atlassian.upm.spi.Plugin.class))).thenReturn(true);
        when(linkBuilder.buildLinkForSelf(any(URI.class))).thenReturn(new LinkBuilder.LinksMapBuilder(permissionEnforcer));
    }
    
    private void setupUpmPlugin()
    {
        when(plugin.getPluginKey()).thenReturn(UPM_PLUGIN_KEY);
        when(plugin.isDeployable()).thenReturn(false);
    }
}
