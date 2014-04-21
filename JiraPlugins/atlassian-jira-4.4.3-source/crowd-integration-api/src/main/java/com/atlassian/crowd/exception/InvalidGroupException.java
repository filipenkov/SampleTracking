package com.atlassian.crowd.exception;

import com.atlassian.crowd.model.group.Group;

/**
 * Thrown to indicate an invalid model group.
 */
public class InvalidGroupException extends CrowdException
{
    private final Group group;

    /**
     * Constructs a new <code>InvalidGroupException</code> with the invalid group given and a cause.
     *
     * @param group the invalid group
     * @param cause the cause (a null value is permitted)
     */
    public InvalidGroupException(Group group, Throwable cause)
    {
        super(cause);
        this.group = group;
    }

    /**
     * Constructs a new <code>InvalidGroupException</code> with the invalid group and
     *
     * @param group invalid group
     * @param message detail message
     */
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