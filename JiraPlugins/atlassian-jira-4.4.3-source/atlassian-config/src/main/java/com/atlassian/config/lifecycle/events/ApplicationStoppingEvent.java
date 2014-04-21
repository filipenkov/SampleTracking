package com.atlassian.config.lifecycle.events;

/**
 * Event produced when the application is about to shut down, before any shutdown life-cycle
 * plug-ins are run.
 */
public class ApplicationStoppingEvent extends ConfigEvent
{
    public ApplicationStoppingEvent(Object object)
    {
        super(object);
    }
}
