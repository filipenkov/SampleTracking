package com.atlassian.applinks.core.auth.trusted;

import java.net.URI;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestAdaptor;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.request.TrustedRequest;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @since v3.0
 */
class TrustedApplicationsRequestFactory implements ApplicationLinkRequestFactory
{
    private final String username;
    private final CurrentApplication currentApplication;
    private final RequestFactory requestFactory;

    public TrustedApplicationsRequestFactory(final CurrentApplication currentApplication,
                                             final RequestFactory requestFactory,
                                             final String username)
    {
        this.currentApplication = checkNotNull(currentApplication, "currentApplication");
        this.requestFactory = checkNotNull(requestFactory, "requestFactory");
        this.username = checkNotNull(username, "username");
    }

    public ApplicationLinkRequest createRequest(Request.MethodType methodType, String url)
            throws CredentialsRequiredException
    {
        final ApplicationLinkRequest request = new ApplicationLinkRequestAdaptor(
                requestFactory.createRequest(methodType, url));
        TrustedApplicationUtils.addRequestParameters(currentApplication.encode(username, url),
                new TrustedRequest()
                {
                    public void addRequestParameter(String name, String value)
                    {
                        request.addHeader(name, value);
                    }
                });
        return request;
    }

    public URI getAuthorisationURI(URI callback)
    {
        return null;
    }

    public URI getAuthorisationURI()
    {
        return null;
    }
}
