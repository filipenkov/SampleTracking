package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.membership.MembershipType;

/**
 * An Event that represents the creation of a Principal/Child Group to Group membership
 */
public class GroupMembershipCreatedEvent extends DirectoryEvent
{
    private final String entityName;
    private final String groupName;
    private final MembershipType membershipType;

    public GroupMembershipCreatedEvent(Object source, Directory directory, String entityName, String groupName, MembershipType membershipType)
    {
        super(source, directory);
        this.entityName = entityName;
        this.groupName = groupName;
        this.membershipType = membershipType;
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
