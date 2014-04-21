package com.atlassian.crowd.exception;

/**
 * Thrown when the account is inactive.
 */
public class InactiveAccountException extends FailedAuthenticationException
{
    private final String name;

    /**
     * Constructs a new InvalidAccountException.
     *
     * @param name name of the account
     */
    public InactiveAccountException(String name)
    {
        this(name, null);
    }

    public InactiveAccountException(String name, Throwable e)
    {
        super(String.format("Account with name <%s> is inactive", name), e);
        this.name = name;
    }

    /**
     * Returns the name of the account.
     *
     * @return name of the account
     */
    public String getName()
    {
        return name;
    }
}