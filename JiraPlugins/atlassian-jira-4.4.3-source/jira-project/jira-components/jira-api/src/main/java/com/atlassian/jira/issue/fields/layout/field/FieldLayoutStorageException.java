/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import org.apache.commons.lang.exception.NestableException;

public class FieldLayoutStorageException extends NestableException
{
    public FieldLayoutStorageException()
    {
    }

    public FieldLayoutStorageException(String string)
    {
        super(string);
    }

    public FieldLayoutStorageException(Throwable throwable)
    {
        super(throwable);
    }

    public FieldLayoutStorageException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
