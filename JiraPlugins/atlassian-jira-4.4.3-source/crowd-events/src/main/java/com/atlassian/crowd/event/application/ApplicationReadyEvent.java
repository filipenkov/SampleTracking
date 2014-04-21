package com.atlassian.crowd.event.application;

import com.atlassian.crowd.event.Event;

/**
 * This event is fired when the application has started and is ready. (ie. after ApplicationStartedEvent is fired)
 *
 * This custom event is needed as the ApplicationStartedEvent is a LifecycleEvent
 * and is not handled by listeners that are registered in system-listeners.xml
 */
public class ApplicationReadyEvent extends Event
{
    public ApplicationReadyEvent(Object source)
    {
        super(source);
    }
}