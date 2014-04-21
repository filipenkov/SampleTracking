/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.exception;

/**
 * Thrown when an entity is not found.
 */
public class ObjectNotFoundException extends CrowdException
{
    public ObjectNotFoundException()
    {
        super();
    }

    public ObjectNotFoundException(Class entityClass, Object identifier)
    {
        super(new StringBuilder(64).append("Failed to find entity of type [").append(entityClass.getCanonicalName()).append("] with identifier [").append(identifier).append("]").toString());
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ObjectNotFoundException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ObjectNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Default constructor.
     * @param throwable The {@link Exception Exception}.
     */
    public ObjectNotFoundException(Throwable throwable)
    {
        super(throwable);
    }
}