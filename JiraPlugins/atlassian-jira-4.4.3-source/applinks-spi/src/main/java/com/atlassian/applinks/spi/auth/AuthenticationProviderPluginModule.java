package com.atlassian.applinks.spi.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;

/**
 * This is the interface that must be implemented by the class that is used in
 * the authentication provider module descriptor. It's setters are used by the
 * module descriptor class to pass in the descriptor's configuration
 * properties.
 *
 * @since 3.0
 */
public interface AuthenticationProviderPluginModule
{
    /**
     * @param link an {@link ApplicationLink} to create an {@link AuthenticationProvider} for
     * @return an {@link AuthenticationProvider} instance configured for the specified {@link ApplicationLink}, or null
     *         if this {@link AuthenticationProvider} is not configured for the specified link.
     */
    AuthenticationProvider getAuthenticationProvider(ApplicationLink link);

    /**
     * Called by AppLinks when it renders the inbound and outbound authentication configuration. The returned url is absolute.
     *
     * If the returned URL points to the local configuration servlet, it should respect the request url of the incoming request.
     *
     * @param direction the authentication direction (inbound or outbound)
     * @param request   the incoming request
     * @return the url of the servlet used to configure the authenticator in the desired direction
     */
    String getConfigUrl(ApplicationLink link, Version applicationLinksVersion, AuthenticationDirection direction, final HttpServletRequest request);

    Class<? extends AuthenticationProvider> getAuthenticationProviderClass();
}
