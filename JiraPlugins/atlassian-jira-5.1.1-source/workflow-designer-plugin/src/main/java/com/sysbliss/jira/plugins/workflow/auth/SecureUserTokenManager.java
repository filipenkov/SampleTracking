package com.sysbliss.jira.plugins.workflow.auth;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.sysbliss.jira.plugins.workflow.util.TokenMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class SecureUserTokenManager implements UserTokenManager
{
    public static long DEFAULT_TIMEOUT = 1 * DateUtils.HOUR_MILLIS;

    private final Map<String, User> tokens;

    private final TokenFactory tokenFactory;

    public SecureUserTokenManager()
    {
        tokens = new TokenMap<String, User>(DEFAULT_TIMEOUT);
        tokenFactory = new TokenFactory();
    }

    public User getUserFromToken(String token)
    {
        final User user = tokens.get(token);

        // update the session timeout
        if (user != null)
        {
            tokens.put(token, user);
        }

        return user;
    }

    public String findToken(User user)
    {
        String token = null;
        final Set entrySet = tokens.entrySet();
        final Iterator it = entrySet.iterator();
        Map.Entry entry;
        User userValue;
        while (it.hasNext())
        {
            entry = (Map.Entry) it.next();
            userValue = (User) entry.getValue();
            if (userValue == user)
            {
                token = (String) entry.getKey();
                break;
            }
        }
        return token;
    }

    public String createToken(User user) throws Exception
    {
        String token = findToken(user);
        if (token == null)
        {
            synchronized (tokens)
            {
                token = tokenFactory.createToken();
                tokens.put(token, user);
            }
        }
        return token;
    }

    public static class TokenFactory
    {
        public String createToken()
        {
            return DefaultSecureTokenGenerator.getInstance().generateToken();
        }
    }
}
