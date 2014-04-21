package com.atlassian.jira.plugin.issuenav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * A condition that checks if a tip is suppressed for the authenticated user.
 *
 * The tip's key must be provided as a parameter named "tipKey".
 */
public class TipSuppressedCondition implements Condition
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SuppressedTipsManager suppressedTipsManager;
    private String tipKey;

    public TipSuppressedCondition(JiraAuthenticationContext jiraAuthenticationContext, UserPropertyManager userPropertyManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;

        // TODO: This really should be injected, but conditions are created in a DI container that doesn't contain
        // plugin components. The underlying cause of this problem is fixed in 5.1, so we can remove this hack then.
        this.suppressedTipsManager = new SuppressedTipsManager(userPropertyManager);
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
        tipKey = params.get("tipKey");
    }

    public boolean shouldDisplay(Map<String, Object> context)
    {
        try
        {
            User user = jiraAuthenticationContext.getLoggedInUser();
            return suppressedTipsManager.isSuppressed(tipKey, user);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}