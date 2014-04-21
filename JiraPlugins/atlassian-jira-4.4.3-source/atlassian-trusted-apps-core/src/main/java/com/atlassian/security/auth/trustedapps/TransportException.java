package com.atlassian.security.auth.trustedapps;

public abstract class TransportException extends Exception
{
    private final TransportErrorMessage error;
    
    TransportException(TransportErrorMessage error)
    {
        super(error.getFormattedMessage());
        Null.not("error", error);
        this.error = error;
    }

    TransportException(TransportErrorMessage error, Exception exception)
    {
        super(error.getFormattedMessage(), exception);
        Null.not("exception", exception);
        this.error = error;
    }

    public final TransportErrorMessage getTransportErrorMessage()
    {
        return error;
    }
}