package com.atlassian.crowd.event;

import com.atlassian.crowd.exception.CrowdException;

public class IncrementalSynchronisationNotAvailableException extends CrowdException
{
    public IncrementalSynchronisationNotAvailableException(String message)
    {
        super(message);
    }
}
