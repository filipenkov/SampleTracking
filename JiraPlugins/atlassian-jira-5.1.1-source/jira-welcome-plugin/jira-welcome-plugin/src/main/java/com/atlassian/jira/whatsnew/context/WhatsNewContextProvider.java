package com.atlassian.jira.whatsnew.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Map;

/**
 * Provides velocity context for the what's new web-panel.
 *
 * @since v1.0
 */
public class WhatsNewContextProvider implements ContextProvider
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;

    public WhatsNewContextProvider(final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext)
    {
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);
        paramsBuilder.add("appVersion", applicationProperties.getVersion());
        paramsBuilder.add("whatsnewFullLink", HelpUtil.getInstance().getHelpPath("whatsnew_iframe_link").getUrl());
        User loggedInUser = authenticationContext.getLoggedInUser();
        paramsBuilder.add("remoteUser", loggedInUser == null ? "" : loggedInUser.getName());
        return paramsBuilder.toMap();
    }
}
