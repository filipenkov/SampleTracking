package com.atlassian.crowd.exception;

/**
 * Thrown when the account could not be found during an authentication attempt.
 *
 * @since 2.2.4
 */
public class AccountNotFoundException extends FailedAuthenticationException
{
    private final String name;

    /**
     * Constructs a new AccountNotFoundException.
     *
     * @param name name of the account
     */
    public AccountNotFoundException(String name)
    {
        this(name, null);
    }

    /**
     * Constructs a new AccountNotFoundException.
     *
     * @param name name of the account
     * @param e cause of the exception
     */
    public AccountNotFoundException(String name, Throwable e)
    {
        super(String.format("Account with name <%s> could not be found", name), e);
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
