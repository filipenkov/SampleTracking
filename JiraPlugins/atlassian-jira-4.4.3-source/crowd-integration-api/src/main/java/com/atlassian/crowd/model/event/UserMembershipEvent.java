package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;

import java.util.Collections;
import java.util.Set;

public class UserMembershipEvent extends AbstractOperationEvent
{
    private final String childUsername;
    private final Set<String> parentGroupNames;

    public UserMembershipEvent(Operation operation, Directory directory, String childUsername, String groupName)
    {
        super(operation, directory);
        this.childUsername = childUsername;
        this.parentGroupNames = Collections.singleton(groupName);
    }

    public UserMembershipEvent(Operation operation, Directory directory, String childUsername, Set<String> parentGroupNames)
    {
        super(operation, directory);
        this.childUsername = childUsername;
        this.parentGroupNames = parentGroupNames;
    }

    public Set<String> getParentGroupNames()
    {
        return parentGroupNames;
    }

    public String getChildUsername()
    {
        return childUsername;
    }
}
