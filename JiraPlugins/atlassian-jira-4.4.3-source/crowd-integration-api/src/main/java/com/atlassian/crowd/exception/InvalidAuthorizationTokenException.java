/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.exception;

/**
 * Thrown when the authenticated token is invalid.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class InvalidAuthorizationTokenException extends Exception
{
    /**
     * Default constructor.
     */
    public InvalidAuthorizationTokenException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public InvalidAuthorizationTokenException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidAuthorizationTokenException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidAuthorizationTokenException(Throwable throwable)
    {
        super(throwable);
    }
}