package com.atlassian.gadgets.renderer.internal.oauth;

import com.atlassian.oauth.consumer.ConsumerService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.oauth.OAuthFetcher;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthFetcherFactory;

/**
 * Override the default Shindig {@link OAuthFetcherFactory} to return our own {@link AtlassianOAuthFetcher} instead
 * of the regular {@link OAuthFetcher}.
 */
@Singleton
public class AtlassianOAuthFetcherFactory extends OAuthFetcherFactory
{
    private final ConsumerService consumerService;

    @Inject
    protected AtlassianOAuthFetcherFactory(OAuthFetcherConfig fetcherConfig, ConsumerService consumerService)
    {
        super(fetcherConfig);
        this.consumerService = consumerService;
    }

    /**
     * Returns an {@link AtlassianOAuthFetcher}
     * 
     * @returns an {@link AtlassianOAuthFetcher}
     */
    @Override
    public OAuthFetcher getOAuthFetcher(HttpFetcher nextFetcher, HttpRequest request) throws GadgetException
    {
        return new AtlassianOAuthFetcher(consumerService, fetcherConfig, nextFetcher, request);
    }
}
