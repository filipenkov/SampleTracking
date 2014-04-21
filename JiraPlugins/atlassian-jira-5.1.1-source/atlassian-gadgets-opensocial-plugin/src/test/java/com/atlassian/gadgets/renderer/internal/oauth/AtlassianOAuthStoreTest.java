package com.atlassian.gadgets.renderer.internal.oauth;

import java.net.URI;

import com.atlassian.gadgets.renderer.internal.oauth.AtlassianOAuthStore.TokenSessionProperties;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Consumer.SignatureMethod;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.Token;
import com.atlassian.oauth.bridge.Consumers;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.atlassian.oauth.consumer.ConsumerTokenStore.Key;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.collect.ImmutableMap;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.apache.shindig.gadgets.oauth.OAuthStore.ConsumerInfo;
import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.hamcrest.DeepIsEqual.deeplyEqualTo;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AtlassianOAuthStoreTest
{
    private static final SecurityToken EMPTY_SECURITY_TOKEN = new SecurityTokenBuilder().build();
    private static final ConsumerTokenStore.Key EMPTY_SECURITY_TOKEN_KEY = new ConsumerTokenStore.Key(":::");
    private static final Consumer CONSUMER = Consumer.key("consumer key")
        .callback(URI.create("http://consumer/callback"))
        .name("Test Consumer")
        .signatureMethod(SignatureMethod.HMAC_SHA1)
        .build();
    private static final ServiceProvider SERVICE_PROVIDER = new ServiceProvider(
        URI.create("http://serviceprovider/request-token"),
        URI.create("http://serviceprovider/authorization"),
        URI.create("http://serviceprovider/access-token")
    );
    private static final ConsumerInfo CONSUMER_INFO =
        new ConsumerInfo(Consumers.asOAuthConsumer(CONSUMER, SERVICE_PROVIDER), "consumer", "http://gadgets/callback");

    private static final String SESSION_HANDLE = "session";
    private static final long TOKEN_EXPIRE_MILLIS = 3600000;

    @Mock ConsumerService consumerService;
    @Mock ConsumerTokenStore tokenStore;
    @Mock ApplicationProperties applicationProperties;

    OAuthStore store;

    @Before
    public void setUp()
    {
        store = new AtlassianOAuthStore(consumerService, tokenStore, applicationProperties);
    }

    @Test
    public void verifyThatConsumerInfoIsUsedWhenSettingSecurityToken() throws Exception
    {
        TokenInfo tokenInfo = new TokenInfo("token", "secret", SESSION_HANDLE, TOKEN_EXPIRE_MILLIS);
        ConsumerToken token = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .properties(ImmutableMap.of(
                TokenSessionProperties.SESSION_HANDLE, SESSION_HANDLE,
                TokenSessionProperties.TOKEN_EXPIRE_MILLIS, Long.toString(TOKEN_EXPIRE_MILLIS))
            )
            .build();
        when(tokenStore.put(eq(EMPTY_SECURITY_TOKEN_KEY), (ConsumerToken) argThat(is(deeplyEqualTo(token))))).thenReturn(token);
        
        store.setTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "", tokenInfo);

        verify(tokenStore).put(eq(EMPTY_SECURITY_TOKEN_KEY), argThat(hasEqualConsumer(token)));
        verifyZeroInteractions(consumerService);
    }

    @Test
    public void verifyThatSetTokenInfoStoresSessionName() throws Exception
    {
        TokenInfo tokenInfo = new TokenInfo("token", "secret", SESSION_HANDLE, TOKEN_EXPIRE_MILLIS);
        ConsumerToken token = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .properties(ImmutableMap.of(
                TokenSessionProperties.SESSION_HANDLE, SESSION_HANDLE,
                TokenSessionProperties.TOKEN_EXPIRE_MILLIS, Long.toString(TOKEN_EXPIRE_MILLIS))
            )
            .build();
        when(tokenStore.put(eq(EMPTY_SECURITY_TOKEN_KEY), (ConsumerToken) argThat(is(deeplyEqualTo(token))))).thenReturn(token);

        store.setTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "", tokenInfo);

        verify(tokenStore).put(
            eq(EMPTY_SECURITY_TOKEN_KEY),
            argThat(hasProperty(AtlassianOAuthStore.TokenSessionProperties.SESSION_HANDLE).withValue(SESSION_HANDLE))
        );
    }

    @Test
    public void verifyThatSetTokenInfoStoresTokenExpireMills() throws Exception
    {
        TokenInfo tokenInfo = new TokenInfo("token", "secret", SESSION_HANDLE, TOKEN_EXPIRE_MILLIS);
        ConsumerToken token = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .properties(ImmutableMap.of(
                TokenSessionProperties.SESSION_HANDLE, SESSION_HANDLE,
                TokenSessionProperties.TOKEN_EXPIRE_MILLIS, Long.toString(TOKEN_EXPIRE_MILLIS))
            )
            .build();
        when(tokenStore.put(eq(EMPTY_SECURITY_TOKEN_KEY), (ConsumerToken) argThat(is(deeplyEqualTo(token))))).thenReturn(token);

        store.setTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "", tokenInfo);

        verify(tokenStore).put(
            eq(EMPTY_SECURITY_TOKEN_KEY),
            argThat(
                hasProperty(AtlassianOAuthStore.TokenSessionProperties.TOKEN_EXPIRE_MILLIS)
                    .withValue(String.valueOf(TOKEN_EXPIRE_MILLIS))
            )
        );
    }

    @Test(expected=GadgetException.class)
    public void verifyThatSetTokenInfoThrowsGadgetExceptionWhenStoreDoesNotReturnSameTokenThatWasPassedIn() throws Exception
    {
        TokenInfo tokenInfo = new TokenInfo("token", "secret", SESSION_HANDLE, TOKEN_EXPIRE_MILLIS);
        ConsumerToken returnedToken = ConsumerToken.newAccessToken("token").tokenSecret("sekret").consumer(CONSUMER).build();
        when(tokenStore.put(isA(Key.class), isA(ConsumerToken.class))).thenReturn(returnedToken);
        
        store.setTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "", tokenInfo);
    }
    
    @Test
    public void verifyThatGetTokenInfoRetrievesSessionHandle() throws Exception
    {
        ConsumerToken tokenWithSessionHandle = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .properties(ImmutableMap.of(AtlassianOAuthStore.TokenSessionProperties.SESSION_HANDLE, SESSION_HANDLE))
            .build();
        when(tokenStore.get(EMPTY_SECURITY_TOKEN_KEY)).thenReturn(tokenWithSessionHandle);

        TokenInfo tokenInfo = store.getTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "");

        assertThat(tokenInfo.getSessionHandle(), is(equalTo(SESSION_HANDLE)));
    }

    @Test
    public void verifyThatGetTokenInfoRetrievesTokenExpireMillis() throws Exception
    {
        ConsumerToken tokenWithExpiration = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .properties(ImmutableMap.of(
                AtlassianOAuthStore.TokenSessionProperties.TOKEN_EXPIRE_MILLIS, String.valueOf(TOKEN_EXPIRE_MILLIS)
            ))
            .build();
        when(tokenStore.get(EMPTY_SECURITY_TOKEN_KEY)).thenReturn(tokenWithExpiration);

        TokenInfo tokenInfo = store.getTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "");

        assertThat(tokenInfo.getTokenExpireMillis(), is(equalTo(TOKEN_EXPIRE_MILLIS)));
    }

    @Test
    public void verifyThatTokenWithNoPropertiesProducesValidTokenInfo() throws Exception
    {
        ConsumerToken tokenWithNoProperties = ConsumerToken.newAccessToken("token")
            .tokenSecret("secret")
            .consumer(CONSUMER)
            .build();
        when(tokenStore.get(EMPTY_SECURITY_TOKEN_KEY)).thenReturn(tokenWithNoProperties);

        TokenInfo tokenInfo = store.getTokenInfo(EMPTY_SECURITY_TOKEN, CONSUMER_INFO, "", "");

        assertThat(tokenInfo.getSessionHandle(), is(nullValue()));
        assertThat(tokenInfo.getTokenExpireMillis(), is(equalTo(0L)));
    }
    
    private Matcher<ConsumerToken> hasEqualConsumer(final Token token)
    {
        final Consumer consumer = token.getConsumer();
        return new BaseMatcher<ConsumerToken>()
        {
            public boolean matches(Object o)
            {
                if (o == null)
                {
                    return false;
                }
                Consumer c = ((Token) o).getConsumer();
                return consumer.getKey().equals(c.getKey());
            }

            public void describeTo(Description desc)
            {
                desc.appendValue(token);
            }
        };
    }

    private static TokenPropertyMatcher hasProperty(String key)
    {
        return new TokenPropertyMatcher(key);
    }

    private static final class TokenPropertyMatcher extends BaseMatcher<ConsumerToken>
    {
        private final String key;

        public TokenPropertyMatcher(String key)
        {
            this.key = checkNotNull(key, "key");
        }

        public boolean matches(Object o)
        {
            return o != null && o instanceof ConsumerToken && ((ConsumerToken) o).hasProperty(key);
        }

        public void describeTo(Description description)
        {
            description.appendText("Token with property ").appendValue(key);
        }

        public Matcher<ConsumerToken> withValue(final String value)
        {
            checkNotNull(value, "value");
            return new BaseMatcher<ConsumerToken>()
            {
                public boolean matches(Object o)
                {
                    return TokenPropertyMatcher.this.matches(o) && value.equals(((ConsumerToken) o).getProperty(key));
                }

                public void describeTo(Description description)
                {
                    TokenPropertyMatcher.this.describeTo(description);
                    description.appendText("=").appendValue(value);
                }
            };
        }
    }
}
