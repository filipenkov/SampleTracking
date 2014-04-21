/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.exception.PermissionException;

/**
 * Thrown when a {@link com.atlassian.crowd.directory.RemoteDirectory} does not have the
 * permission set to perform an operation such as add/modify/delete verses a
 * group/principal/role.
 */
public class DirectoryPermissionException extends PermissionException
{
    /**
     * Default constructor.
     */
    public DirectoryPermissionException()
    {
    }

    /**
     * Default constructor.
     *
     * @param s the message.
     */
    public DirectoryPermissionException(String s)
    {
        super(s);
    }

    /**
     * Default constructor.
     *
     * @param s         the message.
     * @param throwable the {@link Exception Exception}.
     */
    public DirectoryPermissionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Default constructor.
     *
     * @param throwable the {@link Exception Exception}.
     */
    public DirectoryPermissionException(Throwable throwable)
    {
        super(throwable);
    }
}