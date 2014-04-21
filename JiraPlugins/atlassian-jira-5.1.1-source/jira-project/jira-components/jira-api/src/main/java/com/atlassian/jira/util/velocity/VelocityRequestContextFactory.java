package com.atlassian.jira.util.velocity;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.InjectableComponent;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>Provides a request context that can be used to get the 'correct' baseurl.</p>
 *
 * <p>It will use information from the
 * HttpRequest if one was made or applicationProperties otherwise to determine the baseurl.</p>
 *
 * @since v4.0
 */
@PublicApi
@InjectableComponent
public interface VelocityRequestContextFactory
{
    /**
     * Get the request context.
     *
     * @return The request context.
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
     * Update the thread-local storage with the given velocityRequestContext.
     *
     * @param velocityRequestContext The velocity request context to store.
     * @since 4.3
     * @deprecated Use {@link #setVelocityRequestContext(VelocityRequestContext)} instead. Since v5.0.
     */
    void cacheVelocityRequestContext(final VelocityRequestContext velocityRequestContext);

    /**
     * Resets the thread local storage as if no request has occurred, effectively nulling out the current
     * thread local velocity request context.
     *
     * @since 4.3
     */
    void clearVelocityRequestContext();

    /**
     * Update the thread-local storage with the given request information.
     *
     * @param request The http request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(HttpServletRequest request);

    /**
     * Update the thread-local storage with the given request information.
     *
     * @param baseUrl of the request.
     * @param request The http request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(String baseUrl, HttpServletRequest request);

    /**
     * Update the thread-local storage with the given velocityRequestContext.
     *
     * @param velocityRequestContext The velocity request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(VelocityRequestContext velocityRequestContext);
}
