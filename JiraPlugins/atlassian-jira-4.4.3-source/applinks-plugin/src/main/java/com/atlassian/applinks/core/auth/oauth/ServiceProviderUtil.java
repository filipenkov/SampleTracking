package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.oauth.ServiceProvider;

import java.net.URI;
import java.util.Map;

import static com.atlassian.applinks.core.util.URIUtil.uncheckedConcatenate;

/**
 * Creates service provider URL for a service provider that has the atlassian oauth plugin installed.
 *
 * @since 3.0
 */
public class ServiceProviderUtil
{

    public static ServiceProvider getServiceProvider(final URI rpcUrl, final URI displayUrl)
    {
        final URI requestTokenUri = uncheckedConcatenate(rpcUrl, "/plugins/servlet/oauth/request-token");
        final URI authorizeTokenUri = uncheckedConcatenate(displayUrl, "/plugins/servlet/oauth/authorize");
        final URI accessTokenUri = uncheckedConcatenate(rpcUrl, "/plugins/servlet/oauth/access-token");
        return new ServiceProvider(requestTokenUri, authorizeTokenUri, accessTokenUri);
    }

    public static ServiceProvider getServiceProvider(Map<String, String> config, ApplicationLink applicationLink)
    {
        if (config.containsKey(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND))
        {
            final String accessTokenUrl = config.get(AddServiceProviderManuallyServlet.SERVICE_PROVIDER_ACCESS_TOKEN_URL);
            final String requestTokenUrl = config.get(AddServiceProviderManuallyServlet.SERVICE_PROVIDER_REQUEST_TOKEN_URL);
            final String authorizeUrl = config.get(AddServiceProviderManuallyServlet.SERVICE_PROVIDER_AUTHORIZE_URL);
            return new ServiceProvider(URI.create(requestTokenUrl), URI.create(authorizeUrl), URI.create(accessTokenUrl));
        }
        return ServiceProviderUtil.getServiceProvider(applicationLink.getRpcUrl(), applicationLink.getDisplayUrl());
    }

}