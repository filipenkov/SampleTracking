package com.atlassian.crowd.event;

import com.atlassian.crowd.model.event.OperationEvent;
import com.google.common.collect.Iterables;

public class Events
{
    private final Iterable<OperationEvent> events;

    private final String newEventToken;

    public Events(Iterable<OperationEvent> events, String newEventToken)
    {
        this.events = Iterables.unmodifiableIterable(events);
        this.newEventToken = newEventToken;
    }

    public Iterable<OperationEvent> getEvents()
    {
        return events;
    }

    public String getNewEventToken()
    {
        return newEventToken;
    }
}
