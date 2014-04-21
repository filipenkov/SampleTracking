package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;

import javax.annotation.Nullable;

/**
 * Simple class that will compare a username/user combination, in a null safe way, to determine if
 * the user names are equal.
 *
 * @since v3.13 Renamed and refactored in v5.0
 */
public class UserNames
{
    public static boolean equal(final String user1Name, final User user2)
    {
        if (user1Name == null)
        {
            return user2 == null;
        }
        if (user2 == null)
        {
            return false;
        }
        else
        {
            return IdentifierUtils.equalsInLowerCase(user1Name, user2.getName());
        }
    }

    /**
     * Return a unique identifier for the passed username. In Crowd (and JIRA) usernames are case insensitive.
     *
     * @param username the username to process. Can be null.
     *
     * @return the unique identifier for the passed username. Will be null if the passed username is null.
     */
    @Nullable
    public static String toKey(@Nullable final String username)
    {
        return username == null ? null : IdentifierUtils.toLowerCase(username);
    }
}
