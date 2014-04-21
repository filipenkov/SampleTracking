package com.atlassian.upm.rest.resources;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.notification.cache.NotificationCacheUpdater;
import com.atlassian.upm.rest.UpmUriEscaper;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.test.JaxRsMatchers.accepted;
import static com.atlassian.upm.test.JaxRsMatchers.internalError;
import static com.atlassian.upm.test.JaxRsMatchers.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginResourceTest
{
    private static final String PLUGIN_KEY = "test.plugin";
    private static final String PLUGIN_KEY_ESCAPED = UpmUriEscaper.escape(PLUGIN_KEY);

    @Mock RepresentationFactory representationFactory;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock PluginLicenseRepository licenseRepository;
    @Mock NotificationCacheUpdater notificationCacheUpdater;
    @Mock AuditLogService auditLogger;

    PluginResource resource;

    @Before
    public void createResource()
    {
        resource = new PluginResource(representationFactory,
                                      pluginAccessorAndController,
                                      permissionEnforcer,
                                      licenseRepository,
                                      notificationCacheUpdater,
                                      auditLogger);
    }

    @Test
    public void verifyThatPluginControllerUninstallIsCalledIfPluginIsUninstallable()
    {
        Plugin plugin = newUninstallablePlugin(PLUGIN_KEY);
        when(pluginAccessorAndController.getPlugin(PLUGIN_KEY)).thenReturn(plugin, (Plugin) null);
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);

        resource.uninstallPlugin(PLUGIN_KEY_ESCAPED);

        verify(pluginAccessorAndController).uninstallPlugin(plugin);
    }

    @Test
    public void assertThatResponseIsOkWhenPluginIsUninstalled()
    {
        Plugin plugin = newUninstallablePlugin(PLUGIN_KEY);
        when(pluginAccessorAndController.getPlugin(PLUGIN_KEY)).thenReturn(plugin, (Plugin) null);
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);

        assertThat(resource.uninstallPlugin(PLUGIN_KEY_ESCAPED), is(ok()));
    }

    @Test
    public void assertThatResponseIsAcceptedWhenPluginUninstallRequiresRestart()
    {
        Plugin plugin = newUninstallablePlugin(PLUGIN_KEY);
        when(pluginAccessorAndController.getPlugin(PLUGIN_KEY)).thenReturn(plugin);
        when(pluginAccessorAndController.getRestartState(plugin)).thenReturn(PluginRestartState.REMOVE);
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);

        assertThat(resource.uninstallPlugin(PLUGIN_KEY_ESCAPED), is(accepted()));
    }

    @Test
    public void assertThatResponseIsInternalErrorWhenPluginIsNotUninstalledAndDoesNotRequireRestart()
    {
        Plugin plugin = newUninstallablePlugin(PLUGIN_KEY);
        when(pluginAccessorAndController.getPlugin(PLUGIN_KEY)).thenReturn(plugin);
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);

        assertThat(resource.uninstallPlugin(PLUGIN_KEY_ESCAPED), is(internalError()));
    }

    private Plugin newUninstallablePlugin(String pluginKey)
    {
        return newPlugin(pluginKey, true);
    }

    private Plugin newPlugin(String key, boolean uninstallable)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(key);
        when(plugin.isUninstallable()).thenReturn(uninstallable);
        return plugin;
    }
}
