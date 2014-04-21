package com.atlassian.johnson;

import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.setup.ContainerFactory;

import java.util.*;
import javax.servlet.ServletContext;

public class JohnsonEventContainer
{
    public static String SERVLET_CONTEXT_KEY = JohnsonEventContainer.class.getName();

    public synchronized static JohnsonEventContainer get(ServletContext context)
    {
        if (context.getAttribute(SERVLET_CONTEXT_KEY) == null)
        {
            final JohnsonConfig johnsonConfig = JohnsonConfig.getInstance();
            final ContainerFactory containerFactory = johnsonConfig.getContainerFactory();
            context.setAttribute(SERVLET_CONTEXT_KEY, containerFactory.create());
        }

        return (JohnsonEventContainer) context.getAttribute(SERVLET_CONTEXT_KEY);
    }

    /* a WeakHashMap so that the garbage collector will destroy it if there are no links left to it.
    This may be needed if the user presses the Stop button while an action is performing*/
    private final Map<Event, Event> events = new WeakHashMap<Event, Event>();


    /**
     * Determine if there are Application Error Events
     * @return true if there are application errors otherwise false
     */
    public synchronized boolean hasEvents()
    {
        return !events.isEmpty();
    }

    /**
     * Adds an application event to the events Map
     * @param event New Event
     */
    public synchronized void addEvent(Event event)
    {
        events.put(event, event);
    }

    /**
     * Removes an application event from the events Map
     * @param event Event to be removed
     */
    public synchronized void removeEvent(Event event)
    {
        events.remove(event);
    }

    /**
     * Gets a collection of the Event objects
     * @return an unmodifiable connection 
     */
    public synchronized Collection getEvents()
    {
        //prevent concurrent modification - clone the values before returning them.
        return Collections.unmodifiableCollection(new ArrayList<Event>(events.values()));
    }
}
