package com.atlassian.jira.web.component;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.configurable.ObjectConfigurationTypes;
import com.atlassian.jira.portal.PortletConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;

/**
 * Main implementation of {@link com.atlassian.jira.web.component.DashboardPageConfigUrlFactory} which links portlet
 * configuration to the original dashboard and dashboard config actions.
 *
 * @since v3.13
 */
public class DashboardPageConfigUrlFactoryImpl implements DashboardPageConfigUrlFactory
{
    private final Long portalPageId;

    public DashboardPageConfigUrlFactoryImpl(final Long portalPageId)
    {
        this.portalPageId = portalPageId;
    }

    public String getRunPortletUrl(final PortletConfigurationAdaptor accessor)
    {
        // TODO: factor out url building or use URLUtil or TrustedApplicationBuilder.toQueryString()'s QueryBuilder
        final StringBuffer sb = new StringBuffer();
        sb.append("secure/RunPortlet.jspa?portletKey=");
        sb.append(accessor.getPortletId());

        final Collection keys = accessor.getKeys();
        if (!keys.isEmpty())
        {
            try
            {
                sb.append("&");
                for (final Iterator iterator = keys.iterator(); iterator.hasNext();)
                {
                    final String key = (String) iterator.next();
                    sb.append(URLEncoder.encode(key, "UTF8"));
                    sb.append("=");

                    sb.append(URLEncoder.encode(accessor.getPropertyAsString(key), "UTF8"));

                    if (iterator.hasNext())
                    {
                        sb.append("&");
                    }
                }
            }
            catch (final UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    public String getEditPortletUrl(final Long portletConfigId)
    {
        return new StringBuffer("secure/SavePortlet!default.jspa?destination=dashboard&portletConfigId=").append(portletConfigId).append(
            "&portalPageId=").append(portalPageId).toString();
    }

    /**
     * Default production implementation which adapts the PortletConfiguration interface to a more convenient one.
     */
    class PortletConfigurationAdaptorImpl implements PortletConfigurationAdaptor
    {
        private final PortletConfiguration portletConfiguration;

        PortletConfigurationAdaptorImpl(final PortletConfiguration portletConfiguration)
        {
            this.portletConfiguration = portletConfiguration;
        }

        public String getPropertyAsString(final String key)
        {
            try
            {
                if (portletConfiguration.getObjectConfiguration().getFieldType(key) == ObjectConfigurationTypes.TEXT)
                {
                    return portletConfiguration.getTextProperty(key);
                }
                return portletConfiguration.getProperty(key);
            }
            catch (final ObjectConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }

        public String getPortletId()
        {
            return portletConfiguration.getPortlet().getId();
        }

        public Collection getKeys()
        {
            try
            {
                return portletConfiguration.getProperties().getKeys();
            }
            catch (final ObjectConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
