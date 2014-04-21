package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestAdaptor;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFilePart;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.net.auth.Authenticator;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0
 */
public class OAuthRequest implements ApplicationLinkRequest
{

    private String url;
    private final MethodType methodType;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final ApplicationId applicationId;
    private final String username;
    private final ApplicationLinkRequest wrappedRequest;
    private final ServiceProvider serviceProvider;
    private final ConsumerService consumerService;
    private final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private final ConsumerToken consumerToken;
    private static final Logger log = LoggerFactory.getLogger(OAuthRequest.class);
    private boolean followRedirects = true;
    private int redirects = 0;

    public OAuthRequest(
            final String url,
            final MethodType methodType,
            final Request wrappedRequest,
            final ServiceProvider serviceProvider,
            final ConsumerService consumerService,
            final ConsumerToken consumerToken,
            final ConsumerTokenStoreService consumerTokenStoreService,
            final ApplicationId applicationId,
            final String username)
    {
        this.url = url;
        this.methodType = methodType;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.applicationId = applicationId;
        this.username = username;
        this.wrappedRequest = new ApplicationLinkRequestAdaptor(wrappedRequest);
        this.wrappedRequest.setFollowRedirects(false);
        this.serviceProvider = serviceProvider;
        this.consumerService = consumerService;
        this.consumerToken = consumerToken;
    }

    public OAuthRequest setConnectionTimeout(final int i)
    {
        wrappedRequest.setConnectionTimeout(i);
        return this;
    }

    public OAuthRequest setSoTimeout(final int i)
    {
        wrappedRequest.setSoTimeout(i);
        return this;
    }

    public OAuthRequest setUrl(final String s)
    {
        this.url = s;
        wrappedRequest.setUrl(s);
        return this;
    }

    public OAuthRequest setRequestBody(final String s)
    {
        wrappedRequest.setRequestBody(s);
        return this;
    }

    public ApplicationLinkRequest setFiles(List<RequestFilePart> files)
    {
        wrappedRequest.setFiles(files);
        return this;
    }

    public OAuthRequest setEntity(final Object o)
    {
        wrappedRequest.setEntity(o);
        return this;
    }

    public OAuthRequest setRequestContentType(final String s)
    {
        wrappedRequest.setRequestContentType(s);
        return this;
    }

    public OAuthRequest addRequestParameters(final String... params)
    {
        wrappedRequest.addRequestParameters(params);
        for (int i = 0; i < params.length; i += 2)
        {
            final String name = params[i];
            final String value = params[i + 1];
            List<String> list = parameters.get(name);
            if (list == null)
            {
                list = new ArrayList<String>();
                parameters.put(name, list);
            }
            list.add(value);
        }
        return this;
    }

    public OAuthRequest addAuthentication(final Authenticator authenticator)
    {
        wrappedRequest.addAuthentication(authenticator);
        return this;
    }

    public OAuthRequest addTrustedTokenAuthentication()
    {
        wrappedRequest.addTrustedTokenAuthentication();
        return this;
    }

    public OAuthRequest addTrustedTokenAuthentication(final String s)
    {
        wrappedRequest.addTrustedTokenAuthentication(s);
        return this;
    }

    public OAuthRequest addBasicAuthentication(final String s, final String s1)
    {
        wrappedRequest.addBasicAuthentication(s, s1);
        return this;
    }

    public OAuthRequest addSeraphAuthentication(final String s, final String s1)
    {
        wrappedRequest.addSeraphAuthentication(s, s1);
        return this;
    }

    public OAuthRequest addHeader(final String s, final String s1)
    {
        wrappedRequest.addHeader(s, s1);
        return this;
    }

    public OAuthRequest setHeader(final String s, final String s1)
    {
        wrappedRequest.setHeader(s, s1);
        return this;
    }

    public Map<String, List<String>> getHeaders()
    {
        return wrappedRequest.getHeaders();
    }

    public void execute(final ResponseHandler responseHandler) throws ResponseException
    {
        signRequest();
        wrappedRequest.execute(responseHandler);
    }

    public <R> R executeAndReturn(final ReturningResponseHandler<Response, R> responseRETReturningResponseHandler)
            throws ResponseException {
        signRequest();
        return wrappedRequest.executeAndReturn(responseRETReturningResponseHandler);
    }

    public <R> R execute(final ApplicationLinkResponseHandler<R> applicationLinkResponseHandler)
            throws ResponseException
    {
        signRequest();
        return wrappedRequest.execute(new OAuthApplinksResponseHandler<R>(applicationLinkResponseHandler, consumerTokenStoreService, this, applicationId, username, followRedirects));
    }

    public String execute() throws ResponseException
    {
        signRequest();
        return wrappedRequest.execute();
    }

    private void signRequest() throws ResponseException
    {
        final com.atlassian.oauth.Request oAuthRequest = new com.atlassian.oauth.Request(convertHttpMethod(methodType), URI.create(url), convertParameters(consumerToken.getToken()));
        final com.atlassian.oauth.Request signedRequest = consumerService.sign(oAuthRequest, serviceProvider, consumerToken);
        final OAuthMessage oAuthMessage = OAuthHelper.asOAuthMessage(signedRequest);
        try
        {
            wrappedRequest.setHeader("Authorization", oAuthMessage.getAuthorizationHeader(null));
        }
        catch (IOException e)
        {
            throw new ResponseException("Unable to generate OAuth Authorization request header.", e);
        }
    }

    private List<com.atlassian.oauth.Request.Parameter> convertParameters(final String accesstoken)
    {
        final List<com.atlassian.oauth.Request.Parameter> parameters = new ArrayList<com.atlassian.oauth.Request.Parameter>();
        parameters.add(new com.atlassian.oauth.Request.Parameter(OAuth.OAUTH_TOKEN, accesstoken));
        for (final String parameterName : this.parameters.keySet())
        {
            final List<String> values = this.parameters.get(parameterName);
            for (final String value : values)
            {
                parameters.add(new com.atlassian.oauth.Request.Parameter(parameterName, value));
            }
        }
        return parameters;
    }

    private com.atlassian.oauth.Request.HttpMethod convertHttpMethod(final MethodType methodType)
    {
        com.atlassian.oauth.Request.HttpMethod method = com.atlassian.oauth.Request.HttpMethod.GET;

        if (methodType == MethodType.GET)
        {
            method = com.atlassian.oauth.Request.HttpMethod.GET;
        }else if (methodType == MethodType.POST)
        {
            method = com.atlassian.oauth.Request.HttpMethod.POST;
        }else if (methodType == MethodType.PUT)
        {
            method = com.atlassian.oauth.Request.HttpMethod.PUT;
        }else if (methodType == MethodType.DELETE)
        {
            method = com.atlassian.oauth.Request.HttpMethod.DELETE;
        }
        return method;
    }

    protected int getRedirects()
    {
        return redirects;
    }

    protected void setRedirects(int redirects)
    {
        this.redirects = redirects;
    }


    public ApplicationLinkRequest setFollowRedirects(boolean followRedirects)
    {
        this.followRedirects = followRedirects;
        return this;
    }

}