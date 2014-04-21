package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;

/**
 * Simple class that will compare a user/user string/user combination, in a null safe way, to determine if
 * the user names are equal.
 *
 * @since v3.13
 */
public class UserNameEqualsUtil
{
    public boolean equals(final String user1Name, final User user2)
    {
        final String name = (user2 != null) ? user2.getName() : null;
        if ((user1Name != null) && (user2 != null))
        {
            return IdentifierUtils.equalsInLowerCase(user1Name, name);
        }
        else if (user1Name == null)
        {
            return name == null;
        }
        return user1Name == null;
    }

    /**
     * @deprecated Use {@link #equals(String, com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    public boolean equals(final String user1Name, final com.opensymphony.user.User user2)
    {
        return this.equals(user1Name, (User) user2);
    }

    public boolean equals(final User user1, final User user2)
    {
        if ((user1 != null) && (user2 != null))
        {
            return IdentifierUtils.equalsInLowerCase(user1.getName(), user2.getName());
        }
        else if (user1 == null)
        {
            return user2.getName() == null;
        }
        return user1.getName() == null;
    }

    /**
     * @deprecated Use {@link #equals(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    public boolean equals(final com.opensymphony.user.User user1, final com.opensymphony.user.User user2)
    {
        return this.equals((User) user1, (User) user2);
    }
}
