package com.atlassian.upm.license.internal.host;

import java.util.Map;

import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.license.internal.LicenseManagerProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import static com.atlassian.extras.api.Product.BONFIRE;
import static com.atlassian.extras.api.Product.GREENHOPPER;
import static com.atlassian.extras.api.Product.JIRA;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.api.util.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

public class JiraHostLicenseProvider implements HostLicenseProvider
{
    private static final String GREENHOPPER_PLUGIN_KEY = "com.pyxis.greenhopper.jira";
    private static final String GREENHOPPER_ADMIN_URI_PATH = "/secure/GHLicense.jspa?decorator=admin";
    private static final String BONFIRE_PLUGIN_KEY = "com.atlassian.bonfire.plugin";
    private static final String BONFIRE_ADMIN_URI_PATH = "/secure/BonfireAdmin.jspa?decorator=admin";
    
    private final JiraLicenseManager jiraLicenseManager;
    private final LicenseManagerProvider licenseManagerProvider;

    public JiraHostLicenseProvider(JiraLicenseManager jiraLicenseManager, LicenseManagerProvider licenseManagerProvider)
    {
        this.jiraLicenseManager = checkNotNull(jiraLicenseManager, "jiraLicenseManager");
        this.licenseManagerProvider = checkNotNull(licenseManagerProvider, "licenseManagerProvider");
    }
    
    public Iterable<ProductLicense> getHostApplicationLicense()
    {
        String licenseString = jiraLicenseManager.getLicense().getLicenseString();
        return option(licenseManagerProvider.getLicenseManager().getLicense(licenseString).getProductLicense(JIRA));
    }

    @Override
    public Iterable<String> getSupportedLegacyPluginKeys()
    {
        return ImmutableList.of(GREENHOPPER_PLUGIN_KEY, BONFIRE_PLUGIN_KEY);
    }
    
    @Override
    public Option<Pair<ProductLicense, String>> getPluginLicenseDetails(String pluginKey)
    {
        if (pluginKey.equals(GREENHOPPER_PLUGIN_KEY))
        {
            return getGreenHopperLicense();
        }
        else if (pluginKey.equals(BONFIRE_PLUGIN_KEY))
        {
            return getBonfireLicense();
        }
        return none();
    }
    
    @Override
    public Option<String> getPluginLicenseAdminUriPath(String pluginKey)
    {
        if (pluginKey.equals(GREENHOPPER_PLUGIN_KEY))
        {
            return some(GREENHOPPER_ADMIN_URI_PATH);
        }
        else if (pluginKey.equals(BONFIRE_PLUGIN_KEY))
        {
            return some(BONFIRE_ADMIN_URI_PATH);
        }
        return none();
    }
    
    private Option<Pair<ProductLicense, String>> getGreenHopperLicense()
    {
        Option<Pair<ProductLicense, String>> ret = extractLicense(jiraLicenseManager.getLicense().getLicenseString(), GREENHOPPER);
        if (ret.isDefined())
        {
            return ret;
        }
        
        // The following logic is adapted from GreenHopper's GreenHopperLicenseStoreImpl and JiraUtil
        
        String escapedLicense = getPropertySetString("GreenHopper", 1L, "LICENSE");
        if (escapedLicense == null)
        {
            return none();
        }
        String licenseString = escapedLicense.replaceAll("@", "a").replaceAll("\\!", "i");
        return extractLicense(licenseString, GREENHOPPER);
    }
    
    private Option<Pair<ProductLicense, String>> getBonfireLicense()
    {
        Option<Pair<ProductLicense, String>> ret = extractLicense(jiraLicenseManager.getLicense().getLicenseString(), BONFIRE);
        if (ret.isDefined())
        {
            return ret;
        }
        
        // The following logic is adapted from Bonfire's BonfireLicenseServiceImpl
        
        String licenseString = getPropertySetString("Excalibur.properties", 1L, "bonfire-license");
        if (licenseString == null)
        {
            return none();
        }
        return extractLicense(licenseString, BONFIRE);
    }
    
    private Option<Pair<ProductLicense, String>> extractLicense(String licenseString, Product product)
    {
        ProductLicense productLicense = licenseManagerProvider.getLicenseManager().getLicense(licenseString).getProductLicense(product);
        if (productLicense != null)
        {
            return some(pair(productLicense, licenseString));
        }
        return none();
    }
    
    private String getPropertySetString(String entityName, long entityId, String propertyName)
    {
        Map<Object, Object> ofbizArgs = ImmutableMap.<Object, Object>of("delegator.name", "default",
                                                                        "entityName", entityName,
                                                                        "entityId", entityId);
        PropertySet ofbizPs = PropertySetManager.getInstance("ofbiz", ofbizArgs);
        return ofbizPs.getText(propertyName);
    }
}
