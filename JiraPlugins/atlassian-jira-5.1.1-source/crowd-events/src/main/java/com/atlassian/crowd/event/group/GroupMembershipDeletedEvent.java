package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.membership.MembershipType;

/**
 * An Event that represents the deletion of a Group + Principal membership
 */
public class GroupMembershipDeletedEvent extends DirectoryEvent
{
    private final String entityName;
    private final String groupName;
    private final MembershipType membershipType;

    public GroupMembershipDeletedEvent(Object source, Directory directory, String entityName, String groupName, MembershipType membershipType)
    {
        super(source, directory);
        this.entityName = entityName;
        this.membershipType = membershipType;
        this.groupName = groupName;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public MembershipType getMembershipType()
    {
        return membershipType;
    }
}
