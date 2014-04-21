package com.atlassian.upm.license.internal.host;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.ConfluenceLicenseRegistry;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
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

import static com.atlassian.extras.api.Product.CONFLUENCE;
import static com.atlassian.extras.api.Product.SHAREPOINT_PLUGIN;
import static com.atlassian.extras.api.Product.TEAM_CALENDARS;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.api.util.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.BooleanUtils.toBoolean;

public class ConfluenceHostLicenseProvider implements HostLicenseProvider
{
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceHostLicenseProvider.class);
    private static final String TEAM_CALENDARS_PLUGIN_KEY = "com.atlassian.confluence.extra.team-calendars";
    private static final String TEAM_CALENDARS_SETTINGS_KEY = "com.atlassian.confluence.extra.calendar3.license.LicenseAccessor";
    private static final String TEAM_CALENDARS_ADMIN_URI_PATH = "/admin/calendar/viewlicense.action";
    private static final String SHAREPOINT_CONNECTOR_PLUGIN_KEY = "com.atlassian.confluence.extra.sharepoint";
    private static final String SHAREPOINT_CONNECTOR_SETTINGS_KEY = "SHAREPOINT_SETTINGS_CONTAINER_BANDANA_KEY";
    private static final Pattern SHAREPOINT_CONNECTOR_LICENSE_XML_REGEX = Pattern.compile("<license>((?s:.*))</license>");
    private static final String SHAREPOINT_CONNECTOR_ADMIN_URI_PATH = "/admin/sharepoint-admin/sharepoint-admin.action";

    private final BandanaManager bandanaManager;
    private final ConfluenceLicenseRegistry licenseRegistry;
    private final LicenseManagerProvider licenseManagerProvider;

    public ConfluenceHostLicenseProvider(BandanaManager bandanaManager,
                                         LicenseManagerProvider licenseManagerProvider)
    {
        this.bandanaManager = checkNotNull(bandanaManager, "bandanaManager");
        this.licenseManagerProvider = checkNotNull(licenseManagerProvider, "licenseManagerProvider");
        this.licenseRegistry = new ConfluenceLicenseRegistry();
    }
    
    public Iterable<ProductLicense> getHostApplicationLicense()
    {
        for (Pair<AtlassianLicense, String> mainLicenseAndString : getMainLicense())
        {
            return option(mainLicenseAndString.first().getProductLicense(CONFLUENCE));
        }
        return none();
    }

    @Override
    public Iterable<String> getSupportedLegacyPluginKeys()
    {
        return ImmutableList.of(TEAM_CALENDARS_PLUGIN_KEY, SHAREPOINT_CONNECTOR_PLUGIN_KEY);
    }
    
    @Override
    public Option<Pair<ProductLicense, String>> getPluginLicenseDetails(String pluginKey)
    {
        if (pluginKey.equals(TEAM_CALENDARS_PLUGIN_KEY))
        {
            return getTeamCalendarsLicense();
        }
        else if (pluginKey.equals(SHAREPOINT_CONNECTOR_PLUGIN_KEY))
        {
            return getSharepointConnectorLicense();
        }
        return none();
    }
    
    @Override
    public Option<String> getPluginLicenseAdminUriPath(String pluginKey)
    {
        if (pluginKey.equals(TEAM_CALENDARS_PLUGIN_KEY))
        {
            return some(TEAM_CALENDARS_ADMIN_URI_PATH);
        }
        else if (pluginKey.equals(SHAREPOINT_CONNECTOR_PLUGIN_KEY))
        {
            return some(SHAREPOINT_CONNECTOR_ADMIN_URI_PATH);
        }
        return none();
    }
    
    private Option<Pair<AtlassianLicense, String>> getMainLicense()
    {
        try
        {
            LicensePair licPair = new LicensePair(licenseRegistry.getLicenseMessage(), licenseRegistry.getLicenseHash());
            String licenseString = licPair.getOriginalLicenseString();
            for (AtlassianLicense license : parseLicense(licenseString))
            {
                return some(pair(license, licenseString));
            }
            return none();
        }
        catch (LicenseException e)
        {
            logger.error("Error getting product license", e);
            return none();
        }
    }

    private Option<Pair<ProductLicense, String>> getTeamCalendarsLicense()
    {
        // first check for a separately stored plugin license
        for (String pluginLicenseString : getBandanaString(TEAM_CALENDARS_SETTINGS_KEY))
        {
            for (AtlassianLicense license : parseLicense(pluginLicenseString))
            {
                for (ProductLicense pluginLicense : option(license.getProductLicense(TEAM_CALENDARS)))
                {
                    return some(pair(pluginLicense, pluginLicenseString));
                }
            }
            // plugin license is defective, doesn't actually contain a Team Calendars license
            return none();
        }
        
        // there's no separately stored plugin license - check in the main license attached to the product
        for (Pair<AtlassianLicense, String> mainLicenseAndString : getMainLicense())
        {
            ProductLicense confluenceLicense = mainLicenseAndString.first().getProductLicense(CONFLUENCE);
            if (confluenceLicense.isEvaluation() && !confluenceLicense.isExpired())
            {
                // piggybacking on product evaluation license - this is valid as far as Team Calendars is concerned,
                // but in terms of the UI we should still show the plugin as not having a license.
                return none();
            }
            if (toBoolean(confluenceLicense.getProperty("ondemand"))) 
            {
                // on-demand license - extract plugin sub-license from main license
                ProductLicense pluginLicense = mainLicenseAndString.first().getProductLicense(TEAM_CALENDARS);
                if (pluginLicense != null)
                {
                    return some(pair(pluginLicense, mainLicenseAndString.second()));
                }
            }
        }

        return none();
    }

    private Option<Pair<ProductLicense, String>> getSharepointConnectorLicense()
    {
        for (String pluginConfigString : getBandanaString(SHAREPOINT_CONNECTOR_SETTINGS_KEY))
        {
            // this plugin stores its configuration as an XML-serialized bean which can have
            // various properties depending on the plugin version; fortunately the license is
            // easy to parse out without actually deserializing the bean.
            Matcher matcher = SHAREPOINT_CONNECTOR_LICENSE_XML_REGEX.matcher(pluginConfigString);
            if (matcher.find())
            {
                String licenseString = matcher.group(1).trim()
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&#x0D;", "");
                for (AtlassianLicense license : parseLicense(licenseString))
                {
                    for (ProductLicense pluginLicense : option(license.getProductLicense(SHAREPOINT_PLUGIN)))
                    {
                        return some(pair(pluginLicense, licenseString));
                    }
                }
            }
        }
        return none();
    }
    
    private Option<AtlassianLicense> parseLicense(String licenseString)
    {
        try
        {
            return option(licenseManagerProvider.getLicenseManager().getLicense(licenseString));
        }
        catch (Exception e)
        {
            logger.warn("Error parsing license: " + e);
            return none();
        }
    }
    
    private Option<String> getBandanaString(String key)
    {
        return option((String) bandanaManager.getValue(ConfluenceBandanaContext.GLOBAL_CONTEXT, key));
    }
}
