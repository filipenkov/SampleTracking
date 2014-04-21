package com.atlassian.applinks.api.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.Request;

/**
 * This provider performs no authentication. It provides a convenience method for creating anonymous requests without
 * having to catch {@link CredentialsRequiredException} as it is never thrown for unauthenticated requests.
 *
 * @since 3.0
 */
public final class Anonymous implements AuthenticationProvider
{
    /**
     * This is a convenience method for creating anonymous (unauthenticated) requests without having to catch the
     * {@link CredentialsRequiredException} declared on the {@link ApplicationLinkRequestFactory} interface
     *
     * @param link       the {@link ApplicationLink} that is the target of the request
     * @param methodType The HTTP method type
     * @param url        The url to request
     * @return the (unauthenticated) request object
     * @see ApplicationLinkRequestFactory#createRequest
     */
    public static Request createAnonymousRequest(final ApplicationLink link, final Request.MethodType methodType, final String url)
    {
        try
        {
            return link.createAuthenticatedRequestFactory(Anonymous.class).createRequest(methodType, url);
        }
        catch (CredentialsRequiredException e)
        {
            throw new RuntimeException("Unexpected CredentialsRequiredException encountered for Anonymous AuthenticationProvider", e);
        }
    }

}
