package com.atlassian.gadgets.renderer.internal.oauth;

import java.net.URI;
import java.util.List;

import com.atlassian.oauth.Request;
import com.atlassian.oauth.Request.HttpMethod;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.bridge.Requests;
import com.atlassian.oauth.bridge.ServiceProviders;
import com.atlassian.oauth.bridge.consumer.ConsumerTokens;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;

import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.oauth.OAuthFetcher;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;

import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

public class AtlassianOAuthFetcher extends OAuthFetcher
{
    private final ConsumerService consumerService;

    public AtlassianOAuthFetcher(ConsumerService consumerService, 
            OAuthFetcherConfig fetcherConfig, 
            HttpFetcher nextFetcher, 
            HttpRequest request)
    {
        super(fetcherConfig, nextFetcher);
        this.consumerService = consumerService;
    }

    @Override
    protected OAuthMessage sign(OAuthAccessor accessor, String httpMethod, String uri, List<Parameter> params)
            throws OAuthException
    {
        Request request = new Request(
            HttpMethod.valueOf(httpMethod.toUpperCase()),
            URI.create(uri),
            Requests.fromOAuthParameters(params)
        );
        
        ServiceProvider serviceProvider = ServiceProviders.fromOAuthServiceProvider(accessor.consumer.serviceProvider);
        if (accessor.requestToken != null || accessor.accessToken != null)
        {
            ConsumerToken token = ConsumerTokens.asConsumerToken(accessor);
            return Requests.asOAuthMessage(consumerService.sign(request, serviceProvider, token));
        }
        else
        {
            return Requests.asOAuthMessage(consumerService.sign(request, accessor.consumer.consumerKey, serviceProvider));
        }
    }    
}
