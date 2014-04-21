package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;

import java.util.Collections;
import java.util.Set;

public class GroupMembershipEvent extends AbstractOperationEvent
{
    private final String groupName;
    private final Set<String> parentGroupNames;
    private final Set<String> childGroupNames;

    public GroupMembershipEvent(Operation operation, Directory directory, String groupName, String parentGroupName)
    {
        super(operation, directory);
        this.groupName = groupName;
        this.parentGroupNames = Collections.singleton(parentGroupName);
        this.childGroupNames = Collections.emptySet();
    }

    public GroupMembershipEvent(Operation operation, Directory directory, String groupName, Set<String> parentGroupNames, Set<String> childGroupNames)
    {
        super(operation, directory);
        this.groupName = groupName;
        this.parentGroupNames = parentGroupNames;
        this.childGroupNames = childGroupNames;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public Set<String> getParentGroupNames()
    {
        return parentGroupNames;
    }

    public Set<String> getChildGroupNames()
    {
        return childGroupNames;
    }
}
