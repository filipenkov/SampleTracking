package com.atlassian.upm.license.internal.host;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.license.LicenseException;
import com.atlassian.license.LicensePair;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.license.internal.LicenseManagerProvider;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.config.util.BootstrapUtils.getBootstrapManager;
import static com.atlassian.extras.api.Product.BAMBOO;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;

public class BambooHostLicenseProvider implements HostLicenseProvider
{
    private static final Logger logger = LoggerFactory.getLogger(BambooHostLicenseProvider.class);
    
    static final String LICENSE_HASH = "license.hash";
    static final String LICENSE_MESSAGE = "license.message";
    static final String LICENSE_STRING = "license.string";

    private final AtlassianBootstrapManager bootstrapManager;
    private final LicenseManagerProvider licenseManagerProvider;

    public BambooHostLicenseProvider(LicenseManagerProvider licenseManagerProvider)
    {
        this(getBootstrapManager(), licenseManagerProvider);
    }
    
    BambooHostLicenseProvider(AtlassianBootstrapManager bootstrapManager,
                                     LicenseManagerProvider licenseManagerProvider)
    {
        this.bootstrapManager = bootstrapManager;
        this.licenseManagerProvider = licenseManagerProvider;
    }

    public Iterable<ProductLicense> getHostApplicationLicense()
    {
        
        String licenseString = (String) bootstrapManager.getProperty(LICENSE_STRING);
        if (licenseString == null)
        {
            String message = (String) bootstrapManager.getProperty(LICENSE_MESSAGE);
            String hash = (String) bootstrapManager.getProperty(LICENSE_HASH);
            try 
            {
                LicensePair licPair = new LicensePair(message, hash);
                licenseString = licPair.getOriginalLicenseString();
            }
            catch (LicenseException e)
            {
                logger.error("Error getting product license", e);
                return none();
            }
        }
        AtlassianLicense license = licenseManagerProvider.getLicenseManager().getLicense(licenseString);
        return option(license.getProductLicense(BAMBOO));
    }

    @Override
    public Iterable<String> getSupportedLegacyPluginKeys()
    {
        return ImmutableList.of();
    }
    
    @Override
    public Option<Pair<ProductLicense, String>> getPluginLicenseDetails(String pluginKey)
    {
        return none();
    }
    
    @Override
    public Option<String> getPluginLicenseAdminUriPath(String pluginKey)
    {
        return none();
    }
}
