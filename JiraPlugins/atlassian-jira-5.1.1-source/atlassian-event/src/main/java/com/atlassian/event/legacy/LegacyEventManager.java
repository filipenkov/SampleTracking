package com.atlassian.event.legacy;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.event.EventManager;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a legacy event manager that implements the now deprecated {@link com.atlassian.event.EventManager} interface. It relies on the
 * {@link com.atlassian.event.api.EventPublisher} to actually publish the events.
 * @since 2.0.0
 * @deprecated since 2.0.0
 */
public final class LegacyEventManager implements EventManager
{
    private final EventPublisher delegateEventPublisher;

    /**
     * We need to keep this map of legacy listeners with their keys to be able to support un-registring with a specific key.
     */
    private final Map<String, EventListener> legacyListeners = Maps.newHashMap();

    public LegacyEventManager(EventPublisher delegateEventPublisher)
    {
        this.delegateEventPublisher = checkNotNull(delegateEventPublisher);
    }

    public void publishEvent(Event event)
    {
        delegateEventPublisher.publish(checkNotNull(event));
    }

    public void registerListener(String listenerKey, EventListener listener)
    {
        checkListenerKey(listenerKey);
        checkNotNull(listener);

        synchronized (legacyListeners)
        {
            final EventListener registeredListener = legacyListeners.get(listenerKey);
            if (registeredListener != null)
            {
                delegateEventPublisher.unregister(registeredListener);
            }
            legacyListeners.put(listenerKey, listener);
            delegateEventPublisher.register(listener);
        }
    }

    public void unregisterListener(String listenerKey)
    {
        checkListenerKey(listenerKey);
        synchronized (legacyListeners)
        {
            final EventListener listener = legacyListeners.get(listenerKey);
            if (listener != null)
            {
                delegateEventPublisher.unregister(listener);
            }
        }
    }

    private static void checkListenerKey(String listenerKey)
    {
        if (StringUtils.isEmpty(listenerKey))
        {
            throw new IllegalArgumentException("listenerKey must not be empty");
        }
    }
}
