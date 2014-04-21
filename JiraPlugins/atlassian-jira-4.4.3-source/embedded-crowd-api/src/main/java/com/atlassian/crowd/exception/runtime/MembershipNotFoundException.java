package com.atlassian.crowd.exception.runtime;

/**
 * Used to denote that a particular USER-GROUP or GROUP-GROUP membership
 * does not exist.
 *
 * This could be thrown in cases where the calling code attempts to remove
 * a user from a group when the user is not a direct member of the group, etc.
 */
public class MembershipNotFoundException extends CrowdRuntimeException
{
    private final String childName;
    private final String parentName;

    public MembershipNotFoundException(String childName, String parentName)
    {
        this(childName, parentName, null);
    }

    public MembershipNotFoundException(String childName, String parentName, Throwable e)
    {
        super("The child entity <" + childName + "> is not a member of the parent <" + parentName + ">", e);
        this.childName = childName;
        this.parentName = parentName;
    }

    public String getChildName()
    {
        return childName;
    }

    public String getParentName()
    {
        return parentName;
    }
}
