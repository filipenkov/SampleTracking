package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

/**
 * An Event that represents the deletion of an attibute+values against a {@link com.atlassian.crowd.model.user.User}
 */
public class UserAttributeDeletedEvent extends UserUpdatedEvent
{
    private final String attributeName;

    public UserAttributeDeletedEvent(Object source, Directory directory, User user, String attributeName)
    {
        super(source, directory, user);
        this.attributeName = attributeName;
    }

    public String getAttributeName()
    {
        return attributeName;
    }
}
