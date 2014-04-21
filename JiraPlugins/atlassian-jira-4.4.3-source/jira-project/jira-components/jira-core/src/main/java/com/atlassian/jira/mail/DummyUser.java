/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;


import com.atlassian.crowd.embedded.api.User;

/**
 * Used to represent a deleted user. For example, if a notification e-mail is being sent and a user who is, for exmaple, a
 * reporter of the issue has been deleted from the system, we need an object that represets the user. (We cannot subclass
 * com.opensymphony.user.User as it is final).
 */
public class DummyUser implements User
{
    private String name;

    public DummyUser(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return "";
    }

    public String getFullName()
    {
        return "Deleted User with username (" + getName() + ")";
    }

    public String getName()
    {
        return name;
    }

    @Override
    public long getDirectoryId()
    {
        throw new UnsupportedOperationException("Dummy User has no Directory.");
    }

    @Override
    public boolean isActive()
    {
        return false;
    }

    @Override
    public String getEmailAddress()
    {
        return "";
    }

    @Override
    public String getDisplayName()
    {
        return name;
    }

    @Override
    public int compareTo(User user)
    {
        throw new UnsupportedOperationException("Dummy User has no Directory.");
    }
}
