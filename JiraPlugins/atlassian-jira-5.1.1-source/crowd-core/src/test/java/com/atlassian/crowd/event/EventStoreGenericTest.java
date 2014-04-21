package com.atlassian.crowd.event;

import com.atlassian.crowd.model.event.AbstractOperationEvent;
import com.atlassian.crowd.model.event.OperationEvent;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EventStoreGenericTest
{
    private final OperationEvent EVENT = new AbstractOperationEvent(null, null)
    {
    };
    private final long EVENTS_SIZE = 5;

    private EventStore eventStore;

    @Before
    public void setUp()
    {
        eventStore = new EventStoreGeneric(EVENTS_SIZE);
    }

    @Test
    public void testBasicOperation() throws EventTokenExpiredException
    {
        final String eventToken = eventStore.getCurrentEventToken();
        eventStore.storeEvent(EVENT);
        final Events events = eventStore.getNewEvents(eventToken);

        assertThat(eventStore.getCurrentEventToken(), not(equalTo(eventToken)));
        assertEquals(eventStore.getCurrentEventToken(), events.getNewEventToken());
        assertEquals(EVENT, Iterables.getOnlyElement(events.getEvents()));
    }

    @Test
    public void testNoChanges() throws EventTokenExpiredException
    {
        final String eventToken = eventStore.getCurrentEventToken();
        final Events events1 = eventStore.getNewEvents(eventToken);
        final Events events2 = eventStore.getNewEvents(events1.getNewEventToken());

        assertEquals(eventToken, events1.getNewEventToken());
        assertEquals(eventToken, events2.getNewEventToken());
        assertTrue(Iterables.isEmpty(events1.getEvents()));
        assertTrue(Iterables.isEmpty(events2.getEvents()));
    }

    @Test
    public void testEventsOnLimit() throws EventTokenExpiredException
    {
        final String eventToken = eventStore.getCurrentEventToken();

        for (int i = 0; i < EVENTS_SIZE; ++i)
        {
            eventStore.storeEvent(EVENT);
        }

        final Events events = eventStore.getNewEvents(eventToken);
        assertEquals(EVENTS_SIZE, Iterables.size(events.getEvents()));
    }

    @Test(expected = EventTokenExpiredException.class)
    public void testEventsAboveLimit() throws EventTokenExpiredException
    {
        final String eventToken = eventStore.getCurrentEventToken();

        for (int i = 0; i < EVENTS_SIZE + 1; ++i)
        {
            eventStore.storeEvent(EVENT);
        }

        eventStore.getNewEvents(eventToken);
    }

    @Test(expected = EventTokenExpiredException.class)
    public void testInvalidateEvents() throws Exception
    {
        final String eventToken = eventStore.getCurrentEventToken();

        eventStore.storeEvent(EVENT);

        eventStore.invalidateEvents();

        eventStore.getNewEvents(eventToken);
    }

    @Test
    public void testGetEventsAfterInvalidateEvents() throws Exception
    {
        for (int i = 0; i < EVENTS_SIZE; ++i)
        {
            eventStore.storeEvent(EVENT);
        }

        eventStore.invalidateEvents();

        final String eventToken = eventStore.getCurrentEventToken();

        for (int i = 0; i < EVENTS_SIZE; ++i)
        {
            eventStore.storeEvent(EVENT);
        }

        final Events events = eventStore.getNewEvents(eventToken);
        assertEquals(EVENTS_SIZE, Iterables.size(events.getEvents()));
    }

    @Test(expected = EventTokenExpiredException.class)
    public void testTooManyPartsInEventToken() throws Exception
    {
        eventStore.getNewEvents(eventStore.getCurrentEventToken() + ":0");
    }

    @Test(expected = EventTokenExpiredException.class)
    public void testInvalidInstanceId() throws Exception
    {
        final String[] eventTokenParts = eventStore.getCurrentEventToken().split(":");
        final long wrongInstanceId = Long.valueOf(eventTokenParts[0]) + 1;
        final String eventId = eventTokenParts[1];
        eventStore.getNewEvents(wrongInstanceId + ":" + eventId);
    }
}
