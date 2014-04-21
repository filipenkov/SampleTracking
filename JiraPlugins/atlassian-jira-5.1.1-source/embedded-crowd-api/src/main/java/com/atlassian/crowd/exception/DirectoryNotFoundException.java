package com.atlassian.crowd.exception;

/**
 * Thrown when a directory could not be found
 */
public class DirectoryNotFoundException extends CrowdException
{
    private final String directoryName;
    private final Long id;

    public DirectoryNotFoundException(String directoryName)
    {
        this(directoryName, null);
    }

    public DirectoryNotFoundException(String directoryName, Throwable e)
    {
        super("Directory <" + directoryName + "> does not exist", e);
        this.directoryName = directoryName;
        this.id = null;
    }

    public DirectoryNotFoundException(Long id)
    {
        this(id, null);
    }

    public DirectoryNotFoundException(Long id, Throwable e)
    {
        super("Directory <" + id + "> does not exist", e);
        this.id = id;
        this.directoryName = null;
    }

    public DirectoryNotFoundException(Throwable e)
    {
        super(e);
        this.id = null;
        this.directoryName = null;
    }

    /**
     * Returns the name of the directory that could not be found.
     *
     * @return name of the directory that could not be found
     */
    public String getDirectoryName()
    {
        return directoryName;
    }

    /**
     * Returns the ID of the directory that could not be found.
     *
     * @return ID of the directory that could not be found
     */
    public Long getId()
    {
        return id;
    }
}