package com.atlassian.jira.dev.reference.plugin.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.opensymphony.user.User;

import java.util.Map;

public class ReferenceContextProvider extends AbstractJiraContextProvider
{
    public Map getContextMap(User user, JiraHelper jiraHelper)
    {
        return EasyMap.build("test", "reloaded");
    }

}
