package com.atlassian.jira.vcs.cvsimpl;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class CvsLogException extends NestableRuntimeException
{
    public CvsLogException(Throwable throwable)
    {
        super(throwable);
    }

    public CvsLogException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

}
