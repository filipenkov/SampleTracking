package com.atlassian.jira.web.component;

import java.util.Collection;

/**
 * Responsible for creating URLs for editing the state and dashboard page location of portlets.
 * The URLs should be relative to context root.
 *
 * @since v3.13
 */
public interface DashboardPageConfigUrlFactory
{

    /**
     * Returns a URL for RunPortlet action that will execute the given portlet configuration.
     *
     * @param configurationAccessor portlet configuration
     * @return URL as String, never null
     */
    String getRunPortletUrl(PortletConfigurationAdaptor configurationAccessor);

    /**
     * Returns a URL for configuring portlets on the dashboard.
     *
     * @param portletConfigId the portlet to configure.
     * @return the URL, never null.
     */
    String getEditPortletUrl(Long portletConfigId);

    /**
     * Abstraction for describing the aspects of the PortletConfiguration object graph that we depend on
     * without depending on all the mechanical details like ObjectConfiguration and PropertySet.
     */
    public interface PortletConfigurationAdaptor
    {
        Collection getKeys();

        String getPropertyAsString(String key);

        String getPortletId();
    }
}
