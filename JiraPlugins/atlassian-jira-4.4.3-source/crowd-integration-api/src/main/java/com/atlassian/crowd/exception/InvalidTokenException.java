/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.exception;

/**
 * Thrown when an invalid token is provided.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class InvalidTokenException extends Exception
{
    /**
     * Default constructor.
     */
    public InvalidTokenException()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public InvalidTokenException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         The message.
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidTokenException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidTokenException(Throwable throwable)
    {
        super(throwable);
    }
}
