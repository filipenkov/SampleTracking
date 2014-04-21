package com.atlassian.jira.admin.contextproviders;

import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.ContextProvider;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Provides the Project Panel on the Admin Summary Screen
 *
 * @since v4.4
 */
public class PluginPanelContextProvider extends AdminSummaryPanelContextProvider implements ContextProvider
{
    private static final Logger log = Logger.getLogger(PluginPanelContextProvider.class);
    private static final String PLUGIN_SECTION = "admin_plugins_menu";
    private static final String SECTION_NAME = "admin.menu.system.plugins";

    public PluginPanelContextProvider(JiraAuthenticationContext authenticationContext, VelocityRequestContextFactory requestContextFactory, SimpleLinkManager linkManager)
    {
        super(requestContextFactory, linkManager, authenticationContext);
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        List<SimpleLinkSection> pluginSections = linkManager.getNotEmptySectionsForLocation(PLUGIN_SECTION, authenticationContext.getLoggedInUser(), jiraHelper);
        return getContextMap(PLUGIN_SECTION, context, SECTION_NAME, pluginSections).toMap();
    }

}
