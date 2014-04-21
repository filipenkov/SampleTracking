package com.atlassian.crowd.event;

import com.atlassian.crowd.model.event.OperationEvent;

/**
 * Represents an event store, which can be used to store events. The amount of
 * events store depends on the implementation and is hidden from the callers.
 *
 * @since 2.2
 */
public interface EventStore
{
    /**
     * Returns a token that can be used for querying events that have happened
     * after the token was generated.
     * <p>
     * If the event token has not changed since the last call to this method,
     * it is guaranteed that no new events have been received.
     * <p>
     * The format of event token is implementation specific and can change
     * without a warning.
     *
     * @return token that can be used for querying events that have happened
     * after the token was generated.
     */
    String getCurrentEventToken();

    /**
     * Returns an events object which contains a new eventToken and events that
     * happened after the given {@code eventToken} was generated.
     * <p>
     * If for any reason event store is unable to retrieve events that happened
     * after the event token was generated, an
     * {@link EventTokenExpiredException} will be thrown. The caller is then
     * expected to call {@link #getCurrentEventToken()} again before asking for
     * new events.
     *
     * @param eventToken event token that was retrieved by a call to
     * {@link #getCurrentEventToken()} or {@link #getNewEvents(String)}
     * @return events object which contains a new eventToken and events that
     * happened after the given {@code eventToken} was generated.
     * @throws EventTokenExpiredException if events that happened after the
     * event token was generated can not be retrieved
     */
    Events getNewEvents(String eventToken) throws EventTokenExpiredException;

    /**
     * Stores the given event. Implementations are free to limit the amount
     * of events stored.
     *
     * @param event event to be stored
     */
    void storeEvent(OperationEvent event);

    /**
     * Removes all events from the EventStore.
     */
    void invalidateEvents();
}
