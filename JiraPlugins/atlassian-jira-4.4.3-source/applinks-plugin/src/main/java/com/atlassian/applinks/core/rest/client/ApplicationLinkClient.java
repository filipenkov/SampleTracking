package com.atlassian.applinks.core.rest.client;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.core.rest.ApplicationLinkResource;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

import java.net.URI;

public class ApplicationLinkClient
{
    private final InternalHostApplication internalHostApplication;
    private final RestUrlBuilder restUrlBuilder;

    public ApplicationLinkClient(final InternalHostApplication internalHostApplication,
                                 final RestUrlBuilder restUrlBuilder)
    {
        this.internalHostApplication = internalHostApplication;
        this.restUrlBuilder = restUrlBuilder;
    }

    public void deleteReciprocalLinkFrom(final ApplicationLink link) throws ReciprocalActionException, CredentialsRequiredException
    {
        final URI baseUri = RestUtil.getBaseRestUri(link);

        final String url;

        try
        {
            url = restUrlBuilder.getUrlFor(baseUri, ApplicationLinkResource.class)
                    .deleteApplicationLink(internalHostApplication.getId().get(), false).toString();
        }
        catch (final TypeNotInstalledException e)
        {
            // should never happen, RestUrlBuilder stubs the Resource class
            throw new RuntimeException(e);
        }

        final ApplicationLinkRequest deleteReciprocalLinkRequest = link.createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.DELETE, url);

        //Indicates that a CredentialsRequiredException from the remote end was thrown and
        //we were not able to authenticate.
        try
        {
            final boolean credentialsRequired = deleteReciprocalLinkRequest.execute(new ApplicationLinkResponseHandler<Boolean>()
            {
                public Boolean handle(final Response response) throws ResponseException
                {
                    if (response.getStatusCode() == 200)
                    {
                        // deleted reciprocal link, continue
                        return false;
                    }
                    else
                    {
                        throw new ResponseException(String.format("Received %s - %s",
                                response.getStatusCode(),
                                response.getStatusText()
                        ));
                    }
                }

                public Boolean credentialsRequired(final Response response) throws ResponseException
                {
                    return true;
                }
            });
            if (credentialsRequired)
            {
                throw new CredentialsRequiredException(link.createAuthenticatedRequestFactory(), "Authentication not attempted as credentials are required.");
            }
        }
        catch (ResponseException e)
        {
            throw new ReciprocalActionException(e);
        }
    }
}
