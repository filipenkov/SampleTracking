package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.opensymphony.user.User;

import java.util.Map;

/**
 * Convenient abstraction for jira specific {@link Condition}'s. 
 */
public abstract class AbstractJiraCondition implements Condition
{
    public void init(Map params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(Map context)
    {
        User user = (User) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USER);
        if (user == null)
        {
            // cross product plugins may not have access to the user object jira uses and will only be able to put the
            // username into the context.  Need to do a lookup here.
            final String username = (String) context.get(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME);
            user = getUserUtil().getUser(username);
        }
        JiraHelper jiraHelper = (JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER);
        return shouldDisplay(user, jiraHelper);
    }

    UserUtil getUserUtil() 
    {
        return ComponentAccessor.getUserUtil();
    }

    public abstract boolean shouldDisplay(User user, JiraHelper jiraHelper);
}