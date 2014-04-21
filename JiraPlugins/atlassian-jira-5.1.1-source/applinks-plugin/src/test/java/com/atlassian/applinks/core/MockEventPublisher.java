package com.atlassian.applinks.core;

import com.atlassian.event.api.EventPublisher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MockEventPublisher implements EventPublisher
{
    private final LinkedList<Object> events = new LinkedList<Object>();

    public void publish(Object o)
    {
        events.addLast(o);
    }

    public void register(Object o)
    {

    }

    public void unregister(Object o)
    {

    }

    public void unregisterAll()
    {

    }

    public LinkedList<Object> getEvents()
    {
        return events;
    }

    public <T> T getLastFired(Class<T> type)
    {
        assertTrue(events.size() > 0);
        Object event = events.getLast();
        assertTrue("Last event was of the wrong type: " + event.getClass(), event.getClass().isAssignableFrom(type));
        return (T) event;
    }
}
