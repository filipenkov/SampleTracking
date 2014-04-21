package com.atlassian.labs.jira4compat.impl;

import com.atlassian.crowd.embedded.api.User;

/**
 *
 */
public class OsUserAdapter implements User
{
    private final com.opensymphony.user.User user;

    /**
     * Static constructor of an OsUserAdapter.
     * Returns null if the user passed is null
     *
     * @param user
     * @return
     */
    public static OsUserAdapter build(com.opensymphony.user.User user)
    {
        if (user == null)
        {
            return null;
        }
        return new OsUserAdapter(user);
    }

    private OsUserAdapter(com.opensymphony.user.User user)
    {
        this.user = user;
    }

    public long getDirectoryId()
    {
        return 0;
    }

    public boolean isActive()
    {
        return user.isActive();
    }

    public String getEmailAddress()
    {
        return user.getEmailAddress();
    }

    public String getDisplayName()
    {
        return user.getDisplayName();
    }

    public int compareTo(User user)
    {
        return user.compareTo(user);
    }

    public String getName()
    {
        return user.getName();
    }
}
