package com.atlassian.crowd.exception.runtime;

/**
 * Thrown when the specified group could not be found.
 *
 * @since v2.1
 */
public class GroupNotFoundException extends CrowdRuntimeException
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
