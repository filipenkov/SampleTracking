package com.atlassian.event.spi;

/**
 * Dispatches an event to its listener (through the invoker). Implementations can choose for example whether to dispatch
 * events asynchronously.
 * @since 2.0
 */
public interface EventDispatcher
{
    /**
     * Dispatches the event using the invoker.
     * @param invoker the invoker to use to dispatch the event
     * @param event the event to dispatch
     * @throws NullPointerException if either the {@code invoker} or the {@code event} is {@code null}
     */
    void dispatch(ListenerInvoker invoker, Object event);
}
