package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @see {@link SecureUserTokenManager} for details
 */
public class DefaultSecureUserTokenManager implements SecureUserTokenManager, Startable
{
    //tokens are valid for 30 mins
    final ConcurrentMap<TokenKey, User> tokenCache = new MapMaker().expiration(30, TimeUnit.MINUTES).makeMap();

    private final EventPublisher eventPublisher;

    public DefaultSecureUserTokenManager(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String generateToken(final User user, final TokenType tokenType)
    {
        if (user == null)
        {
            return null;
        }
        final String token = DefaultSecureTokenGenerator.getInstance().generateToken();
        tokenCache.put(new TokenKey(token, tokenType), user);
        return token;
    }

    @Override
    public User useToken(final String token, final TokenType tokenType)
    {
        return tokenCache.remove(new TokenKey(token, tokenType));
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        tokenCache.clear();
    }

    private static class TokenKey
    {
        private final String token;
        private final TokenType tokenType;

        private TokenKey(final String token, final TokenType tokenType)
        {
            this.token = token;
            this.tokenType = tokenType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TokenKey tokenKey = (TokenKey) o;

            if (token != null ? !token.equals(tokenKey.token) : tokenKey.token != null) { return false; }
            if (tokenType != tokenKey.tokenType) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = token != null ? token.hashCode() : 0;
            result = 31 * result + tokenType.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return token + "," + tokenType;
        }
    }
}
