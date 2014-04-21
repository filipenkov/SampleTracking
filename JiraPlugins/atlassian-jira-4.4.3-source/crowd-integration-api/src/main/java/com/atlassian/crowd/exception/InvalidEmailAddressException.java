/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.exception;

/**
 * Thrown when the email address is not valid.
 */
public class InvalidEmailAddressException extends Exception
{
    /**
     * Default constructor.
     */
    public InvalidEmailAddressException()
    {
    }

    /**
     * @param s the message.
     */
    public InvalidEmailAddressException(String s)
    {
        super(s);
    }

    /**
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidEmailAddressException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     *
     * @param throwable the {@link Exception Exception}.
     */
    public InvalidEmailAddressException(Throwable throwable)
    {
        super(throwable);
    }
}