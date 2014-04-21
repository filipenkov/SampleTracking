package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class DefaultConsumerTokenStoreService implements ConsumerTokenStoreService
{
    private final ConsumerTokenStore consumerTokenStore;
    private final AuthenticationConfigurationManager configurationManager;
    private final ConsumerService consumerService;
    private static final String APPLINKS_APPLICATION_LINK_ID = "applinks.oauth.applicationLinkId";

    public DefaultConsumerTokenStoreService(final ConsumerTokenStore consumerTokenStore,
                                            final AuthenticationConfigurationManager configurationManager,
                                            final ConsumerService consumerService)
    {
        this.consumerTokenStore = consumerTokenStore;
        this.configurationManager = configurationManager;
        this.consumerService = consumerService;
    }

    public void addConsumerToken(final ApplicationLink applicationLink, final String username, final ConsumerToken consumerToken)
    {
        checkNotNull(applicationLink, "applicationLink");
        checkNotNull(username, "username");
        checkNotNull(consumerToken, "consumerToken");
        verifyOAuthOutgoingEnabled(applicationLink.getId());

        final Map<String, String> tokenProperties = new HashMap<String, String>();
        tokenProperties.put(APPLINKS_APPLICATION_LINK_ID, applicationLink.getId().get());
        final ConsumerTokenStore.Key key = makeOAuthApplinksConsumerKey(username, applicationLink.getId().get());
        final ConsumerToken token = ConsumerToken.newAccessToken(consumerToken.getToken()).tokenSecret(consumerToken.getTokenSecret()).consumer(consumerToken.getConsumer()).properties(tokenProperties).build();
        consumerTokenStore.put(key, token);
    }

    public void removeAllConsumerTokens(final ApplicationLink applicationLink)
    {
        checkNotNull(applicationLink, "applicationLink");
        final Map<String, String> configuration = configurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
        verifyOAuthOutgoingEnabled(applicationLink.getId());

        if (configuration.containsKey(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND))
        {
            final String consumerKey = configuration.get(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND);
            consumerTokenStore.removeTokensForConsumer(consumerKey);
        }
        else
        {
            final String consumerKey = consumerService.getConsumer().getKey();
            final Map<ConsumerTokenStore.Key, ConsumerToken> consumerTokens = consumerTokenStore.getConsumerTokens(consumerKey);
            for (ConsumerTokenStore.Key key : consumerTokens.keySet())
            {
                final Map<String, String> tokenProperties = consumerTokens.get(key).getProperties();
                if (tokenProperties.containsKey(APPLINKS_APPLICATION_LINK_ID) && tokenProperties.get(APPLINKS_APPLICATION_LINK_ID).equals(applicationLink.getId().get()))
                {
                    consumerTokenStore.remove(key);
                }
            }
        }
    }

    public boolean removeConsumerToken(final ApplicationId applicationId, final String username)
    {
        checkNotNull(applicationId, "applicationLink");
        verifyOAuthOutgoingEnabled(applicationId);
        final ConsumerTokenStore.Key key = makeOAuthApplinksConsumerKey(username, applicationId.get());
        if (consumerTokenStore.get(key) != null) {
            consumerTokenStore.remove(key);
            return true;
        }
        return false;
    }

    public ConsumerToken getConsumerToken(final ApplicationLink applicationLink, final String username)
    {
        checkNotNull(username, "Username cannot be null!");
        checkNotNull(applicationLink, "Application Link cannot be null!");
        verifyOAuthOutgoingEnabled(applicationLink.getId());
        return consumerTokenStore.get(makeOAuthApplinksConsumerKey(username, applicationLink.getId().get()));
    }

    public static ConsumerTokenStore.Key makeOAuthApplinksConsumerKey(final String username, final String applicationLinkId)
    {
        checkNotNull(username, "Username cannot be null!");
        checkNotNull(applicationLinkId, "Application Link Id cannot be null!");
        return new ConsumerTokenStore.Key(applicationLinkId + ":" + username);
    }

    private void verifyOAuthOutgoingEnabled(final ApplicationId applicationId)
    {
        if (!configurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class))
        {
            throw new IllegalStateException("OAuth not enabled for outgoing authentication!");
        }
    }
}
