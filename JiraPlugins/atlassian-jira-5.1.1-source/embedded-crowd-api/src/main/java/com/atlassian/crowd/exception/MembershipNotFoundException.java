package com.atlassian.crowd.exception;

/**
 * Used to denote that a particular USER-GROUP or GROUP-GROUP membership
 * does not exist.
 *
 * This could be thrown in cases where the calling code attempts to remove
 * a user from a group when the user is not a direct member of the group, etc.
 */
public class MembershipNotFoundException extends ObjectNotFoundException
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

    /**
     * Returns name of the child.
     *
     * @return name of the child
     */
    public String getChildName()
    {
        return childName;
    }

    /**
     * Returns names of the parent.
     *
     * @return name of the parent
     */
    public String getParentName()
    {
        return parentName;
    }
}

