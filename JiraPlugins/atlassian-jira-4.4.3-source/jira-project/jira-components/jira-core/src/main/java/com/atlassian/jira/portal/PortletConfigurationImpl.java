package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration for a portlet. This is the state that is saved to the database.
 * This implmentation uses a passed in PropertySet to store the configurable properties.  It is recommended that
 * an in memory PropertySet is used as the store now persists this manually.
 *
 * @since ??
 */
public class PortletConfigurationImpl implements PortletConfiguration
{
    private final PropertySet ps;
    private final Portlet portlet;

    private final Long id;
    private Long dashboardPageId;
    private final String key;
    private Integer column;
    private Integer row;
    private Color color;
    private Map<String,String> userPrefs;
    private final URI gadgetUri;

    public PortletConfigurationImpl(final Long id, final Long dashboardPageId, final String portletKey,
            final Portlet portlet, final Integer column, final Integer row, final PropertySet configuration,
            final URI gadgetUri, final Color color, final Map<String,String> userPrefs)
    {
        this.id = id;
        this.dashboardPageId = dashboardPageId;
        key = portletKey;
        this.portlet = portlet;
        this.column = column;
        this.row = row;
        ps = configuration;
        this.gadgetUri = gadgetUri;
        //color1 will be the default color if none was specified!
        this.color = color == null ? Color.color1 : color;
        this.userPrefs = Collections.unmodifiableMap(new HashMap<String,String>(userPrefs));
    }

    public Long getId()
    {
        return id;
    }

    public Portlet getPortlet()
    {
        return portlet;
    }

    public Integer getColumn()
    {
        return column;
    }

    public String getKey()
    {
        return key;
    }

    public void setColumn(final Integer column)
    {
        this.column = column;
    }

    public Integer getRow()
    {
        return row;
    }

    public Long getDashboardPageId()
    {
        return dashboardPageId;
    }

    public void setDashboardPageId(final Long dashboardPageId)
    {
        this.dashboardPageId = dashboardPageId;
    }

    public void setRow(final Integer row)
    {
        this.row = row;
    }

    /**
     * Normal rendition of the Portlet should have JavaScript and CSS etc. resources provided somewhere on the page.
     *
     * @return true always.
     */
    public boolean isResourcesProvided()
    {
        return true;
    }

    public URI getGadgetURI()
    {
        return gadgetUri;
    }

    public Map<String, String> getUserPrefs()
    {
        return userPrefs;
    }

    public void setUserPrefs(final Map<String, String> userPrefs)
    {
        this.userPrefs = new HashMap<String,String>(userPrefs);
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getPortlet().getObjectConfiguration(null);
    }

    public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return getProperties().exists(propertyKey);
    }

    /**
     * Return the property if it is found, else return the default property
     *
     * @param propertyKey The key to look up from the PropertySet
     * @throws ObjectConfigurationException
     */
    public String getProperty(final String propertyKey) throws ObjectConfigurationException
    {
        if (dashboardPageId != null)
        {
            final String property = getProperties().getString(propertyKey);
            return (property != null ? property : getDefaultProperty(propertyKey));
        }
        else
        {
            return getDefaultProperty(propertyKey);
        }
    }

    public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
    {
        if (dashboardPageId != null)
        {
            final String property = getProperties().getText(propertyKey);
            return (property != null ? property : getDefaultProperty(propertyKey));
        }
        else
        {
            return getDefaultProperty(propertyKey);
        }
    }

    public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
    {
        Long value = null;
        final String property = getProperty(propertyKey);
        if ((property != null) && (property.length() > 0))
        {
            try
            {
                value = new Long(property);
            }
            catch (final NumberFormatException e)
            {
                throw new ObjectConfigurationException("Could not get Long from " + property);
            }
        }
        return value;
    }

    public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return getObjectConfiguration().getFieldDefault(propertyKey);
    }

    public PropertySet getProperties()
    {
        return ps;
    }

    public int compareTo(final PortletConfiguration that)
    {
        return getRow().compareTo(that.getRow());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
