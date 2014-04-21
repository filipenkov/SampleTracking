package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.membership.MembershipType;

public class AutoGroupMembershipCreatedEvent extends GroupMembershipCreatedEvent
{

    public AutoGroupMembershipCreatedEvent(Object source, Directory directory, String entityName, String groupName, MembershipType membershipType)
    {
        super(source, directory, entityName, groupName, membershipType);
    }

}
