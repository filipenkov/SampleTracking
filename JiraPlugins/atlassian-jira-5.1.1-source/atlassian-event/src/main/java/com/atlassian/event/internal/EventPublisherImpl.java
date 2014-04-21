package com.atlassian.event.internal;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import static org.apache.commons.lang.ObjectUtils.identityToString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>The default implementation of the {@link com.atlassian.event.api.EventPublisher} interface.</p>
 * <p>
 * <p>One can customise the event listening by instantiating with custom
 * {@link com.atlassian.event.spi.ListenerHandler listener handlers} and the event dispatching through
 * {@link com.atlassian.event.spi.EventDispatcher}. See the {@link com.atlassian.event.spi} package
 * for more information.</p>
 * @see com.atlassian.event.spi.ListenerHandler
 * @see com.atlassian.event.spi.EventDispatcher
 * @since 2.0
 */
public final class EventPublisherImpl implements EventPublisher
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EventDispatcher eventDispatcher;
    private final List<ListenerHandler> listenerHandlers;

    /**
     * <strong>Note:</strong> this field makes this implementation stateful
     */
    private final Multimap<Class<?>, KeyedListenerInvoker> listenerInvokers;

    /**
     * <p>If you need to customise the asynchronous handling, you should use the
     * {@link com.atlassian.event.internal.AsynchronousAbleEventDispatcher} together with a custom executor. You might
     * also want to have a look at using the {@link com.atlassian.event.internal.EventThreadFactory} to keep the naming
     * of event threads consistent with the default naming of the Atlassian Event library.<p>
     * @param eventDispatcher the event dispatcher to be used with the publisher
     * @param listenerHandlersConfiguration the list of listener handlers to be used with this publisher
     * @see com.atlassian.event.internal.AsynchronousAbleEventDispatcher
     * @see com.atlassian.event.internal.EventThreadFactory
     */
    public EventPublisherImpl(EventDispatcher eventDispatcher, ListenerHandlersConfiguration listenerHandlersConfiguration)
    {
        this.eventDispatcher = checkNotNull(eventDispatcher);
        this.listenerHandlers = checkNotNull(checkNotNull(listenerHandlersConfiguration).getListenerHandlers());
        this.listenerInvokers = newMultimap();
    }

    public void publish(Object event)
    {
        invokeListeners(findListenerInvokersForEvent(checkNotNull(event)), event);
    }

    public void register(Object listener)
    {
        registerListener(identityToString(checkNotNull(listener)), listener);
    }

    public void unregister(Object listener)
    {
        unregisterListener(identityToString(checkNotNull(listener)));
    }

    public void unregisterAll()
    {
        synchronized (listenerInvokers)
        {
            listenerInvokers.clear();
        }
    }

    private void unregisterListener(String listenerKey)
    {
        checkArgument(isNotEmpty(listenerKey), "Key for the listener must not be empty");

        /** see {@link Multimaps#synchronizedMultimap(Multimap)} for why this synchronize block is there */
        synchronized (listenerInvokers)
        {
            for (Iterator<Map.Entry<Class<?>, KeyedListenerInvoker>> invokerIterator = listenerInvokers.entries().iterator(); invokerIterator.hasNext();)
            {
                if (invokerIterator.next().getValue().getKey().equals(listenerKey))
                {
                    invokerIterator.remove();
                }
            }
        }
    }

    private void registerListener(String listenerKey, Object listener)
    {
        synchronized (listenerInvokers) /* Because we need to un-register an re-register in one 'atomic' operation */
        {
            unregisterListener(listenerKey);

            final List<ListenerInvoker> invokers = Lists.newArrayList();
            for (ListenerHandler listenerHandler : listenerHandlers)
            {
                invokers.addAll(listenerHandler.getInvokers(listener));
            }
            if (!invokers.isEmpty())
            {
                registerListenerInvokers(listenerKey, invokers);
            }
            else
            {
                throw new IllegalArgumentException("No listener invokers were found for listener <" + listener + ">");
            }
        }
    }

    private Set<KeyedListenerInvoker> findListenerInvokersForEvent(Object event)
    {
        final Set<KeyedListenerInvoker> invokersForEvent = Sets.newHashSet();
        /** see {@link Multimaps#synchronizedMultimap(Multimap)} for why this synchronize block is there */
        synchronized (listenerInvokers)
        {
            for (Class<?> eventClass : ClassUtils.findAllTypes(checkNotNull(event).getClass()))
            {
                invokersForEvent.addAll(listenerInvokers.get(eventClass));
            }
        }
        return invokersForEvent;
    }

    private void invokeListeners(Collection<KeyedListenerInvoker> listenerInvokers, Object event)
    {
        for (KeyedListenerInvoker keyedInvoker : listenerInvokers)
        {
            // EVENT-14 -  we should continue to process all listeners even if one throws some horrible exception
            try
            {
                eventDispatcher.dispatch(keyedInvoker.getInvoker(), event);
            }
            catch(Throwable t)
            {
                log.error("There was an exception thrown trying to dispatch event '" + event + "' from the invoker '" + keyedInvoker.getInvoker() + "'.", t);
            }
        }
    }

    private void registerListenerInvokers(String listenerKey, List<? extends ListenerInvoker> invokers)
    {
        for (ListenerInvoker invoker : invokers)
        {
            registerListenerInvoker(listenerKey, invoker);
        }
    }

    private void registerListenerInvoker(String listenerKey, ListenerInvoker invoker)
    {
        // if supported classes is empty, then all events are supported.
        if (invoker.getSupportedEventTypes().isEmpty())
        {
            listenerInvokers.put(Object.class, new KeyedListenerInvoker(listenerKey, invoker));
        }

        // if it it empty, we won't loop, otherwise register the invoker against all its classes
        for (Class<?> eventClass : invoker.getSupportedEventTypes())
        {
            listenerInvokers.put(eventClass, new KeyedListenerInvoker(listenerKey, invoker));
        }
    }

    private Multimap<Class<?>, KeyedListenerInvoker> newMultimap()
    {
        return Multimaps.synchronizedMultimap(
                Multimaps.newMultimap(Maps.<Class<?>, Collection<KeyedListenerInvoker>>newHashMap(),
                        new Supplier<Collection<KeyedListenerInvoker>>()
                        {
                            public Collection<KeyedListenerInvoker> get()
                            {
                                return Sets.newHashSet();
                            }
                        }));
    }

    private static final class KeyedListenerInvoker
    {
        private final String key;
        private final ListenerInvoker invoker;

        KeyedListenerInvoker(String key, ListenerInvoker invoker)
        {
            this.invoker = invoker;
            this.key = key;
        }

        String getKey()
        {
            return key;
        }

        ListenerInvoker getInvoker()
        {
            return invoker;
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(5, 23).append(key).append(invoker).toHashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj == null || obj.getClass() != getClass())
            {
                return false;
            }
            final KeyedListenerInvoker kli = (KeyedListenerInvoker) obj;
            return new EqualsBuilder().append(key, kli.key).append(invoker, kli.invoker).isEquals();
        }
    }
}