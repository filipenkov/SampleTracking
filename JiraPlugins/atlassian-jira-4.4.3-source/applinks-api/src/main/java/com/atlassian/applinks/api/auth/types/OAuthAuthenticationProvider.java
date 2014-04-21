package com.atlassian.applinks.api.auth.types;

import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;

/**
 * The OAuth Authentication provider indicating that OAuth is used to authenticate to the remote application.
 *
 * Pass this type to the {@link com.atlassian.applinks.api.ApplicationLink#createAuthenticatedRequestFactory(Class)}
 * method to obtain a {@link com.atlassian.sal.api.net.RequestFactory} instance
 * that uses oauth authentication for authentication.
 *
 * @since 3.0
 */
public interface OAuthAuthenticationProvider extends ImpersonatingAuthenticationProvider
{
}
