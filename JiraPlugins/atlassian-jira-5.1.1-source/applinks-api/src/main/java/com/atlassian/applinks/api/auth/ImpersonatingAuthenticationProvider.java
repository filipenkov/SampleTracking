package com.atlassian.applinks.api.auth;

import com.atlassian.applinks.api.ApplicationLinkRequestFactory;

/**
 * Impersonating authentication providers allow to specify a user, on his behalf the request
 * to the remote application is made. The remote application will only return content / information that is visible
 * and accessible for this user.
 *
 * @since 3.0
 */
public interface ImpersonatingAuthenticationProvider extends AuthenticationProvider
{
    ApplicationLinkRequestFactory getRequestFactory(String username);
}
