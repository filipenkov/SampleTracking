/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserNameEqualsUtil;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.action.portal.AbstractSaveConfiguration;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortletImpl implements Portlet
{

    private PortletModuleDescriptor descriptor;
    protected final JiraAuthenticationContext authenticationContext;
    protected PermissionManager permissionManager;
    protected ApplicationProperties applicationProperties;
    private final UserNameEqualsUtil userNameEqualsUtil;

    /**
     * @deprecated Use full constructor instead.
     */
    @Deprecated
    public PortletImpl(final JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
        userNameEqualsUtil = new UserNameEqualsUtil();
    }

    public PortletImpl(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        userNameEqualsUtil = new UserNameEqualsUtil();
    }

    public void init(final PortletModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getTemplateLocation()
    {
        final ResourceDescriptor resourceDescriptor = descriptor.getResourceDescriptor("jsp", "view");
        if (resourceDescriptor == null)
        {
            return null;
        }
        return resourceDescriptor.getLocation();
    }

    /**
     * Returns the PortalPageService from the ComponentManager.  A call is being made directly to the
     * ComponentManager because of the incredible changes need in all Portlet implementations
     * to their constructor if we where to take it as a constructor parameter. 
     *
     * @return a PortalPageService
     */
    PortalPageService getPortalPageService()
    {
        return ComponentManager.getInstance().getPortalPageService();
    }

    /**
     * This is called to work out if a PortletConfiguration is on a page that is editable or not.
     *
     * @param portletConfiguration the PortletConfiguration in play
     * @return Boolean.TRUE if the parent PortalPage of the PortletConfiguration is owned and hence editable by the user
     */
    protected Boolean isEditablePortletConfig(final PortletConfiguration portletConfiguration)
    {
        final Long portalPageId = portletConfiguration.getDashboardPageId();
        if (portalPageId != null)
        {
            final User user = authenticationContext.getUser();
            final JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
            final PortalPage portalPage = getPortalPageService().getPortalPage(serviceContext, portalPageId);
            if (portalPage != null)
            {
                if (!portalPage.isSystemDefaultPortalPage() && userNameEqualsUtil.equals(portalPage.getOwnerUserName(), user))
                {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    public String getViewHtml(final PortletConfiguration portletConfiguration)
    {
        final Map<String, Object> startingParams = new HashMap<String, Object>();
        startingParams.put("portletConfig", portletConfiguration);
        startingParams.put("editablePortletConfig", isEditablePortletConfig(portletConfiguration));
        startingParams.putAll(getVelocityParams(portletConfiguration));
        return descriptor.getHtml("view", startingParams);
    }

    /**
     * Allow for the addition of additional Velocity parameters to the context.
     * Sub-classes may wish to override this to add their own object to the velocity context
     *
     * @param portletConfiguration portlet configuration
     * @return map of velocity parameters, never null
     */
    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        // Some universally useful params
        params.put("admin", isAdmin());
        params.put("loggedin", authenticationContext.getUser() != null);
        params.put("indexing", applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING));
        params.put("portlet", this);
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        params.put("requestContext", velocityRequestContext);
        params.put("baseurl", velocityRequestContext.getBaseUrl());
        params.put("textUtils", new TextUtils());
        params.put("configId", portletConfiguration.getId());
        params.put("helpUtil", new HelpUtil());
        params.put("userUtil", ComponentAccessor.getUserUtil());
        params.put("searchService", ComponentManager.getComponent(SearchService.class));

        return params;
    }

    private Boolean isAdmin()
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
    }

    public ObjectConfiguration getObjectConfiguration(final Map params) throws ObjectConfigurationException
    {
        return descriptor.getObjectConfiguration(params);
    }

    public String getId()
    {
        return descriptor.getCompleteKey();
    }

    // Return the i18n name
    public String getName()
    {

        final String labelKey = descriptor.getLabelKey();
        String label = descriptor.getI18nBean().getText(labelKey);
        if ((label == null) || label.equals(labelKey))
        {
            label = descriptor.getName();
        }
        return label;
    }

    public String getDescription()
    {
        return descriptor.getDescription();
    }

    public boolean hasPermission()
    {
        return getPermission() != -1;
    }

    public int getPermission()
    {
        return descriptor.getPermission();
    }

    public String getThumbnailfile()
    {
        return descriptor.getThumbnail();
    }

    public String getCornerThumbnail()
    {
        // find the last "/" and place the corner before the image filename
        final int i = getThumbnailfile().lastIndexOf("/");
        return getThumbnailfile().substring(0, i + 1) + "corner_" + getThumbnailfile().substring(i + 1);
    }

    public String getThumbnailPath()
    {
        return ComponentManager.getInstance().getWebResourceManager().getStaticPluginResource(descriptor, getThumbnailfile());
    }

    public String getCornerThumbnailPath()
    {
        return ComponentManager.getInstance().getWebResourceManager().getStaticPluginResource(descriptor, getCornerThumbnail());
    }

    ///CLOVER:OFF
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PortletImpl))
        {
            return false;
        }
        final PortletImpl portlet = (PortletImpl) o;
        return descriptor == null ? portlet.descriptor == null : descriptor.equals(portlet.descriptor);
    }

    @Override
    public int hashCode()
    {
        return (descriptor != null ? descriptor.hashCode() : 0);
    }

    public PortletModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public IssueConstant getIssueConstant(final GenericValue issueConstantGV)
    {
        if (issueConstantGV != null)
        {
            return ComponentAccessor.getConstantsManager().getIssueConstant(issueConstantGV);
        }
        else
        {
            return null;
        }
    }

    /**
     * Retrieve a list of values from delimited String.  Used for MultiSelect Object Configurables
     *
     * @param values Delimited String
     * @return List of Strings, never null
     */
    public List<String> getListFromMultiSelectValue(final String values)
    {
        final String[] vals = StringUtils.splitByWholeSeparator(values, AbstractSaveConfiguration.MULTISELECT_SEPARATOR);
        return vals == null ? Collections.<String>emptyList() : Arrays.asList(vals);
    }

}
