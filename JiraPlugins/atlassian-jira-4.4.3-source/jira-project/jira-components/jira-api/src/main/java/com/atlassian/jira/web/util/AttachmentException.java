package com.atlassian.jira.web.util;

import org.apache.commons.lang.exception.NestableException;

public class AttachmentException extends NestableException
{
    public AttachmentException(String message)
    {
        super(message);
    }

    public AttachmentException(Throwable cause)
    {
        super(cause);
    }

    public AttachmentException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
