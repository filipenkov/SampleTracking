package com.atlassian.gadgets.dashboard.internal;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.util.WebItemFinder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableMap;

public class DashboardWebItemFinder extends WebItemFinder
{
    
    public DashboardWebItemFinder(WebInterfaceManager webInterfaceManager,
            ApplicationProperties applicationProperties,
            I18nResolver i18n)
    {
        super(webInterfaceManager, applicationProperties, i18n);
    }

    public Iterable<WebItem> findDashboardMenuItems(DashboardId dashboardId, String username)
    {
        ImmutableMap.Builder<String, Object> context = getContext(dashboardId, username);
        return findMenuItemsInSection(DASHBOARD_MENU_SECTION, context.build());
    }

    public Iterable<WebItem> findDashboardToolsMenuItems(DashboardId dashboardId, String username)
    {
        ImmutableMap.Builder<String, Object> context = getContext(dashboardId, username);
        return findMenuItemsInSection(DASHBOARD_TOOLS_MENU_SECTION, context.build());
    }

    private ImmutableMap.Builder<String, Object> getContext(final DashboardId dashboardId, final String username)
    {
        ImmutableMap.Builder<String, Object> context = ImmutableMap.<String, Object>builder().put("dashboardId", dashboardId);
        if (username != null)
        {
            context.put("username", username);
        }
        return context;
    }

}
