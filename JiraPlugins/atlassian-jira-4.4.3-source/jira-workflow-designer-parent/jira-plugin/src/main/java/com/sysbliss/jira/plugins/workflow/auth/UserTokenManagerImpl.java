/**
 *
 */
package com.sysbliss.jira.plugins.workflow.auth;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.RandomGenerator;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import com.sysbliss.jira.plugins.workflow.exception.FlexLoginException;
import com.sysbliss.jira.plugins.workflow.util.TokenMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author jdoklovic
 */
public class UserTokenManagerImpl implements UserTokenManager {

    public static long DEFAULT_TIMEOUT = 1 * DateUtils.HOUR_MILLIS;
    private static UserTokenManagerImpl _instance;

    private final Map tokens;

    private UserTokenManagerImpl() {
        this(DEFAULT_TIMEOUT);
    }

    private UserTokenManagerImpl(final long timeout) {
        tokens = new TokenMap(timeout);
    }

    public static UserTokenManagerImpl getInstance() {
        if (_instance == null) {
            _instance = new UserTokenManagerImpl();
        }

        return _instance;
    }

    /**
     * {@inheritDoc}
     */
    public User getUserFromToken(final String token) {
        final User user = (User) tokens.get(token);

        // update the session timeout
        if (user != null) {
            tokens.put(token, user);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    public String login(final String username, final String password) throws FlexLoginException {
        User user = null;

        try {
            user = UserManager.getInstance().getUser(username);
            final boolean loggedIn = user.authenticate(password);
            if (loggedIn) {
                return createToken(user);
            }

        } catch (final Exception e) {

        }

        throw new FlexLoginException(username + " could not be logged in.");
    }

    public String createToken(final User user) throws Exception {
        String token = findToken(user);
        if (token == null) {
            synchronized (tokens) {
                token = RandomGenerator.randomString(10);

                int count = 0;
                while (tokens.containsKey(token) && count++ < 10) {
                    token = RandomGenerator.randomString(10);
                }

                if (count >= 10) {
                    throw new Exception("Error generating authentication token after 10 attempts?");
                }

                tokens.put(token, user);
            }
        }

        return token;
    }

    public String findToken(final User user) {
        String token = null;
        final Set entrySet = tokens.entrySet();
        final Iterator it = entrySet.iterator();
        Entry entry;
        User userValue;
        while (it.hasNext()) {
            entry = (Entry) it.next();
            userValue = (User) entry.getValue();
            if (userValue == user) {
                token = (String) entry.getKey();
                break;
            }
        }
        return token;

    }

}
