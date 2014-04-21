package com.atlassian.applinks.api;

import java.net.URI;

import com.atlassian.applinks.api.auth.AuthenticationProvider;

/**
 * Used to generate URIs for users to view or revoke their credentials for an authorised
 * application link. This is only implemented by an {@link ApplicationLinkRequestFactory}
 * that supports per-user authorisation, e.g. OAuth.
 *
 * @since 3.6
 */
public interface AuthorisationAdminURIGenerator
{
    /**
     * <p>
     * The URI that can be used to view or revoke authentication for the requested
     * resource.  For OAuth links, this corresponds to the "OAuth Access Tokens" page
     * in the remote application.
     * </p>
     */
    URI getAuthorisationAdminURI();
}
