package com.atlassian.applinks.api.auth;

import com.atlassian.applinks.api.ApplicationLinkRequestFactory;

/**
 * TODO: Document this class / interface here
 *
 * @since 3.0
 */
public interface NonImpersonatingAuthenticationProvider extends AuthenticationProvider
{
    ApplicationLinkRequestFactory getRequestFactory();
}
