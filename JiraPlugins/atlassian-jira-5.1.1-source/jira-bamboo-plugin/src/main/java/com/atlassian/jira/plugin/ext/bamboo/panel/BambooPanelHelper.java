package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper class for common things needed between the various panels
 */
public class BambooPanelHelper
{
    private static final Logger log = Logger.getLogger(BambooPanelHelper.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String BAMBOO_PLUGIN_KEY = "com.atlassian.jira.plugin.ext.bamboo";
    public static final String SELECTED_SUB_TAB_KEY = "selectedSubTab";

    public static final String SUB_TAB_PLAN_STATUS = "planStatus";
    public static final String SUB_TAB_BUILD_BY_PLAN = "buildByPlan";
    public static final String SUB_TAB_BUILD_BY_DATE = "buildByDate";
    public static final List SUB_TABS = EasyList.build(SUB_TAB_BUILD_BY_DATE, SUB_TAB_PLAN_STATUS);
    public static final List<String> ALL_SUB_TABS = EasyList.build(SUB_TAB_BUILD_BY_DATE, SUB_TAB_PLAN_STATUS, SUB_TAB_BUILD_BY_PLAN);


    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final WebResourceManager webResourceManager;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BambooPanelHelper(final WebResourceManager webResourceManager, final BambooApplicationLinkManager bambooApplicationLinkManager, final PermissionManager permissionManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.webResourceManager = webResourceManager;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public void prepareVelocityContext(Map<String, Object> velocityParams, String bambooPluginModuleKey, String baseLinkUrl, String queryString, List subTabs, Project project)
    {
        String selectedSubTab;
        if (subTabs != null && !subTabs.isEmpty())
        {
            velocityParams.put("availableTabs", subTabs);
            selectedSubTab = retrieveFromRequestOrSession(BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_BUILD_BY_DATE);
            if (!ALL_SUB_TABS.contains(selectedSubTab))
            {
                selectedSubTab = BambooPanelHelper.SUB_TAB_BUILD_BY_DATE;
            }
        }
        else
        {
            selectedSubTab = SUB_TAB_BUILD_BY_DATE;
        }
        velocityParams.put(BambooPanelHelper.SELECTED_SUB_TAB_KEY, selectedSubTab);

        if (BambooPanelHelper.SUB_TAB_BUILD_BY_DATE.equals(selectedSubTab))
        {
            velocityParams.put("showRss", Boolean.TRUE);
        }

        prepareVelocityContext(velocityParams, bambooPluginModuleKey, baseLinkUrl, queryString, project);

    }

    public void prepareVelocityContext(Map<String, Object> velocityParams, String bambooPluginModuleKey, String baseLinkUrl, String queryString, Project project)
    {
        webResourceManager.requireResource(BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + "css");
        ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(project.getKey());

        velocityParams.put("moduleKey", bambooPluginModuleKey);
        velocityParams.put("querySection", queryString);
        velocityParams.put("baseLinkUrl", baseLinkUrl);
        velocityParams.put("baseResourceUrl", "/download/resources/" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + bambooPluginModuleKey);

        if (applicationLink != null)
        {
            velocityParams.put("baseBambooUrl", applicationLink.getDisplayUrl().toASCIIString());
            velocityParams.put("baseBambooRestUrl", applicationLink.getRpcUrl() + "/rest/api/latest/");
        }

        velocityParams.put("baseRestUrl", "/rest/bamboo/1.0/");
        velocityParams.put("baseBambooRestProxyUrl", "/rest/bamboo/1.0/proxy/");

        sanitiseParams(velocityParams);

        //not sanitised on purpose
        if (applicationLink != null)
        {
            velocityParams.put("bambooServerName", applicationLink.getName());
        }

        velocityParams.put("isSystemAdmin", permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraAuthenticationContext.getLoggedInUser()));
    }

    protected void sanitiseParams(final Map<String, Object> velocityParams)
    {
        for (Map.Entry<String,Object> entry : velocityParams.entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof String)
            {
                entry.setValue(sanitiseString((String)value));
            }
        }
    }

    private static final Pattern UNSAFE_CHARACTERS_REGEX = Pattern.compile("[\"'<>\\\\]");

    protected static String sanitiseString(final String string)
    {
        return UNSAFE_CHARACTERS_REGEX.matcher(string).replaceAll("");
    }
    
    // -------------------------------------------------------------------------------------------------- Private Helper
    private String retrieveFromRequestOrSession(final String requestKey, String defaultValue)
    {
        final String value = retrieveFromRequestOrSession(requestKey);
        return value != null ? value : defaultValue;
    }

    private String retrieveFromRequestOrSession(final String requestKey)
    {
        final String sessionKey = BambooPanelHelper.BAMBOO_PLUGIN_KEY + "." + requestKey;

        final String paramFromRequest = ParameterUtils.getStringParam(ActionContext.getParameters(), requestKey);
        final Map session = ActionContext.getSession();

        if (StringUtils.isNotBlank(paramFromRequest))
        {
            // Sets param in session & return
            session.put(sessionKey, paramFromRequest);
            return paramFromRequest;
        }
        else
        {
            // Try to get it from the session
            return (String) session.get(sessionKey);
        }
    }
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators


}
