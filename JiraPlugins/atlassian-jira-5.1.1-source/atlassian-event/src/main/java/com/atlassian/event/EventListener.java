package com.atlassian.event;

/**
 * Defines a listener for events.
 * @since 1.0
 * @deprecated since 2.0, you can now use a POJO annotated with {@link com.atlassian.event.api.EventListener} and the {@link com.atlassian.event.api.EventPublisher}
 * @see com.atlassian.event.api.EventListener
 * @see com.atlassian.event.api.EventPublisher
 */
public interface EventListener
{
    /**
     * Perform some action as a response to a Confluence event. The EventManager will
     * ensure that this is only called if the class of the event matches one of the
     * classes returned by getHandledEventClasses
     * @param event some event triggered within Confluence
     */
    void handleEvent(Event event);

    /**
     * Determine which event classes this listener is interested in.
     *
     * <p>The EventManager performs rudimentary filtering of events by their class. If
     * you want to receive only a subset of events passing through the system, return
     * an array of the Classes you wish to listen for from this method.
     *
     * <p>Listening for a class will also listen for all its subclasses. (And listening
     * for an interface will listen for any implementation of that interface)
     *
     * <p>Returning an empty array will allow you to receive every event.
     * @return An array of the event classes that this event listener is interested in,
     *         or an empty array if the listener should receive all events. <b>Must not</b>
     *         return null.
     */
    Class[] getHandledEventClasses();
}
