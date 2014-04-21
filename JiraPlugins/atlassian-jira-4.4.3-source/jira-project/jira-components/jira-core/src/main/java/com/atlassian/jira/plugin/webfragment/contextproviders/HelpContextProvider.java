package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.HelpUtil;
import com.opensymphony.user.User;

import java.util.Map;

public class HelpContextProvider extends AbstractJiraContextProvider
{
    private final JiraAuthenticationContext authenticationContext;

    public HelpContextProvider(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public Map getContextMap(User user, JiraHelper jiraHelper)
    {
        return EasyMap.build("i18n", authenticationContext.getI18nHelper(),
                             "helpUtil", HelpUtil.getInstance());
    }
}
