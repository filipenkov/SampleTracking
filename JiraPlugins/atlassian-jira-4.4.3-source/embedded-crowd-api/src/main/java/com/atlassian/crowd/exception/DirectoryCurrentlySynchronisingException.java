package com.atlassian.crowd.exception;

/**
 * Thrown when an operation failed because the directory is currently synchronising.
 *
 * @since v2.1
 */
public class DirectoryCurrentlySynchronisingException extends CrowdException
{
    final long directoryId;

    public DirectoryCurrentlySynchronisingException(final long directoryId)
    {
        this(directoryId, null);
    }

    public DirectoryCurrentlySynchronisingException(final long directoryId, Throwable cause)
    {
        super("Directory " + directoryId + " is currently synchronising.", cause);
        this.directoryId = directoryId;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }
}
