package com.atlassian.crowd.exception.embedded;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.CrowdException;

/**
 * An exception to denote an invalid application/embedded group.
 */
public class InvalidGroupException extends CrowdException
{
    private final Group group;

    public InvalidGroupException(Group group, Throwable cause)
    {
        super(cause);
        this.group = group;
    }

    public InvalidGroupException(Group group, String message)
    {
        super(message);
        this.group = group;
    }

    public InvalidGroupException(Group group, String message, Throwable cause)
    {
        super(message, cause);
        this.group = group;
    }

    public Group getGroup()
    {
        return group;
    }
}
