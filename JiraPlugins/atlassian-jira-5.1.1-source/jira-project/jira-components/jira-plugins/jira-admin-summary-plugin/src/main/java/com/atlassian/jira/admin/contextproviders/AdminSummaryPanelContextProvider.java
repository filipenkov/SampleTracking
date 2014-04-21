package com.atlassian.jira.admin.contextproviders;

import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Map;

/**
 * Abstract parent for all the Admin Summary standard panels
 *
 * @since v4.4
 */
public  abstract class AdminSummaryPanelContextProvider
{
    private static final String SECTION_NAME_KEY = "panelName";
    private static final String SECTION_ID_KEY = "outerSectionId";
    private static final String SECTIONS_KEY = "childSections";
    private static final String LINK_MANAGER_KEY = "linkManager";
    private static final String JIRA_HELPER_KEY = "helper";
    private static final String USER_KEY = "user";
    protected static final String BASE_URL_KEY = "baseUrl";
    protected final JiraAuthenticationContext authenticationContext;
    protected final VelocityRequestContextFactory requestContextFactory;
    protected final SimpleLinkManager linkManager;
    protected final JiraHelper jiraHelper;

    public AdminSummaryPanelContextProvider(VelocityRequestContextFactory requestContextFactory, SimpleLinkManager linkManager, JiraAuthenticationContext authenticationContext)
    {
        this.requestContextFactory = requestContextFactory;
        this.linkManager = linkManager;
        this.jiraHelper = new JiraHelper(ExecutingHttpRequest.get());
        this.authenticationContext = authenticationContext;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public MapBuilder<String, Object> getContextMap(String sectionId, Map<String, Object> context, String nameKey, List<SimpleLinkSection> pluginSections)
    {
        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);

        I18nHelper i18n = (I18nHelper) context.get("i18n");
        // Get the Plugin section
        contextMap.add(SECTIONS_KEY, pluginSections);
        contextMap.add(SECTION_NAME_KEY, i18n.getText(nameKey));
        contextMap.add(SECTION_ID_KEY, sectionId);
        contextMap.add(LINK_MANAGER_KEY, linkManager);
        contextMap.add(JIRA_HELPER_KEY, jiraHelper);
        contextMap.add(USER_KEY, authenticationContext.getLoggedInUser());
        contextMap.add("textutils", new TextUtils());

        return contextMap;
    }

    public String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }
}
