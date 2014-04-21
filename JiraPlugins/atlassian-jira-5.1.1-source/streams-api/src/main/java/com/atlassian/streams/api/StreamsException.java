package com.atlassian.streams.api;

/**
 * {@link RuntimeException} thrown when an error is encountered in processing a request for an activity stream
 */
public class StreamsException extends RuntimeException
{
    public StreamsException()
    {
    }

    public StreamsException(String s)
    {
        super(s);
    }

    public StreamsException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public StreamsException(Throwable throwable)
    {
        super(throwable);
    }
}
