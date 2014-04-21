/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import org.apache.commons.lang.exception.NestableException;

public class ColumnLayoutStorageException extends NestableException
{
    public ColumnLayoutStorageException()
    {
    }

    public ColumnLayoutStorageException(String string)
    {
        super(string);
    }

    public ColumnLayoutStorageException(Throwable throwable)
    {
        super(throwable);
    }

    public ColumnLayoutStorageException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
