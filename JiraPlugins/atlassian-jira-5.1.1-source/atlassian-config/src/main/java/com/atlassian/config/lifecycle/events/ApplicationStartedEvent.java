package com.atlassian.config.lifecycle.events;

/**
 * Event produced when the application has completed starting up, and all life-cycle event plug-ins have had
 * their start-up methods called successfully.
 */
public class ApplicationStartedEvent extends ConfigEvent
{
    public ApplicationStartedEvent(Object object)
    {
        super(object);
    }
}
