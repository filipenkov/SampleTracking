package com.atlassian.trackback;

import org.apache.commons.lang.exception.NestableException;

public class TrackbackException extends NestableException
{
    private final Trackback ping;

    public TrackbackException(String string)
    {
        super(string);
        ping = null;
    }

    public TrackbackException(Throwable throwable)
    {
        super(throwable);
        ping = null;
    }

    public TrackbackException(Trackback ping, Exception cause)
    {
        super("Error storing trackback ping", cause);
        this.ping = ping;
    }
}
