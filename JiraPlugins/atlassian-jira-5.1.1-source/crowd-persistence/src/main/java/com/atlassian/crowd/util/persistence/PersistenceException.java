/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.util.persistence;

/**
 * Persistence related exception.
 */
public class PersistenceException extends Exception
{
    /**
     * Default constructor.
     */
    public PersistenceException() {
    }

    /**
     * Default constructor.
     *
     * @param s The message.
     */
    public PersistenceException(String s) {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         The message.
     * @param throwable The {@link Exception Exception}.
     */
    public PersistenceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable The {@link Exception Exception}.
     */
    public PersistenceException(Throwable throwable) {
        super(throwable);
    }
}