package com.atlassian.jira.web.monitor.jmx;

/**
 * An exception indicating that there was a problem registering or unregistering a bean in JMX.
 *
 * @since v4.3
 */
public class JMXException extends RuntimeException
{
    /**
     * Constructs a new JMXException exception with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     * value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public JMXException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
