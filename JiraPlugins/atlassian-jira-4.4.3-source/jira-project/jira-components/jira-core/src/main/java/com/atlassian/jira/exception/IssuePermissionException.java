/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class IssuePermissionException extends NestableRuntimeException
{
    public IssuePermissionException()
    {
    }

    public IssuePermissionException(String string)
    {
        super(string);
    }

    public IssuePermissionException(Throwable throwable)
    {
        super(throwable);
    }

    public IssuePermissionException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
