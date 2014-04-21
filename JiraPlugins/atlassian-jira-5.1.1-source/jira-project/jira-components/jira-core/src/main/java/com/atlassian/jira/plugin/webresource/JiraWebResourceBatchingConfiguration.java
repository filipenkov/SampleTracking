package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.UserAgentUtil;
import com.atlassian.jira.util.UserAgentUtilImpl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.webresource.DefaultResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * Determins which resources are included superbatched on every page!
 *
 * @since v4.3
 */
public class JiraWebResourceBatchingConfiguration implements ResourceBatchingConfiguration
{
    private static final String USER_AGENT = "USER-AGENT";

    private static final boolean DEV_MODE = JiraSystemProperties.isDevMode();
    private static final boolean SUPER_BATCHING_DISABLED = JiraSystemProperties.isSuperBatchingDisabled();
    private static final String WEB_RESOURCE_BATCHING_OFF = System.getProperty(DefaultResourceBatchingConfiguration.PLUGIN_WEBRESOURCE_BATCHING_OFF);
    private static final List<String> resources = new ArrayList<String>();

    static
    {
        resources.add("com.atlassian.auiplugin:ajs");
        resources.add("com.atlassian.auiplugin:aui-experimental-page-layout");
        resources.add("com.atlassian.auiplugin:aui-experimental-page-layout-typography");
        resources.add("jira.webresources:util-lite");
        resources.add("jira.webresources:util");
        resources.add("jira.webresources:inline-layer");
        resources.add("jira.webresources:content-retrievers");
        resources.add("jira.webresources:list");
        resources.add("jira.webresources:dropdown");
        resources.add("jira.webresources:issue-table");
        resources.add("jira.webresources:dropdown-select");
        resources.add("jira.webresources:select-pickers");
        resources.add("jira.webresources:dialogs");
        resources.add("jira.webresources:set-focus");
        resources.add("jira.webresources:jira-global");
        resources.add("jira.webresources:key-commands");
        resources.add("jira.webresources:header");
    }

    @Override
    public boolean isSuperBatchingEnabled()
    {
        return (!resources.isEmpty() && !SUPER_BATCHING_DISABLED) || forceBatchingInThisRequest();
    }

    @Override
    public boolean isContextBatchingEnabled()
    {
        return !DEV_MODE || forceBatchingInThisRequest();
    }

    @Override
    public boolean isPluginWebResourceBatchingEnabled()
    {
        boolean enabled;
        if (WEB_RESOURCE_BATCHING_OFF != null)
        {
            enabled = !Boolean.parseBoolean(WEB_RESOURCE_BATCHING_OFF);
        }
        else
        {
            enabled = !DEV_MODE;
        }

        return enabled || forceBatchingInThisRequest();
    }

    @Override
    public List<String> getSuperBatchModuleCompleteKeys()
    {
        return resources;
    }

    private boolean forceBatchingInThisRequest()
    {
        // only force batching if in DEV mode -- this whole "forcing" concept is a developer helper only
        if (!DEV_MODE)
        {
            return false;
        }
        
        HttpServletRequest httpRequest = ExecutingHttpRequest.get();
        if (httpRequest == null)
        {
            return false;
        }
        String userAgent = httpRequest.getHeader(USER_AGENT);
        UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
        UserAgentUtil.UserAgent userAgentInfo = userAgentUtil.getUserAgentInfo(userAgent);

        // force batching if we are on IE, disable otherwise
        return userAgentInfo.getBrowser().getBrowserFamily().equals(UserAgentUtil.BrowserFamily.MSIE);
    }
}
