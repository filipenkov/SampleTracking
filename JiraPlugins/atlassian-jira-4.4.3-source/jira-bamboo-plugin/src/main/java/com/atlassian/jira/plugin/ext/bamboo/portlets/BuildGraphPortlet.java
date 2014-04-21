package com.atlassian.jira.plugin.ext.bamboo.portlets;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BuildGraphPortlet extends AbstractBambooPortletImpl
{
    private static final Logger log = Logger.getLogger(BuildGraphPortlet.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public BuildGraphPortlet(JiraAuthenticationContext jiraAuthenticationContext,
                             PermissionManager permissionManager,
                             ApplicationProperties applicationProperties,
                             BambooApplicationLinkManager applicationLinkManager,
                             ProjectManager projectManager)
    {
        super(jiraAuthenticationContext, permissionManager, applicationProperties, applicationLinkManager, projectManager);
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    @Override
    protected Map getVelocityParams(PortletConfiguration portletConfiguration)
    {
        Map params = new HashMap();
        params.put("responseHtml", getText("bamboo.jiraportlet.error.portletNotUpgraded"));
        return params;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators


}
