package com.atlassian.jira.web.action.portal;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import webwork.action.ParameterAware;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class RunPortlet extends JiraWebActionSupport implements ParameterAware
{
    String portletKey;
    private Map params;
    private PortletConfiguration portletConfiguration;
    private Portlet portlet;
    private final PluginAccessor pluginAccessor;

    private static final Logger log = Logger.getLogger(RunPortlet.class);

    public RunPortlet(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    protected String doExecute()
    {
        PortletModuleDescriptor moduleDescriptor = null;

        try
        {
            moduleDescriptor = (PortletModuleDescriptor) pluginAccessor.getEnabledPluginModule(portletKey);
        }
        catch (Exception exception)
        {
            log.debug("An exception occurred when trying to read the module descriptor for the portlet key '" + portletKey + "'", exception);
        }

        if (moduleDescriptor == null)
        {
            return ERROR;
        }

        portlet = (Portlet) moduleDescriptor.getModule();
        portletConfiguration = new ParameterPortletConfiguration(portlet, params);

        return SUCCESS;
    }

    public PortletConfiguration getPortletConfiguration()
    {
        return portletConfiguration;
    }

    public Portlet getPortlet()
    {
        return portlet;
    }

    public void setPortletKey(String portletKey)
    {
        this.portletKey = portletKey;
    }

    public void setParameters(Map params)
    {
        this.params = params;
    }

    private static class ParameterPortletConfiguration extends PortletConfigurationImpl
    {
        private Portlet portlet;
        private Map params;

        public ParameterPortletConfiguration(Portlet portlet, Map params)
        {
            super(-1L, null, null, portlet, 1, 1, null, null, null, Collections.<String, String>emptyMap());
            this.portlet = portlet;
            this.params = params;
        }

        public void setColumn(Integer column)
        {
            throw new UnsupportedOperationException("Cannot set column of temporary portlet configuration.");
        }

        public void setRow(Integer row)
        {
            throw new UnsupportedOperationException("Cannot set column of temporary portlet configuration.");
        }

        public boolean isUsingDefaultPortalConfig(User user)
        {
            return true;
        }

        public Portlet getPortlet()
        {
            return portlet;
        }

        /**
         * For RunPortlet, resources are not provided, portlets should not rely on JavaScript functions defined in them.
         *
         * @return false always.
         */
        public boolean isResourcesProvided()
        {
            return false;
        }

        public URI getGadgetURI()
        {
            throw new UnsupportedOperationException("Legacy portlets do not support gadget properties.");
        }

        public Map<String, String> getUserPrefs()
        {
            throw new UnsupportedOperationException("Legacy portlets do not support gadget properties.");
        }

        public Color getColor()
        {
            throw new UnsupportedOperationException("Legacy portlets do not support gadget properties.");
        }

        public PropertySet getProperties()
        {
            throw new UnsupportedOperationException("Cannot get all properties with temporary portlet configuration.");
        }

        public String getProperty(String propertyKey) throws ObjectConfigurationException
        {
            final String[] paramValues = (String[]) params.get(propertyKey);

            if (paramValues != null && paramValues.length > 0)
            {
                return paramValues[0];
            }

            return getDefaultProperty(propertyKey);
        }

        public String getTextProperty(String propertyKey) throws ObjectConfigurationException
        {
            return getProperty(propertyKey);
        }
    }
}
