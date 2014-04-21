package com.atlassian.crowd.exception;

/**
 * Thrown when the specified group could not be found.
 */
public class GroupNotFoundException extends ObjectNotFoundException
{
    private final String groupName;

    public GroupNotFoundException(String groupName)
    {
        this(groupName, null);
    }

    public GroupNotFoundException(String groupName, Throwable e)
    {
        super("Group <" + groupName + "> does not exist", e);
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }
}
