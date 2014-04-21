package com.atlassian.jira.vcs.cvsimpl;

import org.apache.commons.lang.exception.NestableException;

public class ValidationException extends NestableException
{
    public ValidationException(String string)
    {
        super(string);
    }

    public ValidationException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
