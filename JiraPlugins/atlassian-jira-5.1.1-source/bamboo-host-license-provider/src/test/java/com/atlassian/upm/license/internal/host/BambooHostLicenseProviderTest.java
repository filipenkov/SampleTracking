package com.atlassian.upm.license.internal.host;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.upm.license.internal.LicenseManagerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.extras.api.LicenseType.TESTING;
import static com.atlassian.extras.api.Product.BAMBOO;
import static com.atlassian.upm.license.internal.host.BambooHostLicenseProvider.LICENSE_HASH;
import static com.atlassian.upm.license.internal.host.BambooHostLicenseProvider.LICENSE_MESSAGE;
import static com.atlassian.upm.license.internal.host.BambooHostLicenseProvider.LICENSE_STRING;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BambooHostLicenseProviderTest
{
    AtlassianBootstrapManager bootstrapManager = Mockito.mock(AtlassianBootstrapManager.class);
    LicenseManagerProvider licenseManagerProvider = Mockito.mock(LicenseManagerProvider.class);
    @Mock LicenseManager licenseManager;
    @Mock AtlassianLicense atlLicense;
    @Mock com.atlassian.extras.api.ProductLicense extrasProductLicense;
    @Mock ProductLicense productLicense;
    BambooHostLicenseProvider test;
    
    @Before
    public void setUp()
    {
        when(bootstrapManager.getProperty(anyString())).thenReturn(null);
        when(licenseManagerProvider.getLicenseManager()).thenReturn(licenseManager);
        when(licenseManager.getLicense(anyString())).thenReturn(atlLicense);
        when(atlLicense.getProductLicense(BAMBOO)).thenReturn(extrasProductLicense);
        when(extrasProductLicense.getLicenseType()).thenReturn(TESTING);
        when(extrasProductLicense.getProduct()).thenReturn(BAMBOO);
        test = new BambooHostLicenseProvider(bootstrapManager, licenseManagerProvider);
    }

    @Test
    public void testGetHostApplicationLicenseWithFullLicenseString()
    {
        assertNotNull(licenseManagerProvider);
        when(bootstrapManager.getProperty(LICENSE_HASH)).thenReturn("abcdef");
        when(bootstrapManager.getProperty(LICENSE_MESSAGE)).thenReturn("abcdef");
        Iterable<? extends ProductLicense> result = test.getHostApplicationLicense();
        assertTrue("license returned", result.iterator().hasNext());
    }

    @Test
    public void testGetHostApplicationLicenseWithoutFullLicenseString()
    {
        assertNotNull(bootstrapManager);
        when(bootstrapManager.getProperty(LICENSE_STRING)).thenReturn("abcdef");
        Iterable<? extends ProductLicense> result = test.getHostApplicationLicense();
        assertTrue("license returned", result.iterator().hasNext());
    }

}
