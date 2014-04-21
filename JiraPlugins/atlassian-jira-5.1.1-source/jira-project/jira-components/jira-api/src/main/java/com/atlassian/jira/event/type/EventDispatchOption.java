package com.atlassian.jira.event.type;

import com.atlassian.annotations.PublicApi;

/**
 * Defines an event dispatch strategy to use when updating issues.
 * <p/>
 * A client caller may want to skip dispatching an event or they may want to specify which event should be fired.
 * <p/>
 * The {@link #DO_NOT_DISPATCH} event is to be used if an event should not be dispatched. {@link #ISSUE_UPDATED} is the
 * default behavior and will dispatch a {@link com.atlassian.jira.event.type.EventType#ISSUE_UPDATED_ID} event.
 *
 * @since v4.1
 */
@PublicApi
public interface EventDispatchOption
{
    /**
     * Use this when you do not want an event to be dispatched.
     */
    static final EventDispatchOption DO_NOT_DISPATCH = new EventDispatchOptionImpl();

    /**
     * Use this when you want to dispatch an {@link com.atlassian.jira.event.type.EventType#ISSUE_UPDATED_ID} event.
     */
    static final EventDispatchOption ISSUE_UPDATED = new EventDispatchOptionImpl(EventType.ISSUE_UPDATED_ID);

    /**
     * Use this when you want to dispatch an {@link com.atlassian.jira.event.type.EventType#ISSUE_ASSIGNED_ID} event.
     */
    static final EventDispatchOption ISSUE_ASSIGNED = new EventDispatchOptionImpl(EventType.ISSUE_ASSIGNED_ID);

    /**
     * Use this when you want to dispatch an {@link EventType#ISSUE_DELETED_ID} event.
     */
    static final EventDispatchOption ISSUE_DELETED = new EventDispatchOptionImpl(EventType.ISSUE_DELETED_ID);

    /**
     * This should be false if you do not want to dispatch an event, otherwise the event specified by {@link
     * #getEventTypeId()} will be used.
     */
    boolean isEventBeingSent();

    /**
     * Used to specify the event type this option is configured to express.
     *
     * @return a legal eventTypeId. Must not be null if isEventBeingSent() returns true!
     */
    Long getEventTypeId();

    /**
     * A simple convienience object that will allow you to easily construct a EventDispatchOption.
     */
    public final static class Factory
    {
        /**
         * Creates an EventDispatchOption with the specified eventTypeId.
         *
         * @param id specifies an event type id as found in the {@link EventType} class. This must not be null.
         * @return an EventDispatchOption with the specified eventTypeId.
         */
        public static EventDispatchOption get(Long id)
        {
            return new EventDispatchOptionImpl(id);
        }
    }

    static class EventDispatchOptionImpl implements EventDispatchOption
    {
        private final Long eventTypeId;

        EventDispatchOptionImpl()
        {
            eventTypeId = null;
        }

        EventDispatchOptionImpl(Long eventTypeId)
        {
            if (eventTypeId == null)
            {
                throw new IllegalArgumentException("Use DO_NOT_DISPATCH if you want a null");
            }
            this.eventTypeId = eventTypeId;
        }

        public boolean isEventBeingSent()
        {
            return eventTypeId != null;
        }

        public Long getEventTypeId()
        {
            return eventTypeId;
        }
    }
}