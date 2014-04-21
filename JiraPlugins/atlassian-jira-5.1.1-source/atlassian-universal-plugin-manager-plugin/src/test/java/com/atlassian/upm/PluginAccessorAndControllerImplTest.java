package com.atlassian.upm;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacAuditClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginAccessorAndControllerImplTest
{
    private static final String PLUGIN_KEY = "some-plugin-key";
    private static final String PLUGIN_KEY2 = "another-plugin-key";
    private static final String LICENSE_STORAGE_PLUGIN_KEY = "com.atlassian.upm.plugin-license-storage-plugin";

    @Mock private ApplicationProperties applicationProperties;
    @Mock private PluginAccessor pluginAccessor;
    @Mock private PluginController pluginController;
    @Mock private AuditLogService auditLogger;
    @Mock private ConfigurationStore configurationStore;
    @Mock private TransactionTemplate txTemplate;
    @Mock private PluginFactory pluginFactory;
    @Mock private PluginMetadataManager pluginMetadataManager;
    @Mock private HostLicenseProvider hostLicenseProvider;
    @Mock private PacAuditClient pacAuditClient;
    @Mock private com.atlassian.upm.spi.Plugin plugin;
    @Mock private Plugin plugPlugin;
    @Mock private com.atlassian.upm.spi.Plugin plugin2;
    @Mock private Plugin plugPlugin2;

    private PluginAccessorAndController pluginAccessorAndController;

    @Before
    public void setup()
    {
        when(plugin.getPlugin()).thenReturn(plugPlugin);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugPlugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin2.getPlugin()).thenReturn(plugPlugin2);
        when(plugin2.getKey()).thenReturn(PLUGIN_KEY2);
        when(plugPlugin2.getKey()).thenReturn(PLUGIN_KEY2);

        when(pluginMetadataManager.isUserInstalled(plugPlugin)).thenReturn(false);

        pluginAccessorAndController = new PluginAccessorAndControllerImpl(applicationProperties,
                                                                          pluginAccessor,
                                                                          pluginController,
                                                                          auditLogger,
                                                                          configurationStore,
                                                                          txTemplate,
                                                                          pluginFactory,
                                                                          pluginMetadataManager,
                                                                          hostLicenseProvider,
                                                                          pacAuditClient);
    }

    @Test
    public void assertThatActualUserInstalledPluginIsMarkedAsUserInstalledByDefault()
    {
        when(pluginMetadataManager.isUserInstalled(plugPlugin)).thenReturn(true);
        assertTrue(pluginAccessorAndController.isUserInstalled(plugin));
    }

    @Test
    public void assertThatNonUserInstalledPluginIsNotMarkedAsUserInstalledByDefault()
    {
        assertFalse(pluginAccessorAndController.isUserInstalled(plugin));
    }

    @Test
    public void assertThatUserInstalledPluginsOverrideDoesNotWorkWhenBehindTheFirewall()
    {
        try
        {
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, PLUGIN_KEY);
            assertFalse(pluginAccessorAndController.isUserInstalled(plugin));
        }
        finally
        {
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, "");
        }
    }

    @Test
    public void assertThatUserInstalledPluginsOverrideWorksWhenOnDemand()
    {
        try
        {
            System.setProperty(Sys.UPM_ON_DEMAND, "true");
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, PLUGIN_KEY);
            assertTrue(pluginAccessorAndController.isUserInstalled(plugin));
        }
        finally
        {
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, "");
            System.setProperty(Sys.UPM_ON_DEMAND, "false");
        }
    }

    @Test
    public void assertThatUserInstalledPluginsOverrideIsCommaSeparated()
    {
        try
        {
            System.setProperty(Sys.UPM_ON_DEMAND, "true");
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, PLUGIN_KEY + "," + PLUGIN_KEY2);
            assertTrue(pluginAccessorAndController.isUserInstalled(plugin));
            assertTrue(pluginAccessorAndController.isUserInstalled(plugin2));
        }
        finally
        {
            System.setProperty(Sys.UPM_USER_INSTALLED_OVERRIDE, "");
            System.setProperty(Sys.UPM_ON_DEMAND, "false");
        }
    }

    @Test
    public void assertThatLicenseStoragePluginIsMarkedAsASystemPlugin()
    {
        when(plugin.getKey()).thenReturn(LICENSE_STORAGE_PLUGIN_KEY);
        assertFalse(pluginAccessorAndController.isUserInstalled(plugin));
    }
}
