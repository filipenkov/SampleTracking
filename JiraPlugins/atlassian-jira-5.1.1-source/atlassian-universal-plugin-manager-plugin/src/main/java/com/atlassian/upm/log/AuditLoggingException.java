package com.atlassian.upm.log;

/**
 * A {@code RuntimeException} thrown when an error occurs writing to the audit log
 */
public class AuditLoggingException extends RuntimeException
{
    public AuditLoggingException(String message)
    {
        super(message);
    }

    public AuditLoggingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
