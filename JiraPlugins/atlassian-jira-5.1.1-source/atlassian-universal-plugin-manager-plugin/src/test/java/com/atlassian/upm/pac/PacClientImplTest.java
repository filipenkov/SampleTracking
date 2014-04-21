package com.atlassian.upm.pac;

import java.util.Map;

import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugins.domain.model.plugin.Plugin;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.plugins.service.plugin.PluginVersionService;
import com.atlassian.plugins.service.product.ProductService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.Sys;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PacClientImplTest
{
    @Mock ApplicationProperties applicationProperties;
    @Mock PluginAccessorAndController manager;
    @Mock PacServiceFactory factory;
    @Mock PluginVersionService pluginVersionService;
    @Mock ProductService productService;
    @Mock PackageAccessor packageAccessor;
    @Mock Product product;
    @Mock Version version;
    @Mock com.atlassian.upm.osgi.Package pkg;

    PacClient client;

    @Before
    public void createApplicationPropertiesStubs()
    {
        when(applicationProperties.getDisplayName()).thenReturn("testing");
        when(applicationProperties.getBuildNumber()).thenReturn("1");
        when(pkg.getVersion()).thenReturn(version);
        when(packageAccessor.getExportedPackages(0L, "com.atlassian.testing")).thenReturn(ImmutableList.of(pkg));
    }

    @Before
    public void createPacServiceFactoryStubs()
    {
        when(factory.getPluginVersionService()).thenReturn(pluginVersionService);
        when(factory.getProductService()).thenReturn(productService);
        when(productService.getLatestProductVersion(anyString())).thenReturn(product);
        when(product.getBuildNumber()).thenReturn(1L);
    }

    @Before
    public void createPluginVersionServiceStubs()
    {
        when(
            pluginVersionService.findCompatiblePluginVersions(
                eq("testing"), eq(1L), anyString(), (Boolean) isNull(), (Integer) isNull(), (Integer) isNull(), eq(ImmutableList.of("plugin", "plugin.tinyicon")))
        ).thenReturn(ImmutableList.of(newPluginVersion("test.plugin"), newPluginVersion("other.plugin"), newPluginVersion("another.plugin")));
    }

    @Before
    public void createPacClient()
    {
        client = new PacClientImpl(applicationProperties, manager, factory, packageAccessor);
    }

    @Before
    @After
    public void resetOfflineMode()
    {
        System.clearProperty(Sys.UPM_PAC_DISABLE);
    }

    /**
     * Sets the UPM mode. It can be offline or online.
     * @param systemPropertyValue the mode in the System Property. If null, the system property is not set. Boolean.TRUE sets to "Disabled"
     */
    private void setUpmMode(Boolean systemPropertyValue)
    {
        if (systemPropertyValue != null)
        {
            System.setProperty(Sys.UPM_PAC_DISABLE, Boolean.toString(systemPropertyValue));
        }
    }

    /**
     * Sets UPM offline, assuming resetOfflineMode() was called before.
     */
    private void setUpmOfflineMode()
    {
        setUpmMode(Boolean.TRUE);
    }

    /**
     * Tests PAC is enabled by default
     */
    @Test
    public void pacIsOnlineByDefault()
    {
        assertTrue(!client.isPacDisabled());
    }

    /**
     * Tests PAC is enabled if the system property sets "disabled=false"
     */
    @Test
    public void systemPropertySetsOnlineMode()
    {
        setUpmMode(Boolean.FALSE);
        assertTrue(!client.isPacDisabled());
    }

    /**
     * Tests PAC is disabled if the system property sets "disabled=true"
     */
    @Test
    public void systemPropertySetsOfflineMode()
    {
        setUpmMode(Boolean.TRUE);
        assertTrue(client.isPacDisabled());
    }

    @Test
    public void assertThatNoFilteringOfAvailablePluginsIsDoneWhenNoPluginsAreInstalled()
    {
        assertThat(client.getAvailable(null, null, null), containsEntries("test.plugin", "other.plugin", "another.plugin" ));
    }

    @Test
    public void assertThatInstalledPluginsAreFilteredFromAvailablePlugins()
    {
        when(manager.isPluginInstalled("other.plugin")).thenReturn(true);
        assertThat(client.getAvailable(null, null, null), containsEntries("test.plugin", "another.plugin"));
    }

    @Test
    public void assertThatAvailablePluginsAreLimitedWhenMaxResultsIsSpecified()
    {
        assertThat(client.getAvailable(null, 2, null), containsEntries("test.plugin", "other.plugin"));
    }

    @Test
    public void assertThatInstalledPluginsAreFilteredFromAvailablePluginsBeforeItIsFilteredByMaxResults()
    {
        when(manager.isPluginInstalled("other.plugin")).thenReturn(true);
        assertThat(client.getAvailable(null, 2, null), containsEntries( "test.plugin", "another.plugin"));
    }

    @Test
    public void assertThatAvailablePluginsReturnedStartsAtSpecifiedOffset()
    {
        assertThat(client.getAvailable(null, null, 1), containsEntries("other.plugin", "another.plugin"));
    }

    @Test
    public void assertThatInstalledPluginsAreFilteredFromAvailablePluginsBeforeItIsFilteredByOffset()
    {
        when(manager.isPluginInstalled("other.plugin")).thenReturn(true);
        assertThat(client.getAvailable(null, null, 1), containsEntries("another.plugin"));
    }

    @Test
    public void assertThatGetPluginByKeyReturnsPluginIfPluginKeyExists()
    {
        PluginVersion plugin = mock(PluginVersion.class);
        when(plugin.getBuildNumber()).thenReturn(1L);
        when(
            pluginVersionService.findAllCompatiblePluginVersionsByPluginKey(
                "testing", 1L, "test.plugin", null, null, ImmutableList.of("plugin.icon", "plugin.vendor", "license", "reviewSummary"))
        ).thenReturn(ImmutableList.of(plugin));
        assertThat(client.getAvailablePlugin("test.plugin"), is(equalTo(plugin)));
    }

    @Test
    public void assertThatGetPluginByKeyReturnsNullIfPluginKeyDoesNotExist()
    {
        when(
            pluginVersionService.findAllCompatiblePluginVersionsByPluginKey(
                "testing", 1L, "i.do.not.exist", null, null, ImmutableList.of("plugin.icon", "plugin.vendor", "license", "reviewSummary"))
        ).thenReturn(ImmutableList.<PluginVersion>of());
        assertNull(client.getAvailablePlugin("i.do.not.exist"));
    }

    @Test
    public void assertThatGetPluginByKeyReturnsLatestPluginIfMultipleCompatibleVersionsExist()
    {
        PluginVersion olderVersion = mock(PluginVersion.class);
        PluginVersion latestVersion = mock(PluginVersion.class);
        when(olderVersion.getBuildNumber()).thenReturn(1L);
        when(latestVersion.getBuildNumber()).thenReturn(2L);
        when(
            pluginVersionService.findAllCompatiblePluginVersionsByPluginKey(
                "testing", 1L, "test.plugin", null, null, ImmutableList.of("plugin.icon", "plugin.vendor", "license", "reviewSummary"))
        ).thenReturn(ImmutableList.of(olderVersion, latestVersion));

        assertThat(client.getAvailablePlugin("test.plugin"), is(equalTo(latestVersion)));
    }

    @Test
    public void assertThatGetUpdatesReturnsNewVersionsOfAllInstalledNonSystemPlugins()
    {
        Iterable<com.atlassian.upm.spi.Plugin> plugins = ImmutableList.of(
            newUserInstalledPlugin("user.installed.plugin", "1.0"), newSystemInstalledPlugin("system.plugin", "1.0")
        );
        when(manager.getPlugins()).thenReturn(plugins);
        PluginVersion userInstalledPluginVersion = mock(PluginVersion.class);
        Plugin userInstalledPluginVersionPlugin = mock(Plugin.class);
        when(userInstalledPluginVersion.getPlugin()).thenReturn(userInstalledPluginVersionPlugin);
        when(userInstalledPluginVersionPlugin.getPluginKey()).thenReturn("user.installed.plugin");
        when(userInstalledPluginVersion.getVersion()).thenReturn("dummy version");
        when(pluginVersionService.findUpdates(
            "testing", 1L, ImmutableMap.of("user.installed.plugin", "1.0"), null, null, null, ImmutableList.of("plugin")
        )).thenReturn(ImmutableList.of(userInstalledPluginVersion));

        assertThat(client.getUpdates(), contains(userInstalledPluginVersion));
    }

    @Test
    public void verifyThatInstalledPluginsWaitingForRestartAreNotCheckedForUpdates()
    {
        com.atlassian.upm.spi.Plugin normalPlugin = newUserInstalledPlugin("installed.plugin", "1.0");
        com.atlassian.upm.spi.Plugin pluginRequiringRestart = newUserInstalledPlugin("requires.restart", "1.0");

        when(manager.getPlugins()).thenReturn(ImmutableList.of(
            normalPlugin,
            pluginRequiringRestart
        ));
        when(manager.requiresRestart(pluginRequiringRestart)).thenReturn(true);

        client.getUpdates();

        Map<String, String> installedPluginsNotRequiringRestart = ImmutableMap.of("installed.plugin", "1.0");

        verify(pluginVersionService).findUpdates(
            anyString(),
            anyLong(),
            eq(installedPluginsNotRequiringRestart),
            (Boolean) isNull(),
            (Integer) isNull(),
            (Integer) isNull(),
            eq(ImmutableList.of("plugin"))
        );
    }

    @Test
    public void verifyThatGetAvailableDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getAvailable(null, null, null), is(Matchers.<PluginVersion>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetFeaturedDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getFeatured(null, null), is(Matchers.<PluginVersion>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetPopularDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getPopular(null, null), is(Matchers.<PluginVersion>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetSupportedDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getSupported(null, null), is(Matchers.<PluginVersion>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetUpdatesDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getUpdates(), is(Matchers.<PluginVersion>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetAvailablePluginDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        verifyZeroInteractions(factory);
        assertThat(client.getAvailablePlugin("foo"), is(nullValue()));
    }

    @Test
    public void verifyThatGetAvailablePluginVersionDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getAvailablePlugin("key"), is(nullValue()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetProductUpdatePluginCompatibilityDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        ProductUpdatePluginCompatibility compat = client.getProductUpdatePluginCompatibility(1L);
        assertThat(compat.getCompatible(), is(Matchers.<com.atlassian.upm.spi.Plugin>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void verifyThatGetProductUpdatesDoesNothingInOfflineMode()
    {
        setUpmOfflineMode();

        assertThat(client.getProductUpdates(), is(Matchers.<Product>emptyIterable()));
        verifyZeroInteractions(factory);
    }

    @Test
    public void assertThatProductVersionIsNotDevelopmentVersionIfQualifierIsBlank()
    {
        when(version.getQualifier()).thenReturn("");
        assertFalse(client.isDevelopmentProductVersion().get());
    }

    @Test
    public void assertThatProductVersionIsNotDevelopmentVersionIfQualifierIsNull()
    {
        when(version.getQualifier()).thenReturn(null);
        assertFalse(client.isDevelopmentProductVersion().get());
    }

    @Test
    public void assertThatProductVersionIsDevelopmentVersionIfQualifierIsMilestone()
    {
        when(version.getQualifier()).thenReturn("m5");
        assertTrue(client.isDevelopmentProductVersion().get());
    }

    @Test
    public void assertThatProductVersionIsDevelopmentVersionIfQualifierIsSnapshot()
    {
        when(version.getQualifier()).thenReturn("SNAPSHOT");
        assertTrue(client.isDevelopmentProductVersion().get());
    }

    private com.atlassian.upm.spi.Plugin newUserInstalledPlugin(String key, String version)
    {
        PluginInformation info = mock(PluginInformation.class);
        when(info.getVersion()).thenReturn(version);

        com.atlassian.upm.spi.Plugin plugin = mock(com.atlassian.upm.spi.Plugin.class);
        when(plugin.getKey()).thenReturn(key);
        when(plugin.getPluginInformation()).thenReturn(info);
        when(manager.isUserInstalled(plugin)).thenReturn(true);

        return plugin;
    }

    private com.atlassian.upm.spi.Plugin newSystemInstalledPlugin(String key, String version)
    {
        PluginInformation info = mock(PluginInformation.class);
        when(info.getVersion()).thenReturn(version);

        com.atlassian.upm.spi.Plugin plugin = mock(com.atlassian.upm.spi.Plugin.class);
        when(plugin.getKey()).thenReturn(key);
        when(plugin.getPluginInformation()).thenReturn(info);
        when(manager.isUserInstalled(plugin)).thenReturn(false);

        return plugin;
    }

    private Matcher<Iterable<PluginVersion>> containsEntries(String firstKey, String... additionalKeys)
    {
        ImmutableList.Builder<Matcher<? super PluginVersion>> matchers = ImmutableList.builder();
        matchers.add(entry(firstKey));
        for (String key : additionalKeys)
        {
            matchers.add(entry(key));
        }
        return contains(matchers.build());
    }

    private Matcher<PluginVersion> entry(final String key)
    {
        return new TypeSafeDiagnosingMatcher<PluginVersion>()
        {
            @Override
            protected boolean matchesSafely(PluginVersion plugin, Description mismatchDescription)
            {
                if (!plugin.getPlugin().getPluginKey().equals(key))
                {
                    mismatchDescription.appendText("was ").appendValue(plugin.getPlugin().getPluginKey());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendValue(key);
            }
        };
    }

    private PluginVersion newPluginVersion(String key)
    {
        Plugin plugin = new Plugin();
        plugin.setPluginKey(key);
        PluginVersion version = new PluginVersion();
        version.setPlugin(plugin);
        return version;
    }
}
