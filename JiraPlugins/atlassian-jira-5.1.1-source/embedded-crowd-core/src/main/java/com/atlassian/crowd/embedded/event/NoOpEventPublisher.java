package com.atlassian.crowd.embedded.event;

import com.atlassian.event.api.EventPublisher;

public class NoOpEventPublisher implements EventPublisher
{
    public void publish(Object event)
    {
    }

    public void register(Object listener)
    {
    }

    public void unregister(Object listener)
    {
    }

    public void unregisterAll()
    {
    }
}