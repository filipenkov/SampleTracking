/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.exception;

import com.atlassian.crowd.model.group.Group;

/**
 * Thrown when an invalid role is provided.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class InvalidRoleException extends InvalidGroupException
{
    public InvalidRoleException(final Group legacyRole, final String message)
    {
        super(legacyRole, message);
    }
}