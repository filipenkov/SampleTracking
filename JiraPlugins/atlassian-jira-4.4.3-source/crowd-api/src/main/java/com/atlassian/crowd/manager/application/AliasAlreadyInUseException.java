package com.atlassian.crowd.manager.application;

/**
 * Used to indicate that an alias is already by another application user.
 */
public class AliasAlreadyInUseException extends Exception
{
    private final String applicationName;
    private final String aliasName;
    private final String username;

    public AliasAlreadyInUseException(final String applicationName, final String aliasName, final String username)
    {
        super(new StringBuilder().append("Alias [").append(aliasName).append("] already in use for application [").append(applicationName).append("] by user [").append(username).append("]").toString());

        this.applicationName = applicationName;
        this.aliasName = aliasName;
        this.username = username;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public String getAliasName()
    {
        return aliasName;
    }

    public String getUsername()
    {
        return username;
    }
}
