package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;

import java.util.Collections;
import java.util.Map;

/**
 * @since v4.4
 */
public class DefaultVelocityContextFactory implements VelocityContextFactory
{
    private final JiraAuthenticationContext authenticationContext;

    public DefaultVelocityContextFactory(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public Map<String, Object> createVelocityContext(Map<String, Object> initContext)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(initContext, authenticationContext);
    }

    public Map<String, Object> createDefaultVelocityContext()
    {
        return createVelocityContext(Collections.<String, Object>emptyMap());
    }
}
