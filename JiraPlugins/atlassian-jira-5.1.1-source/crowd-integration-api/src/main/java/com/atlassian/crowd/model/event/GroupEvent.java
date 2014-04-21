package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.group.Group;

import java.util.Map;
import java.util.Set;

public class GroupEvent extends AbstractAttributeEvent
{
    private final Group group;

    public GroupEvent(Operation operation, Directory directory, Group group, Map<String, Set<String>> storedAttributes, Set<String> deletedAttributes)
    {
        super(operation, directory, storedAttributes, deletedAttributes);
        this.group = group;
    }

    public Group getGroup()
    {
        return group;
    }
}
