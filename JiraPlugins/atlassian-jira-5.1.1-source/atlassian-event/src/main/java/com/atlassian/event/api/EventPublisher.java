package com.atlassian.event.api;

/**
 * Interface to publish events. It allows the decoupling of listeners which handle events and
 * publishers which dispatch events.
 * @since 2.0
 * @see EventListener annotation which can be used to indicate event listener methods
 */
public interface EventPublisher
{
    /**
     * Publish an event that will be consumed by all listeners which have registered to receive it.
     * Implementations must dispatch events to listeners which have a public method annotated with
     * {@link EventListener} and one argument which is assignable from the event type (i.e. a superclass
     * or interface implemented by the event object). Implementations may also dispatch events
     * to legacy {@link com.atlassian.event.EventListener} implementations based on the types returned
     * from {@link com.atlassian.event.EventListener#getHandledEventClasses()}.
     *
     * This method should process all event listeners, despite any errors or exceptions that are generated
     * as a result of dispatching the event. 
     *
     * @param event the event to publish
     * @throws NullPointerException if the event is {@code null}
     */
    void publish(Object event);

    /**
     * Register a listener to receive events. All implementations must support registration of listeners
     * where event handling methods are indicated by the {@link EventListener} annotation. Legacy
     * implementations may also support listeners which implement the now-deprecated
     * {@link com.atlassian.event.EventListener} interface.
     *
     * @param listener The listener that is being registered
     * @throws NullPointerException if the listener is {@code null}
     * @throws IllegalArgumentException if the parameter is not found to be an actual listener
     * @see EventListener annotation which can be used to indicate event listener methods
     */
    void register(Object listener);

    /**
     * Un-register a listener so that it will no longer receive events. If the given listener is not registered nothing
     * will happen.
     * @param listener The listener to un-register
     * @throws NullPointerException if the listener is {@code null}
     */
    void unregister(Object listener);

    /**
     * Un-register all listeners that this publisher knows about.
     */
    void unregisterAll();
}
