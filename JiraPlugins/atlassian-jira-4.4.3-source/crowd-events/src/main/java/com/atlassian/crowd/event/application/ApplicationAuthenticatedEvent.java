package com.atlassian.crowd.event.application;

import com.atlassian.crowd.event.Event;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.token.Token;

public class ApplicationAuthenticatedEvent extends Event
{
    private final Application application;
    private final Token token;

    public ApplicationAuthenticatedEvent(Object source, Application application, Token token)
    {
        super(source);
        this.application = application;
        this.token = token;
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
