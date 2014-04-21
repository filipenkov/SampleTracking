package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.opensymphony.user.User;

import java.util.Map;

public abstract class AbstractJiraContextProvider implements ContextProvider
{
    public void init(Map params) throws PluginParseException
    {
    }

    public Map getContextMap(Map context)
    {
        User user = (User) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USER);
        JiraHelper jiraHelper = (JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        return getContextMap(user, jiraHelper);
    }

    public abstract Map getContextMap(User user, JiraHelper jiraHelper);
}
