package com.atlassian.crowd.event;

import com.atlassian.crowd.model.event.AbstractOperationEvent;
import com.atlassian.crowd.model.event.OperationEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Thread-safe {@link EventStore} implementation that uses main memory as a
 * backing store. The amount of events stored is fixed, and after reaching the
 * limit, the oldest events will start to get dropped.
 */
public final class EventStoreGeneric implements EventStore
{
    private final static String SEPARATOR = ":";
    private final static OperationEvent RESET_EVENT = new AbstractOperationEvent(null, null) { };
    private final static Random random = new Random();

    private final long maxStoredEvents;
    private final ConcurrentNavigableMap<Long, OperationEvent> events = new ConcurrentSkipListMap<Long, OperationEvent>();
    private final String instanceId = String.valueOf(random.nextLong());

    private long eventNumber = 0;

    public String getCurrentEventToken()
    {
        return toEventToken(events.lastKey());
    }

    /**
     * Creates a new EventStoreGeneric instance.
     *
     * @param maxStoredEvents maximum amount of events to keep in the memory
     */
    public EventStoreGeneric(long maxStoredEvents)
    {
        this.maxStoredEvents = maxStoredEvents + 1;
        storeEvent(RESET_EVENT);
    }

    public Events getNewEvents(String eventToken) throws EventTokenExpiredException
    {
        final Long currentEventNumber = toEventNumber(eventToken);
        final Iterator<Map.Entry<Long, OperationEvent>> eventsSince = events.tailMap(currentEventNumber).entrySet().iterator();

        if (!eventsSince.hasNext() || !eventsSince.next().getKey().equals(currentEventNumber))
        {
            throw new EventTokenExpiredException();
        }

        final List<OperationEvent> events = new ArrayList<OperationEvent>();
        Long newEventNumber = currentEventNumber;
        while (eventsSince.hasNext())
        {
            final Map.Entry<Long, OperationEvent> eventEntry = eventsSince.next();
            final OperationEvent event = eventEntry.getValue();
            if (event == RESET_EVENT)
            {
                throw new EventTokenExpiredException();
            }
            newEventNumber = eventEntry.getKey();
            events.add(eventEntry.getValue());
        }
        return new Events(events, toEventToken(newEventNumber));
    }

    public synchronized void storeEvent(OperationEvent event)
    {
        final long currentEventNumber = ++eventNumber;

        // Keep only fixed amount of events
        if (currentEventNumber > maxStoredEvents)
        {
            events.remove(events.firstKey());
        }

        events.put(currentEventNumber, event);
    }

    public void invalidateEvents()
    {
        storeEvent(RESET_EVENT);
    }

    private Long toEventNumber(String eventToken) throws EventTokenExpiredException
    {
        final String[] parts = eventToken.split(SEPARATOR);
        if (parts.length != 2 || !parts[0].equals(instanceId))
        {
            throw new EventTokenExpiredException();
        }
        return Long.valueOf(parts[1]);
    }

    private String toEventToken(long eventNumber)
    {
        return instanceId + SEPARATOR + eventNumber;
    }
}
