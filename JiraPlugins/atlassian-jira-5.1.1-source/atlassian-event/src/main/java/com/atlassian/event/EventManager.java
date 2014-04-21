package com.atlassian.event;

/**
 * Interface to manage events. It essentially allows to register event listeners and publish events.
 * @since 1.0
 * @deprecated since 2.0, use {@link com.atlassian.event.api.EventPublisher}
 */
public interface EventManager
{
    /**
     * Publish an event that will be consumed by all listeners which have registered to receive it.
     * @param event the event to publish
     * @throws NullPointerException if the event is {@code null}
     */
    void publishEvent(Event event);

    /**
     * Register a listener to receive events. If you register a listener with the same key as an existing listener,
     * the previous listener with that key will be unregistered.
     * @param listenerKey A unique key for this listener. If the listener is a plugin module, use the
     * module's complete key
     * @param listener The listener that is being registered
     * @throws NullPointerException if the listenerKey or listener is {@code null}
     * @throws IllegalArgumentException if the listener key is empty (i.e. {@code null} or empty {@link String})
     */
    void registerListener(String listenerKey, EventListener listener);

    /**
     * Un-register a listener so that it will no longer receive events. If no listener is registered under this key,
     * nothing will happen.
     * @param listenerKey the key under which the listener was registered.
     * @throws IllegalArgumentException if the listener key is empty (i.e. {@code null} or empty {@link String})
     */
    void unregisterListener(String listenerKey);
}
