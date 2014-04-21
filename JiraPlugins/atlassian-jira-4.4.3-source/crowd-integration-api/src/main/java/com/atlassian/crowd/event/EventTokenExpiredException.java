package com.atlassian.crowd.event;

import com.atlassian.crowd.exception.CrowdException;

/**
 * Thrown when an event token is either not recognised or has expired.
 */
public class EventTokenExpiredException extends CrowdException
{
    public EventTokenExpiredException()
    {
    }

    public EventTokenExpiredException(String message)
    {
        super(message);
    }
}
