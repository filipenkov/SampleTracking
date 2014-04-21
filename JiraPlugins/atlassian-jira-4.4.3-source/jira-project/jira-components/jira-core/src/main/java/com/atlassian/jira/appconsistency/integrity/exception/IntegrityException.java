/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.exception;

import org.apache.commons.lang.exception.NestableException;

public class IntegrityException extends NestableException
{
    public IntegrityException()
    {
    }

    public IntegrityException(String string)
    {
        super(string);
    }

    public IntegrityException(Throwable throwable)
    {
        super(throwable);
    }

    public IntegrityException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
