package com.atlassian.johnson;

import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import junit.framework.TestCase;

import java.util.Collection;

public class TestJohnsonEventContainer extends TestCase
{
    public void testContainer()
    {
        // no events at the start!
        JohnsonEventContainer container = new JohnsonEventContainer();
        assertFalse(container.hasEvents());

        // add an event and check it exists
        Event event = new Event(new EventType("systemic", "Systemic Anomaly"), "There is an anomaly in the matrix");
        container.addEvent(event);
        assertTrue(container.hasEvents());
        Collection containerEvents = container.getEvents();
        assertEquals(1, containerEvents.size());
        assertTrue(containerEvents.contains(event));

        // now remove the event and check it's gone
        container.removeEvent(event);
        assertFalse(container.hasEvents());
        assertEquals(0, container.getEvents().size());
    }
}
