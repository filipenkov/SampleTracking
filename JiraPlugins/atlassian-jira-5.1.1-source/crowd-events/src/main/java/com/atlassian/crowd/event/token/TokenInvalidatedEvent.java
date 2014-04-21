package com.atlassian.crowd.event.token;

import com.atlassian.crowd.event.Event;
import com.atlassian.crowd.model.token.Token;

/**
 * An Event that's fired when a {@link com.atlassian.crowd.model.token.Token} is invalidated. This occurs when a user or
 * application logs off.
 */
public class TokenInvalidatedEvent extends Event
{
    private final Token token;

    public TokenInvalidatedEvent(Object source, Token token)
    {
        super(source);
        this.token = token;
    }

    public Token getToken()
    {
        return token;
    }
}
