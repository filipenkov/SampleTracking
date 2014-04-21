/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.manager.property;

/**
 * Error accessing property.
 */
public class PropertyManagerException extends Exception
{
    /**
     * Default constructor.
     */
    public PropertyManagerException() {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public PropertyManagerException(String s) {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public PropertyManagerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public PropertyManagerException(Throwable throwable) {
        super(throwable);
    }
}
