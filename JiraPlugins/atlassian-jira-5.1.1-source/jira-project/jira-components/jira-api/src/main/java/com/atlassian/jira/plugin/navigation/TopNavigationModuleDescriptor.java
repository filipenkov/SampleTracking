package com.atlassian.jira.plugin.navigation;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.plugin.web.descriptors.ConditionalDescriptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Module descriptor used for plugins that render the top navigation in JIRA.
 *
 * @since v3.12
 */
//@RequiresRestart
public interface TopNavigationModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableTopNavigation>, OrderableModuleDescriptor, ConditionalDescriptor
{
    public String getTopNavigationHtml(HttpServletRequest request, Map<String, Object> startingParms);
}
