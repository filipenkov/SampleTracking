package com.atlassian.event.legacy;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener handler to deal with legacy {@link com.atlassian.event.EventListener event listeners}
 * @see com.atlassian.event.EventListener
 * @see Event
 * @since 2.0
 */
public class LegacyListenerHandler implements ListenerHandler
{
    public List<? extends ListenerInvoker> getInvokers(Object listener)
    {
        return checkNotNull(listener) instanceof EventListener
                ? getLegacyListenerInvoker((EventListener) listener)
                : Collections.<ListenerInvoker>emptyList();
    }

    private List<? extends ListenerInvoker> getLegacyListenerInvoker(EventListener eventListener)
    {
        return Collections.singletonList(new LegacyListenerInvoker(eventListener));
    }

    private static class LegacyListenerInvoker implements ListenerInvoker
    {
        private final EventListener eventListener;

        public LegacyListenerInvoker(EventListener eventListener)
        {
            this.eventListener = checkNotNull(eventListener);
        }

        public Set<Class<?>> getSupportedEventTypes()
        {
            Class[] classes = eventListener.getHandledEventClasses();
            if (classes.length == 0)
                return Collections.<Class<?>>singleton(Event.class);
            else
                return Sets.<Class<?>>newHashSet(classes);
        }

        public void invoke(Object event)
        {
            eventListener.handleEvent((Event) event);
        }

        public boolean supportAsynchronousEvents()
        {
            return true;
        }
    }
}
