package com.atlassian.applinks.api.auth.types;

import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;

/**
 * Pass this type to the {@link com.atlassian.applinks.api.ApplicationLink#createAuthenticatedRequestFactory(Class)}
 * method to obtain a {@link com.atlassian.sal.api.net.RequestFactory} instance
 * that uses the Trusted Apps protocol for transparent authentication.
 *
 * @see com.atlassian.applinks.api.ApplicationLink#createAuthenticatedRequestFactory(Class)
 * @since 3.0
 */
public interface TrustedAppsAuthenticationProvider extends ImpersonatingAuthenticationProvider
{
}
