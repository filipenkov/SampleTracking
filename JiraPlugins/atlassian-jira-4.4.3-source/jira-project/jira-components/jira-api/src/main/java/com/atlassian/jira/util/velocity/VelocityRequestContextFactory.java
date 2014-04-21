package com.atlassian.jira.util.velocity;

import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Map;

/**
 * Provides a request context that can be used to get the 'correct' baseurl.  It will use information from the
 * HttpRequest if one was made or applicationProperties otherwise to determine the baseurl.
 *
 * @since v4.0
 */
public interface VelocityRequestContextFactory
{
    /**
     * Get the request context.
     *
     * @return The request context
     */
    VelocityRequestContext getJiraVelocityRequestContext();

    /**
     * Constructs a map with a number of common parameters used by velocity templates.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, JiraAuthenticationContext authenticationContext);

    /**
     * Update the threadlocal storage with the given velocityRequestContext.
     *
     * @param velocityRequestContext The velocity request context to store
     * @since 4.3
     */
    void cacheVelocityRequestContext(final VelocityRequestContext velocityRequestContext);

    /**
     * Resets the thread local storage as if no request has occurred, effectively nulling out the current
     * thread local velocity request context.
     *
     * @since 4.3
     */
    void clearVelocityRequestContext();
}
