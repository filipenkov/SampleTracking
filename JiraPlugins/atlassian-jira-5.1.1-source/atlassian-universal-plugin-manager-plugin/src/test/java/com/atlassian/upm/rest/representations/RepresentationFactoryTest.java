package com.atlassian.upm.rest.representations;

import java.util.Locale;

import com.atlassian.extras.api.Organisation;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.Organization;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.license.internal.LicenseDateFormatter;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.osgi.BundleAccessor;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.ServiceAccessor;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.permission.PermissionService;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationFactoryTest
{
    private static final String SEN = "SENSENSEN";
    private static final Integer MAX_USERS = 5;
    private static final String HOST_CONTACT_EMAIL = "hostcontact@example.com";
    private static final String HOST_ORGANIZATION = "SMERSH";
    private static final String PLUGIN_CONTACT_EMAIL = "plugincontact@example.com";
    private static final String PLUGIN_ORGANIZATION = "KAOS";
    
    @Mock HostLicenseProvider hostLicenseProvider;
    @Mock ProductLicense hostLicense;
    @Mock PluginLicense pluginLicense;
    @Mock Organisation hostOrganization;
    @Mock Organization licenseOrganization;
    @Mock com.atlassian.extras.api.Contact hostContact;
    @Mock Contact pluginContact;
    @Mock LicenseDateFormatter licenseDateFormatter;

    @Mock ApplicationProperties applicationProperties;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    UpmUriBuilder upmUriBuilder;
    @Mock AsynchronousTaskManager asynchronousTaskManager;
    @Mock PermissionEnforcer permissionEnforcer;
    LinkBuilder linkBuilder;
    @Mock PacClient pacClient;
    @Mock BundleAccessor bundleAccessor;
    @Mock ServiceAccessor serviceAccessor;
    @Mock PackageAccessor packageAccessor;
    @Mock UserManager userManager;
    @Mock PermissionService permissionService;
    @Mock PluginLicenseRepository pluginLicenseRepository;
    
    private RepresentationFactory factory;
    
    @Before
    public void setupLicenses()
    {
        when(pluginLicense.getExpiryDate()).thenReturn(some(new DateTime().plusDays(1)));
        when(pluginLicense.isValid()).thenReturn(true);
        when(pluginLicense.getError()).thenReturn(none(LicenseError.class));
        when(pluginLicense.getMaximumNumberOfUsers()).thenReturn(some(MAX_USERS));
        when(pluginLicense.getMaintenanceExpiryDate()).thenReturn(some(new DateTime().plusDays(1)));
        when(pluginLicense.isEvaluation()).thenReturn(true);
        when(pluginLicense.getLicenseType()).thenReturn(LicenseType.DEVELOPER);
        when(pluginLicense.getSupportEntitlementNumber()).thenReturn(some(SEN));
        when(licenseOrganization.getName()).thenReturn(PLUGIN_ORGANIZATION);
        when(pluginLicense.getOrganization()).thenReturn(licenseOrganization);
        when(pluginContact.getEmail()).thenReturn(PLUGIN_CONTACT_EMAIL);
        when(pluginLicense.getContacts()).thenReturn(ImmutableList.of(pluginContact));

        when(hostLicense.getMaximumNumberOfUsers()).thenReturn(MAX_USERS);
        when(hostLicense.getSupportEntitlementNumber()).thenReturn(SEN);
        when(hostLicense.getOrganisation()).thenReturn(hostOrganization);
        when(hostContact.getEmail()).thenReturn(HOST_CONTACT_EMAIL);
        when(hostOrganization.getName()).thenReturn(HOST_ORGANIZATION);
        when(hostLicense.getContacts()).thenReturn(ImmutableList.of(hostContact));
        when(hostLicenseProvider.getHostApplicationLicense()).thenReturn(ImmutableList.of(hostLicense));

        when(licenseDateFormatter.format(any(DateTime.class))).thenReturn("tomorrow");

        when(applicationProperties.getBaseUrl()).thenReturn("http://test");
        upmUriBuilder = new UpmUriBuilder(applicationProperties);
        linkBuilder = new LinkBuilder(upmUriBuilder, pluginAccessorAndController, asynchronousTaskManager, permissionEnforcer);
        when(pluginAccessorAndController.getPlugins(any(Iterable.class))).thenReturn(ImmutableList.<Plugin>of());
        
        // unfortunately we have to mock/fake up a whole lot of things for RepresentationFactoryImpl, even though
        // many of them will not be used in these tests
        factory = new RepresentationFactoryImpl(pluginAccessorAndController, upmUriBuilder, linkBuilder, pacClient,
                                                bundleAccessor, serviceAccessor, packageAccessor, permissionEnforcer,
                                                applicationProperties, licenseDateFormatter, hostLicenseProvider, pluginLicenseRepository);
    }

    @Test
    public void assertThatInstalledPluginsRepresentationHasHostLicense()
    {
        InstalledPluginCollectionRepresentation installedPlugins = factory.createInstalledPluginCollectionRepresentation(Locale.ENGLISH, ImmutableList.<Plugin>of(), false, "");
        assertThat(installedPlugins.getHostStatus().getHostLicense(), is(notNullValue()));
    }
    
    @Test
    public void assertThatInstallablePluginsRepresentationHasHostLicense()
    {
        AvailablePluginCollectionRepresentation availablePlugins = factory.createInstallablePluginCollectionRepresentation(ImmutableList.<PluginVersion>of(), false);
        assertThat(availablePlugins.getHostStatus().getHostLicense(), is(notNullValue()));
    }

    @Test
    public void assertThatPopularPluginsRepresentationHasHostLicense()
    {
        PopularPluginCollectionRepresentation popularPlugins = factory.createPopularPluginCollectionRepresentation(ImmutableList.<PluginVersion>of(), false);
        assertThat(popularPlugins.getHostStatus().getHostLicense(), is(notNullValue()));
    }

    @Test
    public void assertThatSupportedPluginsRepresentationHasHostLicense()
    {
        SupportedPluginCollectionRepresentation supportedPlugins = factory.createSupportedPluginCollectionRepresentation(ImmutableList.<PluginVersion>of(), false);
        assertThat(supportedPlugins.getHostStatus().getHostLicense(), is(notNullValue()));
    }

    @Test
    public void assertThatFeaturedPluginsRepresentationHasHostLicense()
    {
        FeaturedPluginCollectionRepresentation featuredPlugins = factory.createFeaturedPluginCollectionRepresentation(ImmutableList.<PluginVersion>of(), false);
        assertThat(featuredPlugins.getHostStatus().getHostLicense(), is(notNullValue()));
    }
    
    @Test
    public void assertThatPluginLicenseRepresentationCanBeCreatedWithNoMaintenanceExpiryDate()
    {
        when(pluginLicense.getMaintenanceExpiryDate()).thenReturn(none(DateTime.class));
        assertThat(factory.createPluginLicenseRepresentation(pluginLicense), is(notNullValue()));
    }

    @Test
    public void assertThatPluginLicenseRepresentationCanBeCreatedWithNoExpiryDate()
    {
        when(pluginLicense.getExpiryDate()).thenReturn(none(DateTime.class));
        assertThat(factory.createPluginLicenseRepresentation(pluginLicense), is(notNullValue()));
    }

    @Test
    public void assertThatPluginLicenseRepresentationHasLicenseValidity()
    {
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertTrue(licenseDetails.isValid());
    }

    @Test
    public void assertThatPluginLicenseRepresentationHasError()
    {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.getError()).thenReturn(some(LicenseError.USER_MISMATCH));
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertThat(licenseDetails.getError(), equalTo(LicenseError.USER_MISMATCH));
    }
    
    @Test
    public void assertThatPluginLicenseRepresentationHasMaximumNumberOfUsers()
    {
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertThat(licenseDetails.getMaximumNumberOfUsers(), is(equalTo(MAX_USERS)));
    }

    @Test
    public void assertThatPluginLicenseRepresentationCanBeCreatedWithUnlimitedUserLicense()
    {
        when(pluginLicense.getMaximumNumberOfUsers()).thenReturn(none(Integer.class));
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertThat(licenseDetails.getMaximumNumberOfUsers(), is(nullValue()));
    }

    @Test
    public void assertThatPluginLicenseMaintenanceExpiryDateStringIsFormatted()
    {
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertThat(licenseDetails.getMaintenanceExpiryDateString(), is(equalTo("tomorrow")));
    }

    @Test
    public void assertThatPluginLicenseContactIsUsedWhenPresent()
    {
        LicenseDetailsRepresentation licenseDetails = factory.createPluginLicenseRepresentation(pluginLicense);
        assertThat(licenseDetails.getContactEmail(), is(equalTo("plugincontact@example.com")));
    }
    
    @Test
    public void assertThatHostLicenseRepresentationHasSupportEntitlementNumber()
    {
        assertThat(factory.createHostLicenseRepresentation().getSupportEntitlementNumber(), is(equalTo(SEN)));
    }

    @Test
    public void assertThatHostLicenseRepresentationHasMaximumNumberOfUsers()
    {
        assertThat(factory.createHostLicenseRepresentation().getMaximumNumberOfUsers(), is(equalTo(MAX_USERS)));
    }

    @Test
    public void assertThatHostLicenseRepresentationHasOrganizationName()
    {
        assertThat(factory.createHostLicenseRepresentation().getOrganizationName(), is(equalTo(HOST_ORGANIZATION)));
    }

    @Test
    public void assertThatHostLicenseRepresentationHasContact()
    {
        assertThat(factory.createHostLicenseRepresentation().getContactEmail(), is(equalTo(HOST_CONTACT_EMAIL)));
    }
}
