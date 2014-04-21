package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;

/**
 * This event is published when a user has been successfully authenticated
 * to application.
 */
public class UserAuthenticatedEvent extends DirectoryEvent
{
    private final Application application;

    private final User user;

    public UserAuthenticatedEvent(Object source, Directory directory, Application application, User user)
    {
        super(source, directory);
        this.application = application;
        this.user = user;
    }

    public Application getApplication()
    {
        return application;
    }

    public User getUser()
    {
        return user;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserAuthenticatedEvent that = (UserAuthenticatedEvent) o;

        if (!application.equals(that.application)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + application.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }
}
