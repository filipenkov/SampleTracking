package com.atlassian.crowd.event.user;

import com.atlassian.crowd.event.Event;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;

public class UserAuthenticationFailedAccessDeniedEvent extends Event
{
    private final User user;
    private final Application application;

    public UserAuthenticationFailedAccessDeniedEvent(Object source, User user, Application application)
    {
        super(source);
        this.user = user;
        this.application = application;
    }

    public User getRemotePrincipal()
    {
        return user;
    }

    public Application getApplication()
    {
        return application;
    }

}
