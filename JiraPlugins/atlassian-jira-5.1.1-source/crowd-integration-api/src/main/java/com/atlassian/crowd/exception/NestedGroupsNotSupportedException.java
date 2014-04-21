package com.atlassian.crowd.exception;

/**
 * Thrown when a user attempts to add a group to another group in a Directory that does not support nested groups.
 */
public class NestedGroupsNotSupportedException extends CrowdException
{
    public NestedGroupsNotSupportedException(long directoryId)
    {
        super("Directory with id <" + directoryId + "> does not support nested groups");
    }
}
