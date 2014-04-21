package com.atlassian.event.internal;

/**
 * An interface to resolve whether an event can be handled asynchronously or not.
 * @since 2.0
 */
interface AsynchronousEventResolver
{
    /**
     * Tells whether the event can be handled asynchronously or not
     * @param event the event to check
     * @return {@code true} if the event can be handled asynchronously, {@code false} otherwise.
     * @throws NullPointerException if the event is {@code null}
     */
    boolean isAsynchronousEvent(Object event);
}
