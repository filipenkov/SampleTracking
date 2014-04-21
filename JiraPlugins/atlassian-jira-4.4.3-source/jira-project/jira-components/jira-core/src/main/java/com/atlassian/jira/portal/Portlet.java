/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;

import java.util.Map;

public interface Portlet
{
    public String getId();

    public String getName();

    public String getDescription();

    /**
     * Returns true if the portlet requires a specific permission to view it.
     *
     * @return true If this portlet requires a permission i.e. getPermission != -1
     */
    public boolean hasPermission();

    /**
     * Returns the permission required to view this portlet. See {@link com.atlassian.jira.security.Permissions} for
     * symbolic constants.
     *
     * @return Returns a {@link com.atlassian.jira.security.Permissions Permission}. or -1 indicating no permission.
     */
    public int getPermission();

    /**
     * The path to a thumbnail image of this portlet.
     *
     * @return Path to image.
     */
    public String getThumbnailfile();

    public String getCornerThumbnail();

    void init(PortletModuleDescriptor descriptor);

    public String getTemplateLocation();

    public String getViewHtml(PortletConfiguration portletConfiguration);

    ObjectConfiguration getObjectConfiguration(Map map) throws ObjectConfigurationException;

    public PortletModuleDescriptor getDescriptor();

}
