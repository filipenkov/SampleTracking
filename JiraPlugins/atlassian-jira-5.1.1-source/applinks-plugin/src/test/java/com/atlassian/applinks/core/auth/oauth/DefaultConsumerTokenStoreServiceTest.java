package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.atlassian.oauth.util.RSAKeys;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultConsumerTokenStoreServiceTest extends TestCase
{
    private ConsumerTokenStoreService consumerTokenStoreService;
    private ConsumerTokenStore consumerTokenStore;
    private AuthenticationConfigurationManager authenticationConfigurationManager;
    private ConsumerService consumerService;

    public static final KeyPair KEYS;
    public static Consumer HOST_CONSUMER;

    static
    {
        try
        {
            KEYS = RSAKeys.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Before
    protected void setUp() throws Exception
    {
        HOST_CONSUMER = Consumer.key("host-consumer")
                        .name("Host Consumer")
                        .description("description")
                        .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                        .publicKey(KEYS.getPublic())
                        .callback(URI.create("http://consumer/host-callback")).build();

        super.setUp();
        consumerTokenStore = mock(ConsumerTokenStore.class);
        authenticationConfigurationManager = mock(AuthenticationConfigurationManager.class);
        consumerService = mock(ConsumerService.class);
        consumerTokenStoreService = new DefaultConsumerTokenStoreService(consumerTokenStore, authenticationConfigurationManager, consumerService);
    }

    @Test
    public void testAddConsumerToken() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);

        final ConsumerToken token = ConsumerToken.newAccessToken("abcdefghijklo").tokenSecret("secret").consumer(HOST_CONSUMER).build();

        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);
        when(consumerService.getConsumer()).thenReturn(HOST_CONSUMER);
        final String username = "bob";
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        consumerTokenStoreService.addConsumerToken(applicationLink, username, token);
        final ConsumerTokenStore.Key key = new ConsumerTokenStore.Key(applicationId.get() + ":" + username);
        Map<String, String> tokenProperties = new HashMap<String, String>();
        tokenProperties.put("applinks.oauth.applicationLinkId", applicationId.get());
        final ConsumerToken verifyToken = ConsumerToken.newAccessToken("abcdefghijklo").tokenSecret("secret").consumer(HOST_CONSUMER).properties(tokenProperties).build();
        verify(consumerTokenStore).put(eq(key), argThat(new IsSameConsumerToken(verifyToken)));
    }

    @Test
    public void testRemoveAllConsumerTokensUsingAtlassianAppAsServiceProvider() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);

        final Map<String, String> config = new HashMap<String, String>();
        when(authenticationConfigurationManager.getConfiguration(applicationId, OAuthAuthenticationProvider.class)).thenReturn(config);
        when(consumerService.getConsumer()).thenReturn(HOST_CONSUMER);
        Map<ConsumerTokenStore.Key, ConsumerToken> tokens = new HashMap<ConsumerTokenStore.Key, ConsumerToken>();
        final String username = "fred";
        Map<String, String> tokenProperties = new HashMap<String, String>();
        tokenProperties.put("applinks.oauth.applicationLinkId", applicationId.get());
        final ConsumerTokenStore.Key key1 = new ConsumerTokenStore.Key(applicationId.get() + ":" + username);
        final ConsumerToken token1 = ConsumerToken.newAccessToken("abcdefghijklo").tokenSecret("secret").consumer(HOST_CONSUMER).properties(tokenProperties).build();
        final ConsumerTokenStore.Key key2 = new ConsumerTokenStore.Key(new ApplicationId(UUID.randomUUID().toString()).get() + ":" + "fred");
        final ConsumerToken token2 = ConsumerToken.newAccessToken("xwoieurwopiulkjsf").tokenSecret("secret").consumer(HOST_CONSUMER).build();
        tokens.put(key1, token1);
        tokens.put(key2, token2);
        when(consumerTokenStore.getConsumerTokens(HOST_CONSUMER.getKey())).thenReturn(tokens);
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
        verify(consumerTokenStore).getConsumerTokens(eq(HOST_CONSUMER.getKey()));
        verify(consumerTokenStore).remove(eq(key1));
        verify(consumerTokenStore, never()).remove(eq(key2));
        verify(consumerTokenStore, never()).removeTokensForConsumer(anyString());
    }

    @Test
    public void testRemoveAllConsumerTokesnUsingNonAtlassianAppAsServiceProvider() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);
        final String consumerKey = "CustomConsumer";

        HOST_CONSUMER = Consumer.key(consumerKey)
                        .name("Host Consumer")
                        .description("description")
                        .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                        .publicKey(KEYS.getPublic())
                        .callback(URI.create("http://consumer/host-callback")).build();

        final Map<String, String> config = new HashMap<String, String>();
        config.put(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND, consumerKey);
        when(authenticationConfigurationManager.getConfiguration(applicationId, OAuthAuthenticationProvider.class)).thenReturn(config);
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
        verify(consumerTokenStore).removeTokensForConsumer(eq(consumerKey));
        verify(consumerTokenStore, never()).remove((ConsumerTokenStore.Key) anyObject());
    }

    @Test
    public void testGetConsumerToken() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        consumerTokenStoreService.getConsumerToken(applicationLink, "bob");
        verify(consumerTokenStore).get(eq(new ConsumerTokenStore.Key(applicationId.get() + ":" + "bob")));
    }

    @Test
    public void testRemoveConsumerToken() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        final ConsumerToken token = ConsumerToken.newAccessToken("abcdefghijklo").tokenSecret("secret").consumer(HOST_CONSUMER).build();
        final ConsumerTokenStore.Key key = DefaultConsumerTokenStoreService.makeOAuthApplinksConsumerKey("bob", applicationId.get());
        when(consumerTokenStore.get(key)).thenReturn(token);
        assertTrue(consumerTokenStoreService.removeConsumerToken(applicationId, "bob"));
        verify(consumerTokenStore).remove(key);
    }

    @Test
    public void testNoConsumerTokenToRemove() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());
        when(applicationLink.getId()).thenReturn(applicationId);
        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(true);
        final ConsumerTokenStore.Key key = DefaultConsumerTokenStoreService.makeOAuthApplinksConsumerKey("bob", applicationId.get());
        when(consumerTokenStore.get(key)).thenReturn(null);
        assertFalse(consumerTokenStoreService.removeConsumerToken(applicationId, "bob"));
    }

    @Test
    public void testOAuthNotEnabled() throws Exception
    {
        final ApplicationLink applicationLink = mock(ApplicationLink.class);
        final ApplicationId applicationId = new ApplicationId(UUID.randomUUID().toString());

        when(authenticationConfigurationManager.isConfigured(applicationId, OAuthAuthenticationProvider.class)).thenReturn(false);
        try
        {
            consumerTokenStoreService.getConsumerToken(applicationLink, "bob");
            fail("Should have thrown an exception, because OAuth is not enabled!");
        }
        catch(IllegalStateException ex)
        {
            assertEquals("OAuth not enabled for outgoing authentication!", ex.getMessage());
        }

        final ConsumerToken token = ConsumerToken.newAccessToken("abcdefghijklo").tokenSecret("secret").consumer(HOST_CONSUMER).build();
        try
        {
            consumerTokenStoreService.addConsumerToken(applicationLink, "bob", token);
            fail("Should have thrown an exception, because OAuth is not enabled!");
        }
        catch(IllegalStateException ex)
        {
            assertEquals("OAuth not enabled for outgoing authentication!", ex.getMessage());
        }

        try
        {
            consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
            fail("Should have thrown an exception, because OAuth is not enabled!");
        }
        catch(IllegalStateException ex)
        {
            assertEquals("OAuth not enabled for outgoing authentication!", ex.getMessage());
        }
    }

    class IsSameConsumerToken extends ArgumentMatcher<ConsumerToken>
    {
        private final ConsumerToken expectedToken;

        IsSameConsumerToken(ConsumerToken expectedToken)
        {
            this.expectedToken = expectedToken;
        }

        @Override
        public boolean matches(final Object o)
        {
            ConsumerToken token = (ConsumerToken) o;
            return (token.isAccessToken() == expectedToken.isAccessToken() &&
                    token.isRequestToken() == expectedToken.isRequestToken() &&
                    token.getProperties().equals(expectedToken.getProperties()) &&
                    token.getToken().equals(expectedToken.getToken()) &&
                    token.getTokenSecret().equals(expectedToken.getTokenSecret()) &&
                    token.getConsumer().getKey().equals(expectedToken.getConsumer().getKey()) &&
                    token.getConsumer().getName().equals(expectedToken.getConsumer().getName()) &&
                    token.getConsumer().getPublicKey().equals(expectedToken.getConsumer().getPublicKey()) &&
                    token.getConsumer().getSignatureMethod().equals(expectedToken.getConsumer().getSignatureMethod()) &&
                    token.getConsumer().getDescription().equals(expectedToken.getConsumer().getDescription())
            );
        }
    }

}
