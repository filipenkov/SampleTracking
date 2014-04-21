package com.atlassian.upm.notification;

/**
 * Represents a dismissal state for a specific user.
 */
public class DismissedState
{
    private final String username;
    private final boolean dismissed;

    public DismissedState(String username, boolean dismissed)
    {
        this.username = username; //will be null for anonymous users
        this.dismissed = dismissed;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isDismissed()
    {
        return dismissed;
    }
}