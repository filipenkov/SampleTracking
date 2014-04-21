package com.atlassian.applinks.spi.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.sal.api.net.RequestFactory;

/**
 * Authentication provider modules implementing this interface will be exposed to the Create Application Link  Wizard,
 * and considered for initialisation during the link creation process.
 *
 * @since 3.0
 */
public interface AutoConfiguringAuthenticatorProviderPluginModule extends AuthenticationProviderPluginModule
{
    /**
     * @param authenticationScenario descriptive of the relationship between the local server and the target
     *                               {@link ApplicationLink}
     * @param applicationLink        the {@link ApplicationLink} that will be the target of this authentication provider
     *                               configuration
     * @return true if this authentication provider is appropriate to the supplied {@link AuthenticationScenario} for
     *         the specified {@link ApplicationLink}
     */
    boolean isApplicable(AuthenticationScenario authenticationScenario, ApplicationLink applicationLink);

    /**
     * Initialises a working reciprocal authentication relationship with the target {@link ApplicationLink} using this
     * authentication provider.
     *
     * @param authenticatedRequestFactory a {@link RequestFactory} initialised with the base url of the target
     *                                    application (see {@link ApplicationLink#createAuthenticatedRequestFactory()} for details) and authenticated with
     *                                    an administrators credentials in the remote application (using HTTP Basic authentication).
     * @param applicationLink             the {@link ApplicationLink} that will be the target of this authentication provider
     *                                    configuration
     * @throws AuthenticationConfigurationException
     *          if a problem was encountered initialising the authentication
     *          relationship between the local and remote application
     */
    void enable(RequestFactory authenticatedRequestFactory, ApplicationLink applicationLink)
            throws AuthenticationConfigurationException;

    /**
     * Disables the reciprocal authentication relationship with the target {@link ApplicationLink}.
     *
     * @param authenticatedRequestFactory a {@link com.atlassian.sal.api.net.RequestFactory} initialised with the base url of the target
     *                                    application (see {@link com.atlassian.applinks.api.ApplicationLink#createAuthenticatedRequestFactory()} for details) and authenticated with
     *                                    an administrators credentials in the remote application (using HTTP Basic authentication).
     * @param applicationLink             the {@link com.atlassian.applinks.api.ApplicationLink} that will be the target of this authentication provider
     *                                    configuration change.
     * disabled.
     * @throws AuthenticationConfigurationException
     *          if a problem was encountered initialising the authentication
     *          relationship between the local and remote application
     */
    void disable(RequestFactory authenticatedRequestFactory, ApplicationLink applicationLink)
            throws AuthenticationConfigurationException;

}
