package com.atlassian.gadgets.renderer.internal.oauth;

import java.util.Map;

import com.atlassian.gadgets.util.Uri;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Token;
import com.atlassian.oauth.bridge.Consumers;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.atlassian.oauth.consumer.ConsumerTokenStore.Key;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.OAuthStore;

import net.oauth.OAuthServiceProvider;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.math.NumberUtils.toLong;
import static org.apache.shindig.gadgets.GadgetException.Code.INTERNAL_SERVER_ERROR;

/**
 * Implementation of the {@code OAuthStore} interface that uses the {@link ConsumerService} and {@link ConsumerTokenStore}
 * from the Atlassian OAuth Consumer Plugin to get consumer information and store tokens.  This implementation does
 * not support per gadget Consumer settings, and only returns the applications Consumer information.
 */
public class AtlassianOAuthStore implements OAuthStore
{
    private static final String OAUTH_CALLBACK_SERVLET_PATH = "plugins/servlet/gadgets/oauth-callback";
    
    private final ConsumerService consumerService;
    private final ConsumerTokenStore tokenStore;
    private final ApplicationProperties applicationProperties;

    @Inject
    public AtlassianOAuthStore(ConsumerService consumerService,
        ConsumerTokenStore tokenStore,
        ApplicationProperties applicationProperties)
    {
        this.consumerService = checkNotNull(consumerService, "consumerService");
        this.tokenStore = checkNotNull(tokenStore, "tokenStore");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }
    
    public ConsumerInfo getConsumerKeyAndSecret(SecurityToken securityToken, String service, OAuthServiceProvider provider) throws GadgetException
    {
        return new ConsumerInfo(
            Consumers.asOAuthConsumer(findConsumerForService(service), provider),
            null,
            getOAuthCallbackUrl()
        );
    }

    public TokenInfo getTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName, String tokenName) throws GadgetException
    {
        ConsumerTokenStore.Key key = createKey(securityToken, serviceName, tokenName);
        Token token = tokenStore.get(key);
        if (token == null)
        {
            return null;
        }
        return new TokenInfo(token.getToken(),
                             token.getTokenSecret(),
                             token.getProperty(TokenSessionProperties.SESSION_HANDLE),
                             toLong(token.getProperty(TokenSessionProperties.TOKEN_EXPIRE_MILLIS)));
    }

    public void setTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName, String tokenName, TokenInfo tokenInfo) throws GadgetException
    {
        ConsumerTokenStore.Key key = createKey(securityToken, serviceName, tokenName);
        ConsumerToken token = ConsumerToken.newAccessToken(tokenInfo.getAccessToken())
            .tokenSecret(tokenInfo.getTokenSecret())
            .consumer(Consumers.fromOAuthConsumer(consumerInfo.getConsumer()))
            .properties(TokenSessionProperties.createPropertyMap(tokenInfo))
            .build();
        ConsumerToken savedToken = tokenStore.put(key, token);
        if (savedToken.isRequestToken() || !token.getToken().equals(savedToken.getToken())
         || !token.getTokenSecret().equals(savedToken.getTokenSecret())
         || !token.getConsumer().getKey().equals(savedToken.getConsumer().getKey())
         || !token.getProperties().equals(savedToken.getProperties()))
        {
            throw new GadgetException(INTERNAL_SERVER_ERROR, "Saved token is inconsistent with the actual token");
        }
    }
    
    private String getOAuthCallbackUrl()
    {
        return Uri.ensureTrailingSlash(applicationProperties.getBaseUrl()) + OAUTH_CALLBACK_SERVLET_PATH;
    }
    
    public void removeToken(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName, String tokenName) throws GadgetException
    {
        ConsumerTokenStore.Key key = createKey(securityToken, serviceName, tokenName);
        tokenStore.remove(key);
    }

    private Consumer findConsumerForService(String service)
    {
        Consumer consumer;
        if (isBlank(service))
        {
            consumer = consumerService.getConsumer();
        }
        else
        {
            consumer = consumerService.getConsumer(service);
            if (consumer == null)
            {
                consumer = consumerService.getConsumer();
            }
        }
        return consumer;
    }
    
    private Key createKey(SecurityToken securityToken, String serviceName, String tokenName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(securityToken.getModuleId());
        sb.append(':');
        sb.append(securityToken.getViewerId());
        sb.append(':');
        sb.append(serviceName);
        sb.append(':');
        sb.append(tokenName);
        return new Key(sb.toString());
    }

    /**
     * Property keys used to store extra session information with tokens.
     *
     * @see <a href="http://oauth.googlecode.com/svn/spec/ext/session/1.0/drafts/1/spec.html">OAuth Session 1.0 Draft 1
     *      specification</a>
     */
    static final class TokenSessionProperties
    {
        static final String SESSION_HANDLE = "org.apache.shindig.oauth.sessionHandle";
        static final String TOKEN_EXPIRE_MILLIS = "org.apache.shindig.oauth.tokenExpireMillis";

        static Map<String, String> createPropertyMap(TokenInfo tokenInfo)
        {
            ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();

            if (tokenInfo.getSessionHandle() != null)
            {
                properties.put(TokenSessionProperties.SESSION_HANDLE, tokenInfo.getSessionHandle());
            }

            if (tokenInfo.getTokenExpireMillis() > 0)
            {
                properties.put(TokenSessionProperties.TOKEN_EXPIRE_MILLIS, String.valueOf(tokenInfo.getTokenExpireMillis()));
            }

            return properties.build();
        }
    }
}