package com.atlassian.config.lifecycle.events;

/**
 * Event produced when the application has shut down, and all the life-cycle plug-ins have had their
 * shutdown methods called.
 */
public class ApplicationStoppedEvent extends ConfigEvent
{
    public ApplicationStoppedEvent(Object object)
    {
        super(object);
    }
}
