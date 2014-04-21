package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.user.User;

import java.util.Map;
import java.util.Set;

public class UserEvent extends AbstractAttributeEvent
{
    private final User user;

    public UserEvent(Operation operation, Directory directory, User user, Map<String, Set<String>> storedAttributes, Set<String> deletedAttributes)
    {
        super(operation, directory, storedAttributes, deletedAttributes);
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
