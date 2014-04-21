package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;

import java.util.Map;

/**
 * Context provider for
 *
 * @since v4.4
 */
public class ComponentsContextProvider implements CacheableContextProvider
{
    private final ContextProviderUtils utils;
    private final UserManager userManager;
    private final UserPickerSearchService userPickerSearchService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ComponentsContextProvider(ContextProviderUtils utils, UserManager userManager, UserPickerSearchService userPickerSearchService, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.utils = utils;
        this.userManager = userManager;
        this.userPickerSearchService = userPickerSearchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public void init(Map<String, String> stringStringMap) throws PluginParseException
    {
        //Nothing to do.
    }

    public Map<String, Object> getContextMap(Map<String, Object> params)
    {

        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser(),
                new SimpleErrorCollection());

        return MapBuilder.newBuilder(params)
                .add("isUserPickerDisabled", !userPickerSearchService.canPerformAjaxSearch(jiraServiceContext))
                .add("projectLeadAssignee", getProjectLeadAssigneeDisplayName())
                .add("isDefaultAssigneeProjectLead", isDefaultAssigneeProjectLead())
                .toMap();
    }

    private String getProjectLeadAssigneeDisplayName()
    {
        final String leadUser = utils.getProject().getLeadUserName();
        final User user = userManager.getUserEvenWhenUnknown(leadUser);
        return user.getDisplayName();
    }

    private boolean isDefaultAssigneeProjectLead()
    {
        return utils.getProject().getAssigneeType() == null || utils.getProject().getAssigneeType() == AssigneeTypes.PROJECT_LEAD;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
