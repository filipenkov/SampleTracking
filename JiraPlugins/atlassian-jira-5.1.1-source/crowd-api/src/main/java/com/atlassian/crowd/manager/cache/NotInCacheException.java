/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.manager.cache;

public class NotInCacheException extends Exception
{
    /**
     * Default constructor.
     */
    public NotInCacheException() {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public NotInCacheException(String s) {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public NotInCacheException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public NotInCacheException(Throwable throwable) {
        super(throwable);
    }
}