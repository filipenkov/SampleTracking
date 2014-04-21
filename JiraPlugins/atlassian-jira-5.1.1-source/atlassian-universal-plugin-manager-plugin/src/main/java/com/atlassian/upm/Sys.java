package com.atlassian.upm;

import com.atlassian.upm.test.rest.resources.PacBaseUrlResource;
import com.atlassian.upm.test.rest.resources.SysResource;

import com.google.common.collect.ImmutableList;

import static java.lang.Boolean.getBoolean;
import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class Sys
{
    private static final String UPM_PAC_BASE_URL = System.getProperty("pac.baseurl", "https://plugins.atlassian.com/server");
    public static final String UPM_PAC_DISABLE = "upm.pac.disable";
    public static final String UPM_ON_DEMAND = "atlassian.upm.on.demand";
    public static final String UPM_USER_INSTALLED_OVERRIDE = "atlassian.upm.user.installed.override";
    public static final String UPM_XSRF_TOKEN_DISABLE = "upm.xsrf.token.disable";

    public static boolean isDevModeEnabled()
    {
        return getBoolean("atlassian.dev.mode") || getBoolean("jira.dev.mode");
    }

    public static boolean isUpmDebugModeEnabled()
    {
        return getBoolean("atlassian.upm.debug");
    }

    public static boolean isOnDemand()
    {
        // This allows us to set the isOnDemand flag in integration UI tests
        return SysResource.getIsOnDemand() != null ? SysResource.getIsOnDemand() : getBoolean(UPM_ON_DEMAND);
    }

    public static boolean isPacDisabled()
    {
        return getBoolean(UPM_PAC_DISABLE) || isOnDemand();
    }

    public static boolean isXsrfTokenDisabled()
    {
        return getBoolean(UPM_XSRF_TOKEN_DISABLE);
    }
    
    public static String getPacBaseUrl()
    {
        // UPM-1004 - we need to be able to manipulate this string for test purposes
        return (PacBaseUrlResource.getPacBaseUrl() != null) ? PacBaseUrlResource.getPacBaseUrl() : UPM_PAC_BASE_URL;
    }

    public static String getMacBaseUrl()
    {
        String macBaseUrl = System.getProperty("mac.baseurl");
        return macBaseUrl != null ? macBaseUrl : "https://my.atlassian.com";
    }

    /**
     * UPM-1782 OnDemand wants to list specific bundled plugins as user-installed plugins.
     */
    public static Iterable<String> getOverriddenUserInstalledPluginKeys()
    {
        String pluginKeys = System.getProperty(UPM_USER_INSTALLED_OVERRIDE);
        if (!isOnDemand() || isBlank(pluginKeys))
        {
            return ImmutableList.of();
        }

        return ImmutableList.of(pluginKeys.split(","));
    }
}
