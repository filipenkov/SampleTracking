package com.atlassian.jira.plugin.ext.bamboo.portlets;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;

public abstract class AbstractBambooPortletImpl extends PortletImpl
{    
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private I18nHelper i18nBean = null;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected BambooApplicationLinkManager applicationLinkManager;
    protected ProjectManager projectManager;
    // ---------------------------------------------------------------------------------------------------- Constructors
    protected AbstractBambooPortletImpl(JiraAuthenticationContext jiraAuthenticationContext,
                         PermissionManager permissionManager,
                         ApplicationProperties applicationProperties,
                         BambooApplicationLinkManager applicationLinkManager,
                         ProjectManager projectManager)
    {
        super(jiraAuthenticationContext, permissionManager, applicationProperties);
        this.applicationLinkManager = applicationLinkManager;
        this.projectManager = projectManager;
    }
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    protected I18nHelper getI18nBean()
    {
        if (i18nBean == null)
        {
            i18nBean = authenticationContext.getI18nHelper();
        }
        return i18nBean;
    }

    protected String getText(String i18nKey)
    {
        return getI18nBean().getText(i18nKey);
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
