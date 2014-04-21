package com.atlassian.gadgets.renderer.internal.guice;

import com.atlassian.gadgets.renderer.internal.AtlassianOAuthCallbackGenerator;
import com.atlassian.gadgets.renderer.internal.oauth.AtlassianOAuthFetcherFactory;
import com.atlassian.gadgets.renderer.internal.oauth.AtlassianOAuthStore;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerTokenStore;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.gadgets.oauth.OAuthCallbackGenerator;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthFetcherFactory;
import org.apache.shindig.gadgets.oauth.OAuthModule.OAuthCrypterProvider;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.apache.shindig.gadgets.servlet.AuthenticationModule;

/**
 * A {@link Module} to configure Shindigs authentication.  Most importantly, it binds ours own custom
 * {@link OAuthStore} and {@link OAuthFetcherFactory}.
 */
public class AuthModule extends AbstractModule
{
    private final ConsumerService consumerService;
    private final ConsumerTokenStore tokenStore;

    public AuthModule(ConsumerService consumerService, ConsumerTokenStore tokenStore)
    {
        this.consumerService = consumerService;
        this.tokenStore = tokenStore;
    }
    
    @Override
    protected void configure()
    {
        bind(Boolean.class)
            .annotatedWith(Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED))
            .toInstance(Boolean.TRUE);
        install(new AuthenticationModule());

        // Used for encrypting client-side OAuth state.
        bind(BlobCrypter.class)
            .annotatedWith(Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER))
            .toProvider(OAuthCrypterProvider.class);

        // Used for persistent storage of OAuth access tokens.
        bind(OAuthStore.class).to(AtlassianOAuthStore.class);
        
        // Override OAuthFetchFactory with our own implementation
        bind(OAuthFetcherFactory.class).to(AtlassianOAuthFetcherFactory.class);
        
        bind(ConsumerService.class).toInstance(consumerService);
        bind(ConsumerTokenStore.class).toInstance(tokenStore);
        
        bind(OAuthCallbackGenerator.class).to(AtlassianOAuthCallbackGenerator.class);
    }
}
