package com.atlassian.crowd.exception;

/**
 * Thrown when an application is not found.
 */
public class ApplicationNotFoundException extends ObjectNotFoundException
{
    private final String applicationName;
    private final Long id;

    /**
     * Constructs a new application not found exception with an application name.
     * @param applicationName Name of the application.
     */
    public ApplicationNotFoundException(String applicationName)
    {
        this(applicationName, null);
    }

    /**
     * Constructs a new application not found exception with an application name and
     * cause.
     *
     * @param  applicationName  Name of the application.
     * @param  e the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ApplicationNotFoundException(String applicationName, Throwable e)
    {
        super("Application <" + applicationName + "> does not exist", e);
        this.applicationName = applicationName;
        this.id = null;
    }

    /**
     * Constructs a new application not found exception with an application id.
     * @param id Id of the application.
     */
    public ApplicationNotFoundException(Long id)
    {
        this(id, null);
    }

    /**
     * Constructs a new application not found exception with an application id and
     * cause.
     *
     * @param id Id of the application.
     * @param  e the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ApplicationNotFoundException(Long id, Throwable e)
    {
        super("Application <" + id + "> does not exist", e);
        this.id = id;
        this.applicationName = null;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public Long getId()
    {
        return id;
    }
}
