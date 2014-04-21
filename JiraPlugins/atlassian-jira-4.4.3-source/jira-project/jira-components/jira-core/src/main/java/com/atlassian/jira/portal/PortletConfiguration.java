package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurable;
import com.atlassian.gadgets.dashboard.Color;

import java.net.URI;
import java.util.Map;

/**
 * A representation of a configuration for a Portlet on a page. In addition to the ObjectConfigurable
 * properties of the Portlet, the row and column are available as well as the id of the dashboard page.
 * The interface is a bit bogus - partially a dashboard page position bean with some other stuff on it.
 * <p/>
 * Only the main implementation, {@link com.atlassian.jira.portal.PortletConfigurationImpl}, properly uses
 * the full interface.
 * <p/>
 * If we ever remove support for Legacy Portlets this should probably be converted into a
 * final class and no longer implement ObjectConfigurable.  However with the current model where we still need
 * to support the older style portlets ti will remain as is.
 */
public interface PortletConfiguration extends Comparable<PortletConfiguration>, ObjectConfigurable
{
    /**
     * Return the id of the PortletConfiguration.
     *
     * @return the id of the PortletConfiguration.
     */
    public Long getId();

    /**
     * Returns the portlet implementation that this PortletConfiguration is for.
     *
     * @return the portlet.
     * @deprecated This is only needed for legacy portlets now.  Gadgets should no longer use this.
     */
    public Portlet getPortlet();

    /**
     * Represents the column that the configured portlet resides in.
     *
     * @return the column number starting from 1.
     */
    public Integer getColumn();

    /**
     * Sets the column for the Portlet, effectively moving the portlet left or right on the page.
     *
     * @param column the column number starting from 1.
     */
    public void setColumn(Integer column);

    /**
     * Represents the row that the configured portlet resides in.
     *
     * @return the row number starting from 1.
     */
    public Integer getRow();

    /**
     * Sets the row for the Portlet, effectively moving the portlet up or down on the page.
     *
     * @param row the row number starting from 1.
     */
    public void setRow(Integer row);

    /**
     * Provides the dashboard page id.
     *
     * @return the dashboard page id.
     */
    public Long getDashboardPageId();

    /**
     * Sets the dashboard page id.
     *
     * @param portalPageId the dashboard page id.
     */
    public void setDashboardPageId(Long portalPageId);

    /**
     * Declares whether the portlet should expect to have resources such as CSS and JavaScript files
     * included in the page. If false, the portlet should assume that these resources are unavailable
     * and not render any tricky sexy magic that relies on JavaScript functions defined in these files.
     * The RunPortlet action will most likely provide an implementaion which returns false to this
     * method and Portlets rendered there will therefore need to work without their resource files.
     *
     * @return true only if the CSS and JavaScript files etc. will be available on the rendered page.
     */
    public boolean isResourcesProvided();

    /**
     * Returns the URI pointing to the Gadget XML for this particular portlet.  May return null for
     * legacy portlets (that don't implement the Gadget spec).
     *
     * @see http://code.google.com/apis/gadgets/docs/reference.html
     * @return URI pointing to the Gadget XML or null
     */
    URI getGadgetURI();

    /**
     * Returns the color to use when rendering the Chrome of this gadget.
     *
     * @return color to use when rendering the Chrome of this gadget
     */
    Color getColor();

    /**
     * Set the color of the chrome for a gadget.
     *
     * @param color the color of the chrome for a gadget.
     */
    void setColor(Color color);

    /**
     * An unmodifiable map of user preferences stored for this gadget.  Will return an empty map in the case
     * of a legacy gadget.
     *
     * @see http://code.google.com/apis/gadgets/docs/reference.html#Userprefs_Ref
     * @return map of user preferences stored for this gadget.
     */
    Map<String, String> getUserPrefs();

    /**
     * Sets the userPreferences for this portletconfig.
     * @param userPrefs A map of key value pairs
     */
    void setUserPrefs(Map<String, String> userPrefs);
}
