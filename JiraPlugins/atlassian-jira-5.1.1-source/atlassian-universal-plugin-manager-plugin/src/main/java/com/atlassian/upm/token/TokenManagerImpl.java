package com.atlassian.upm.token;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.atlassian.security.random.DefaultSecureRandomService;
import com.atlassian.security.random.SecureRandomService;
import com.atlassian.upm.Sys;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;

/**
 * Threadsafe implementation of {@code TokenManager} with storage backed by a map
 */
public class TokenManagerImpl implements TokenManager
{
    private static final SecureRandomService random = DefaultSecureRandomService.getInstance();

    private final ConcurrentMap<String, Token> tokenStore;
    private final Function<String, ManagedLock> lockFactory;

    public TokenManagerImpl()
    {
        this.tokenStore = new ConcurrentHashMap<String, Token>();
        lockFactory = ManagedLocks.weakManagedLockFactory();
    }

    public String getTokenForUser(final String username)
    {
        return getTokenObjectForUser(username).getValue();
    }

    private Token getTokenObjectForUser(final String username)
    {
        try
        {
            return lockFactory.get(username).withLock(new Callable<Token>()
            {
                public Token call() throws Exception
                {
                    Token storedToken = tokenStore.get(username);
                    if (storedToken == null || storedToken.isExpired())
                    {
                        return generateAndStoreNewTokenForUser(username);
                    }
                    return storedToken;
                }
            });
        }
        catch (Exception e)
        {
            throw new TokenException("Unable to get token for user " + username, e);
        }
    }

    public boolean attemptToMatchAndInvalidateToken(final String username, final String tokenValue)
    {
        if (Sys.isXsrfTokenDisabled())
        {
            throw new TokenException("Token for user " + username + " rejected due to test mode override");
        }
        try
        {
            return lockFactory.get(username).withLock(new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    Token storedToken = tokenStore.get(username);
                    if (storedToken != null && tokenValue.equals(storedToken.getValue()))
                    {
                        // make sure the token can only be used once
                        generateAndStoreNewTokenForUser(username);
                        return !storedToken.isExpired();
                    }
                    return false;
                }
            });
        }
        catch (Exception e)
        {
            throw new TokenException("Unable to match and invalidate token for user " + username, e);
        }
    }

    private Token generateAndStoreNewTokenForUser(String username)
    {
        Token token = new Token(generateTokenString(), new Date());
        tokenStore.put(username, token);
        return token;
    }

    private String generateTokenString()
    {
        return Long.toString(random.nextLong());
    }
}
