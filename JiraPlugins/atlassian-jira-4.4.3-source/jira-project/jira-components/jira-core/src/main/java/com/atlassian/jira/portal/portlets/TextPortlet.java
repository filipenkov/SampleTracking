package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Implementation of text portlet that renders HTML text.
 *
 * @since v3.13
 */
public class TextPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(TextPortlet.class);

    public TextPortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties)
    {
        super(authenticationContext, permissionManager, applicationProperties);
    }

    protected Map /* <String, Object> */getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map params = super.getVelocityParams(portletConfiguration);
        params.put("title", getTitle(portletConfiguration));
        params.put("html", getHtml(portletConfiguration));
        return params;
    }

    /**
     * Retrieves the title from portlet configuration and returns it.
     * If it fails retrieving it or title is null returns an empty string.
     *
     * @param portletConfiguration portlet configuration
     * @return title, never null
     */
    private String getTitle(final PortletConfiguration portletConfiguration)
    {
        try
        {
            final String title = portletConfiguration.getProperty("title");
            return title == null ? "" : title;
        }
        catch (final ObjectConfigurationException e)
        {
            log.error("Failed getting the 'title' property for Text portlet [portal page id : " + portletConfiguration.getDashboardPageId() + ", portlet id : " + portletConfiguration.getId() + "]");
        }
        return "";
    }

    /**
     * Retrieves the HTML text from portlet configuration and returns it.
     * If it fails retrieving it or title is null returns an empty string.
     *
     * @param portletConfiguration portlet configuration
     * @return title, never null
     */
    private String getHtml(final PortletConfiguration portletConfiguration)
    {
        try
        {
            final String html = portletConfiguration.getTextProperty("html");
            return html == null ? "" : html;
        }
        catch (final ObjectConfigurationException e)
        {
            log.error("Failed getting the 'html' text property for Text portlet [portal page id : " + portletConfiguration.getDashboardPageId() + ", portlet id : " + portletConfiguration.getId() + "]");
            return "";
        }
    }

}
