package com.atlassian.upm.license.internal.host;

import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.license.internal.LicenseManagerProvider;

import com.cenqua.fisheye.AppConfig;
import com.cenqua.fisheye.config1.ConfigDocument;
import com.cenqua.fisheye.config1.LicenseType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.extras.api.Product.CRUCIBLE;
import static com.atlassian.extras.api.Product.FISHEYE;
import static com.atlassian.upm.api.util.Option.none;
import static com.google.common.base.Preconditions.checkNotNull;

public class FecruHostLicenseProvider implements HostLicenseProvider
{
    private final ConfigDocument configDoc;
    private final LicenseManagerProvider licenseManagerProvider;
    
    public FecruHostLicenseProvider(LicenseManagerProvider licenseManagerProvider)
    {
        this.configDoc = AppConfig.getsConfig().getConfigDocument();
        this.licenseManagerProvider = checkNotNull(licenseManagerProvider, "licenseManagerProvider");
    }

    public Iterable<ProductLicense> getHostApplicationLicense()
    {
        LicenseType licenseType = configDoc.getConfig().getLicense();
        String crucibleLicString = licenseType.getCrucible();
        String fisheyeLicString = licenseType.getFisheye();
        LicenseManager licenseManager = licenseManagerProvider.getLicenseManager();
        return ImmutableSet.<ProductLicense>of(licenseManager.getLicense(crucibleLicString).getProductLicense(CRUCIBLE),
                                               licenseManager.getLicense(fisheyeLicString).getProductLicense(FISHEYE));
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
