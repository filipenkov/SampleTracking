package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.core.util.Holder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0
 */
public class OAuthTokenRetriever
{
    private ConsumerService consumerService;
    private RequestFactory requestFactory;

    public OAuthTokenRetriever(ConsumerService consumerService, RequestFactory requestFactory)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
    }

    public ConsumerToken getRequestToken(ServiceProvider serviceProvider, final String consumerKey, String callback) throws ResponseException
    {
        final Request oAuthRequest = new Request(Request.HttpMethod.POST, serviceProvider.getRequestTokenUri(),
                Collections.<Request.Parameter>singleton(new Request.Parameter(OAuth.OAUTH_CALLBACK, callback)));
        final Request signedRequest = consumerService.sign(oAuthRequest, consumerKey, serviceProvider);
        final com.atlassian.sal.api.net.Request tokenRequest = requestFactory.createRequest(com.atlassian.sal.api.net.Request.MethodType.POST, serviceProvider.getRequestTokenUri().toString());
        tokenRequest.addRequestParameters(parameterToStringArray(signedRequest.getParameters()));

        final TokenAndSecret tokenAndSecret = requestToken(serviceProvider.getRequestTokenUri().toString(), signedRequest);
        final ConsumerToken requestToken = ConsumerToken.newRequestToken(tokenAndSecret.token).tokenSecret(tokenAndSecret.secret).consumer(getConsumer(consumerKey)).build();
        assert (requestToken.isRequestToken());
        return requestToken;
    }

    public ConsumerToken getAccessToken(ServiceProvider serviceProvider, String requestToken, String requestVerifier, final String consumerKey)
            throws ResponseException
    {
        final List<Request.Parameter> parameters = new ArrayList<Request.Parameter>();
        parameters.add(new Request.Parameter(OAuth.OAUTH_TOKEN, requestToken));
        if (StringUtils.isNotBlank(requestVerifier)) // Added in OAuth 1.0a
        {
            parameters.add(new Request.Parameter(OAuth.OAUTH_VERIFIER, requestVerifier));
        }
        final Request oAuthRequest = new Request(Request.HttpMethod.POST, serviceProvider.getAccessTokenUri(), parameters);
        final Request signedRequest = consumerService.sign(oAuthRequest, consumerKey, serviceProvider);
        final TokenAndSecret tokenAndSecret = requestToken(serviceProvider.getAccessTokenUri().toString(), signedRequest);
        ConsumerToken accessToken = ConsumerToken.newAccessToken(tokenAndSecret.token).tokenSecret(tokenAndSecret.secret).consumer(getConsumer(consumerKey)).build();
        assert (accessToken.isAccessToken());
        return accessToken;
    }

    private Consumer getConsumer(final String consumerKey)
    {
        return consumerService.getConsumerByKey(consumerKey) == null ? consumerService.getConsumer() : consumerService.getConsumerByKey(consumerKey);
    }

    private TokenAndSecret requestToken(String url, Request signedRequest) throws ResponseException
    {
        final com.atlassian.sal.api.net.Request tokenRequest = requestFactory.createRequest(com.atlassian.sal.api.net.Request.MethodType.POST, url);
        tokenRequest.addRequestParameters(parameterToStringArray(signedRequest.getParameters()));

        final Holder<Map<String, String>> oauthParametersHolder = new Holder<Map<String, String>>();

        final ResponseHandler<Response> responseHandler = new ResponseHandler<Response>()
        {
            public void handle(final Response response) throws ResponseException
            {
                if (response.isSuccessful())
                {
                    try
                    {
                        List<OAuth.Parameter> parameters = OAuth.decodeForm(response.getResponseBodyAsString());
                        Map<String, String> map = OAuth.newMap(parameters);
                        oauthParametersHolder.set(map);
                    }
                    catch (Exception e)
                    {
                        throw new ResponseException("Failed to get token from service provider. Couldn't parse response body " + response.getResponseBodyAsString() + "'", e);
                    }
                }
                else
                {
                    final String authHeader = response.getHeader("WWW-Authenticate");
                    if (authHeader != null && authHeader.startsWith("OAuth"))
                    {
                        final List<OAuth.Parameter> parameters = OAuthMessage.decodeAuthorization(authHeader);
                        String problem = "";
                        for (OAuth.Parameter parameter : parameters)
                        {
                            if (parameter.getKey().equals(OAuthProblemException.OAUTH_PROBLEM))
                            {
                                problem = parameter.getValue();
                            }
                        }
                        throw new ResponseException("Failed to get token from service provider, problem was: '" + problem + "', full details: " + authHeader);
                    }
                    else
                    {
                        throw new ResponseException("Failed to get token from service provider. Response status code is '" + response.getStatusCode() + "'");
                    }
                }
            }
        };
        tokenRequest.setFollowRedirects(false);
        tokenRequest.execute(responseHandler);
        final Map<String, String> oAuthParameterMap = oauthParametersHolder.get();

        final String secret = oAuthParameterMap.get(OAuth.OAUTH_TOKEN_SECRET);
        if (StringUtils.isEmpty(secret))
        {
            throw new ResponseException("Failed to get token from service provider. Secret is missing in response.");
        }
        final String token = oAuthParameterMap.get(OAuth.OAUTH_TOKEN);
        if (StringUtils.isEmpty(token))
        {
            throw new ResponseException("Failed to get token from service provider. Token is missing in response.");
        }
        TokenAndSecret tokenAndSecret = new TokenAndSecret();
        tokenAndSecret.secret = secret;
        tokenAndSecret.token = token;
        return tokenAndSecret;
    }

    private class TokenAndSecret
    {
        public String token;
        public String secret;
    }

    private String[] parameterToStringArray(Iterable<Request.Parameter> iterable)
    {
        List<String> list = new ArrayList<String>();
        for (Request.Parameter parameter : iterable)
        {
            list.add(parameter.getName());
            list.add(parameter.getValue());
        }
        return list.toArray(new String[] { });
    }

}