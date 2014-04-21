package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An Event that represents the creation of an attibute+values against a {@link com.atlassian.crowd.model.user.User}
 */
public class UserAttributeStoredEvent extends UserUpdatedEvent
{
    private final Map<String, Set<String>> attributes;

    public UserAttributeStoredEvent(Object source, Directory directory, User user, final Map<String, Set<String>> attributes)
    {
        super(source, directory, user);
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
