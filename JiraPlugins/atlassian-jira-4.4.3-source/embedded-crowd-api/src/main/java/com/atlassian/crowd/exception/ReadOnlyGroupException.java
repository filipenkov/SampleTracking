package com.atlassian.crowd.exception;

/**
 * Thrown when a directory is asked to modify a group or its memberships
 * which cannot be modified. For example, trying to modify an LDAP group
 * in the hybrid directory when local groups are enabled.
 */
public class ReadOnlyGroupException extends CrowdException
{
    private final String groupName;

    public ReadOnlyGroupException(String groupName)
    {
        this(groupName, null);
    }

    public ReadOnlyGroupException(String groupName, Throwable e)
    {
        super("Group <" + groupName + "> is read-only and cannot be updated", e);
        this.groupName = groupName;
    }

    /**
     * Returns the name of the read-only group.
     *
     * @return name of the read-only group
     */
    public String getGroupName()
    {
        return groupName;
    }
}
