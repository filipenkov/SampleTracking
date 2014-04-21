package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.group.Group;

/**
 * An Event that represents the deletion of an attibute against a {@link com.atlassian.crowd.model.group.Group}
 */
public class GroupAttributeDeletedEvent extends GroupUpdatedEvent
{
    private final String attributeName;

    public GroupAttributeDeletedEvent(Object source, Directory directory, Group group, String attributeName)
    {
        super(source, directory, group);
        this.attributeName = attributeName;
    }

    public String getAttributeName()
    {
        return attributeName;
    }
}
