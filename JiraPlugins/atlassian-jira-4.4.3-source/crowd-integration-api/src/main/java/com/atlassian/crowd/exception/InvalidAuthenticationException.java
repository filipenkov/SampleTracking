package com.atlassian.crowd.exception;

/**
 * Thrown when the attempted authentication is not valid.
 */
public class InvalidAuthenticationException extends CrowdException
{
    /**
     * Constructs a new <code>InvalidAuthenticationException</code> with the specified detail message.
     *
     * @param msg detail message
     */
    public InvalidAuthenticationException(final String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new <code>InvalidAuthenticationException</code> with the specified detail message and cause.
     *
     * @param msg detail message
     * @param cause the cause
     */
    public InvalidAuthenticationException(final String msg, final Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * Constructs a new <code>InvalidAuthenticationException</code> with the specified cause.
     *
     * @param cause the cause
     */
    public InvalidAuthenticationException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new instance of an <code>InvalidAuthenticationException</code> with a default detail message using the
     * name of the entity that failed to authenticate.
     *
     * @param name name of entity
     * @return new instance of <code>InvalidAuthenticationException</code>
     */
    public static InvalidAuthenticationException newInstanceWithName(final String name)
    {
        return new InvalidAuthenticationException("Account with name <" + name + "> failed to authenticate");
    }

    /**
     * Creates a new instance of an <code>InvalidAuthenticationException</code> with a default detail message using the
     * name of the entity that failed to authenticate, and a cause.
     *
     * @param name name of entity
     * @param cause the cause
     * @return new instance of <code>InvalidAuthenticationException</code>
     */
    public static InvalidAuthenticationException newInstanceWithName(final String name, final Throwable cause)
    {
        return new InvalidAuthenticationException("Account with name <" + name + "> failed to authenticate", cause);
    }
}