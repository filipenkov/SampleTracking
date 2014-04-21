/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.manager.cache;

public class CacheManagerException extends RuntimeException
{
    /**
     * Default constructor.
     */
    public CacheManagerException() {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public CacheManagerException(String s) {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public CacheManagerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public CacheManagerException(Throwable throwable) {
        super(throwable);
    }
}