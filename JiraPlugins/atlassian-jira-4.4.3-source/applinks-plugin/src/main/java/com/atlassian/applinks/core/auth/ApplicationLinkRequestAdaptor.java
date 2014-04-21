package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFilePart;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.net.auth.Authenticator;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;

/**
 *
 * @since v3.0
 */
public class ApplicationLinkRequestAdaptor implements ApplicationLinkRequest
{
    private Request request;

    public ApplicationLinkRequestAdaptor(Request request)
    {
        this.request = Preconditions.checkNotNull(request, "request");
    }

    public ApplicationLinkRequest addAuthentication(Authenticator authenticator)
    {
        return setDelegate(request.addAuthentication(authenticator));
    }

    public ApplicationLinkRequest addBasicAuthentication(String username, String password)
    {
        return setDelegate(request.addBasicAuthentication(username, password));
    }

    public ApplicationLinkRequest addHeader(String headerName, String headerValue)
    {
        return setDelegate(request.addHeader(headerName, headerValue));
    }

    public ApplicationLinkRequest addRequestParameters(final String... params)
    {
        return setDelegate(request.addRequestParameters(params));
    }

    public ApplicationLinkRequest addSeraphAuthentication(final String username, final String password)
    {
        return setDelegate(request.addSeraphAuthentication(username, password));
    }

    public ApplicationLinkRequest addTrustedTokenAuthentication()
    {
        return setDelegate(request.addTrustedTokenAuthentication());
    }

    public ApplicationLinkRequest addTrustedTokenAuthentication(final String username)
    {
        return setDelegate(request.addTrustedTokenAuthentication(username));
    }

    public String execute()
            throws ResponseException
    {
        return request.execute();
    }

    public void execute(final ResponseHandler responseHandler)
            throws ResponseException
    {
        request.execute(responseHandler);
    }

    public <R> R executeAndReturn(final ReturningResponseHandler<Response, R> responseRETReturningResponseHandler)
            throws ResponseException {
        return (R) request.executeAndReturn(responseRETReturningResponseHandler);
    }

    public <R> R execute(final ApplicationLinkResponseHandler<R> applicationLinkResponseHandler)
            throws ResponseException
    {
        return (R) request.executeAndReturn(new ReturningResponseHandler<Response, R>()
        {
            public R handle(Response response) throws ResponseException {
                return applicationLinkResponseHandler.handle(response);
            }
        });
    }

    public Map<String, List<String>> getHeaders()
    {
        return request.getHeaders();
    }

    public ApplicationLinkRequest setConnectionTimeout(final int connectionTimeout)
    {
        return setDelegate(request.setConnectionTimeout(connectionTimeout));
    }

    public ApplicationLinkRequest setEntity(final Object entity)
    {
        return setDelegate(request.setEntity(entity));
    }

    public ApplicationLinkRequest setHeader(final String headerName, final String headerValue)
    {
        return setDelegate(request.setHeader(headerName, headerValue));
    }

    public ApplicationLinkRequest setRequestBody(String requestBody)
    {
        return setDelegate(request.setRequestBody(requestBody));
    }

    public ApplicationLinkRequest setFiles(List<RequestFilePart> files)
    {
        return setDelegate(request.setFiles(files));
    }

    public ApplicationLinkRequest setRequestContentType(String contentType)
    {
        return setDelegate(request.setRequestContentType(contentType));
    }

    public ApplicationLinkRequest setSoTimeout(int soTimeout)
    {
        return setDelegate(request.setSoTimeout(soTimeout));
    }

    public ApplicationLinkRequest setUrl(String url)
    {
        return setDelegate(request.setUrl(url));
    }

    private ApplicationLinkRequest setDelegate(Request request)
    {
        this.request = Preconditions.checkNotNull(request, "Method chaining response");
        return this;
    }

    public ApplicationLinkRequest setFollowRedirects(boolean follow)
    {
        return setDelegate(request.setFollowRedirects(follow));
    }
}
