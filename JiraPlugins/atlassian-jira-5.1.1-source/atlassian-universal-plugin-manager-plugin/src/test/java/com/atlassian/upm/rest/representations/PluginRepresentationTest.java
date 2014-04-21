package com.atlassian.upm.rest.representations;

import java.net.URI;

import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.category.Category;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.permission.Permission.GET_PLUGIN_MODULES;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginRepresentationTest
{
    @Mock Category category;
    @Mock Plugin plugin;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock Plugin installedPlugin;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock Module module;
    @Mock AsynchronousTaskManager asynchronousTaskManager;
    @Mock PluginLicense license;
    @Mock RepresentationFactory representationFactory;

    private UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());
    private LinkBuilder linkBuilder;
    private PluginRepresentation pluginRepresentation;
    private static final String INSTALLED_VERSION = "INSTALLED_VERSION";
    private static final String PLUGIN_KEY = "PLUG_KEY";
    private static final String MODULE_KEY = "MOD_KEY";
    private static final String UPM_KEY = "upm-plugin-key";

    @Before
    public void createPluginRepresentation()
    {
        when(category.getName()).thenReturn("CATEGORY_NAME");
        when(pluginAccessorAndController.isPluginInstalled("PLUGIN_KEY")).thenReturn(true);
        when(pluginAccessorAndController.getPlugin("PLUGIN_KEY")).thenReturn(installedPlugin);
        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);
        when(pluginInformation.getVersion()).thenReturn(INSTALLED_VERSION);
        when(permissionEnforcer.hasPermission(any(Permission.class), any(Plugin.class))).thenReturn(true);
        when(pluginAccessorAndController.getRestartState(any(Plugin.class))).thenReturn(PluginRestartState.NONE);
        when(pluginAccessorAndController.usesLicensing(plugin)).thenReturn(true);
        when(pluginAccessorAndController.getUpmPluginKey()).thenReturn(UPM_KEY);
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(true);
        when(module.getPlugin()).thenReturn(plugin);
        when(module.getKey()).thenReturn(MODULE_KEY);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getVersion()).thenReturn("5");
        when(plugin.getModules()).thenReturn(new ImmutableList.Builder<Module>().add(module).build());
        when(plugin.getLicense()).thenReturn(none(PluginLicense.class));
        when(plugin.isStaticPlugin()).thenReturn(false);
        when(plugin.isUninstallable()).thenReturn(true);
        
        this.linkBuilder = new LinkBuilder(uriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);
    }

    @Test
    public void assertThatRepresentationHasLicenseIfPresent()
    {
        when(plugin.getLicense()).thenReturn(some(license));
        when(license.getError()).thenReturn(Option.<LicenseError>none());
        when(license.getExpiryDate()).thenReturn(Option.<DateTime>none());

        LicenseDetailsRepresentation licenseDetails = new LicenseDetailsRepresentation(true, null, false, false, -1, null,
                                                                                       LicenseType.COMMERCIAL, null, "", "", null, null, null);
        when(representationFactory.createPluginLicenseRepresentation(license)).thenReturn(licenseDetails);

        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getLicenseDetails(), is(equalTo(licenseDetails)));
    }

    @Test
    public void assertThatRepresentationHasNoLicenseIfNotPresent()
    {
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getLicenseDetails(), is(nullValue()));
    }

    @Test
    public void assertThatRepresentationHasModuleWithSelfLink()
    {
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_MODULE_ENABLEMENT, module)).thenReturn(true);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getModules().iterator().next().getSelfLink(), is(notNullValue()));
    }

    @Test
    public void assertThatRepresentationDoesNotHaveModuleWithSelfLinkWhenMissingPermission()
    {
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_MODULE_ENABLEMENT, module)).thenReturn(false);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getModules().iterator().next().getSelfLink(), is(nullValue()));
    }

    @Test
    public void assertThatRepresentationHasKey()
    {
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getModules().iterator().next().getKey(), is(notNullValue()));
    }

    @Test
    public void assertThatRepresentationHasNullModulesWhenMissingPermission()
    {
        when(permissionEnforcer.hasPermission(GET_PLUGIN_MODULES, plugin)).thenReturn(false);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getModules(), is(nullValue()));
    }

    @Test
    public void assertBadVendorUrlIsIgnored()
    {
        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);
        when(pluginInformation.getVendorName()).thenReturn("bad vendor");
        when(pluginInformation.getVendorUrl()).thenReturn("invalid url");
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getVendor().getLink(), is(nullValue()));
    }

    @Test
    public void assertGoodVendorUrlIsNotIgnored()
    {
        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);
        when(pluginInformation.getVendorName()).thenReturn("atlassian");
        when(pluginInformation.getVendorUrl()).thenReturn("http://www.atlassian.com");
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getVendor().getLink(), is(URI.create("http://www.atlassian.com")));
    }

    @Test
    public void assertThatPluginRepresentationHasPacDetailsLink()
    {
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getPacDetailsLink(), is(notNullValue()));
    }

    @Test
    public void assertThatPluginRepresentationHasPluginDetailsLink()
    {
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getPluginDetailsLink(), is(notNullValue()));
    }
    
    @Test
    public void assertThatPluginRepresentationHasUninstallLinkByDefault()
    {
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getUninstallLink(), is(notNullValue()));
    }

    @Test
    public void assertThatPluginRepresentationDoesNotHaveUninstallLinkWhenPluginIsStatic()
    {
        when(plugin.isStaticPlugin()).thenReturn(true);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getUninstallLink(), is(nullValue()));
    }
    
    @Test
    public void assertThatPluginRepresentationDoesNotHaveUninstallLinkWhenPluginIsNotUninstallable()
    {
        when(plugin.isUninstallable()).thenReturn(false);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getUninstallLink(), is(nullValue()));
    }
    
    @Test
    public void assertThatPluginRepresentationDoesNotHaveUninstallLinkWhenPluginIsUpm()
    {
        when(plugin.getKey()).thenReturn(UPM_KEY);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getUninstallLink(), is(nullValue()));
    }
    
    @Test
    public void assertThatPluginRepresentationDoesNotHaveUninstallLinkWhenPluginIsBundled()
    {
        when(pluginAccessorAndController.isUserInstalled(plugin)).thenReturn(false);
        pluginRepresentation = getRepresentation();
        assertThat(pluginRepresentation.getUninstallLink(), is(nullValue()));
    }

    private PluginRepresentation getRepresentation()
    {
        return new PluginRepresentation(pluginAccessorAndController, plugin, uriBuilder, linkBuilder, permissionEnforcer, representationFactory);
    }
}
