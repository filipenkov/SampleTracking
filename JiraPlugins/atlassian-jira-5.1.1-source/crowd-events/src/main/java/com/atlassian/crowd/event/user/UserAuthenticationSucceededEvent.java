package com.atlassian.crowd.event.user;

import com.atlassian.crowd.event.Event;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.model.user.User;

/**
 * This event is published when a user has successfully logged in to a
 * single-sign-on application.
 */
public class UserAuthenticationSucceededEvent extends Event
{
    private final User user;
    private final Application application;
    private final Token token;

    public UserAuthenticationSucceededEvent(Object source, User user, Application application, Token token)
    {
        super(source);
        this.user = user;
        this.application = application;
        this.token = token;
    }

    public User getRemotePrincipal()
    {
        return user;
    }

    public Application getApplication()
    {
        return application;
    }

    public Token getToken()
    {
        return token;
    }
}
