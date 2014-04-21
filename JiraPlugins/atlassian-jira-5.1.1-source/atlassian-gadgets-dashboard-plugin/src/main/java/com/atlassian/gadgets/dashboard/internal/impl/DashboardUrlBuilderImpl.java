package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.gadgets.util.AbstractUrlBuilder;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

public class DashboardUrlBuilderImpl extends AbstractUrlBuilder implements DashboardUrlBuilder
{
    public DashboardUrlBuilderImpl(ApplicationProperties applicationProperties, WebResourceManager webResourceManager)
    {
        super(applicationProperties, webResourceManager, "com.atlassian.gadgets.dashboard:dashboard-servlet");
    }

    /**
     * Overridden to return REST URIs instead of servlet URLs.
     * @return the base of the REST URI to a dashboard/gadget resource
     */
    @Override
    protected String getBaseUrl()
    {
        return applicationProperties.getBaseUrl() + "/rest/dashboards/1.0/";
    }

    public String buildDashboardUrl(DashboardId dashboardId)
    {
        return getBaseUrl() + dashboardId;
    }

    public String buildDashboardLayoutUrl(DashboardId dashboardId)
    {
        return buildDashboardUrl(dashboardId) + "/layout";
    }
        
    public String buildGadgetUrl(DashboardId dashboardId, GadgetId gadgetId)
    {
        return buildDashboardUrl(dashboardId) + "/gadget/" + gadgetId;
    }

    public String buildGadgetColorUrl(DashboardId dashboardId, GadgetId gadgetId)
    {
        return buildGadgetUrl(dashboardId, gadgetId) + "/color";
    }

    public String buildGadgetUserPrefsUrl(DashboardId dashboardId, GadgetId gadgetId)
    {
        return buildGadgetUrl(dashboardId, gadgetId) + "/prefs";
    }

    public String buildErrorGadgetUrl()
    {
        return webResourceManager.getStaticPluginResource("com.atlassian.gadgets.dashboard:dashboard", "files/", UrlMode.AUTO) + "errorGadget.html";
    }

    public String buildDashboardResourceUrl(DashboardId dashboardId)
    {
        return applicationProperties.getBaseUrl() + "/rest/dashboards/1.0/" + dashboardId;
    }


    public String buildDashboardDirectoryResourceUrl()
    {
        return applicationProperties.getBaseUrl() + "/rest/config/1.0/directory";
    }

    public String buildDashboardDirectoryBaseUrl()
    {
        return Uri.ensureTrailingSlash(applicationProperties.getBaseUrl());
    }

    public String buildDashboardDirectoryUrl(DashboardId dashboardId)
    {
        return getBaseUrl() + "/directory/" + dashboardId;
    }
    
    public String buildSubscribedGadgetFeedsUrl()
    {
        return buildDashboardDirectoryResourceUrl() + "/subscribed-gadget-feeds";
    }

    public String buildDashboardDiagnosticsRelativeUrl()
    {
        return Uri.create(applicationProperties.getBaseUrl() + "/plugins/servlet/gadgets/dashboard-diagnostics").getPath();
    }

    public String buildSecurityTokensUrl()
    {
        return applicationProperties.getBaseUrl() + "/plugins/servlet/gadgets/security-tokens";
    }
}
