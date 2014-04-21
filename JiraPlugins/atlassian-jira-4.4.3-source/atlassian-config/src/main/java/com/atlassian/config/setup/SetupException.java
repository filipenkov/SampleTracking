/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.config.setup;

import org.apache.commons.lang.exception.NestableException;

public class SetupException extends NestableException
{
    public SetupException()
    {
    }

    public SetupException(String message)
    {
        super(message);
    }

    public SetupException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SetupException(Throwable cause)
    {
        super(cause);
    }
}
