package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Objects;

/**
 * An ApplicationUser comprising of a String key and an embedded crowd User.
 *
 * @since v5.1.1
 */
public class DelegatingApplicationUser implements ApplicationUser
{
    private final String key;
    private final User user;

    public DelegatingApplicationUser(final String key, final User user)
    {
        this.key = key;
        this.user = user;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public long getDirectoryId()
    {
        return user.getDirectoryId();
    }

    @Override
    public boolean isActive()
    {
        return user.isActive();
    }

    @Override
    public String getEmailAddress()
    {
        return user.getEmailAddress();
    }

    @Override
    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    @Override
    public int compareTo(User user)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName()
    {
        return user.getName();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof ApplicationUser)
        {
            final ApplicationUser other = (ApplicationUser) obj;
            return Objects.equal(key, other.getKey());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(key);
    }

    @Override
    public String toString()
    {
        return getName() + "(" + getKey() + ")";
    }
}
