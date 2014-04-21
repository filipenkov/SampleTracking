package com.atlassian.crowd.event.group;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.group.Group;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An Event that represents the creation of a atrributes against a {@link com.atlassian.crowd.model.group.Group}
 */
public class GroupAttributeStoredEvent extends GroupUpdatedEvent
{
    private final Map<String, Set<String>> attributes;

    public GroupAttributeStoredEvent(Object source, Directory directory, Group group, final Map<String, Set<String>> attributes)
    {
        super(source, directory, group);
        this.attributes = attributes;
    }

    public Set getAttributeNames()
    {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    public Set<String> getAttributeValues(final String key)
    {
        return Collections.unmodifiableSet(attributes.get(key));
    }
}
