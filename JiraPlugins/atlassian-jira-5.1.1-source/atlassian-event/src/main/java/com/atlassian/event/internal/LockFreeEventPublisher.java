package com.atlassian.event.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;
import com.atlassian.util.concurrent.NotNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * A non-blocking implementation of the {@link com.atlassian.event.api.EventPublisher} interface.
 * <p>
 * This class is a drop-in replacement for {@link EventPublisherImpl} except that it does not
 * synchronise on the internal map of event type to {@link ListenerInvoker}, and should handle
 * much higher parallelism of event dispatch.
 * <p>
 * One can customise the event listening by instantiating with custom
 * {@link com.atlassian.event.spi.ListenerHandler listener handlers} and the event dispatching through
 * {@link com.atlassian.event.spi.EventDispatcher}. See the {@link com.atlassian.event.spi} package
 * for more information.
 * 
 * @see com.atlassian.event.spi.ListenerHandler
 * @see com.atlassian.event.spi.EventDispatcher
 * @since 2.0.2
 */
public final class LockFreeEventPublisher implements EventPublisher
{
    /**
     * Gets the {@link ListenerInvoker invokers} for a listener
     */
    private final InvokerBuilder invokerBuilder;

    /**
     * Publishes an event.
     */
    private final Publisher publisher;

    /**
     * <strong>Note:</strong> this field makes this implementation stateful
     */
    private final Listeners listeners = new Listeners();

    /**
     * If you need to customise the asynchronous handling, you should use the
     * {@link com.atlassian.event.internal.AsynchronousAbleEventDispatcher} 
     * together with a custom executor.
     * <p>
     * You might also want to have a look at using the 
     * {@link com.atlassian.event.internal.EventThreadFactory} to keep the naming
     * of event threads consistent with the default naming of the Atlassian Event 
     * library.
     * 
     * @param eventDispatcher the event dispatcher to be used with the publisher
     * @param listenerHandlersConfiguration the list of listener handlers to be used with this publisher
     * 
     * @see com.atlassian.event.internal.AsynchronousAbleEventDispatcher
     * @see com.atlassian.event.internal.EventThreadFactory
     */
    public LockFreeEventPublisher(final EventDispatcher eventDispatcher, final ListenerHandlersConfiguration listenerHandlersConfiguration)
    {
        invokerBuilder = new InvokerBuilder(checkNotNull(listenerHandlersConfiguration).getListenerHandlers());
        publisher = new Publisher(eventDispatcher, listeners);
    }

    public void publish(final @NotNull Object event)
    {
        checkNotNull(event);
        publisher.dispatch(event);
    }

    public void register(final @NotNull Object listener)
    {
        checkNotNull(listener);
        listeners.register(listener, invokerBuilder.build(listener));
    }

    public void unregister(final @NotNull Object listener)
    {
        checkNotNull(listener);
        listeners.remove(listener);
    }

    public void unregisterAll()
    {
        listeners.clear();
    }

    //
    // inner classes
    //

    /**
     * Maps classes to the relevant {@link Invokers}
     */
    static final class Listeners
    {
        /**
         * We always want an {@link Invokers} created for any class requested, even if it is empty.
         */
        private final ConcurrentMap<Class<?>, Invokers> invokers = new MapMaker().makeComputingMap(new Function<Class<?>, Invokers>()
        {
            public Invokers apply(final Class<?> from)
            {
                return new Invokers();
            }
        });

        void register(final Object listener, final Iterable<ListenerInvoker> invokers)
        {
            for (final ListenerInvoker invoker : invokers)
            {
                register(listener, invoker);
            }
        }

        private void register(final Object listener, final ListenerInvoker invoker)
        {
            // if supported classes is empty, then all events are supported.
            if (invoker.getSupportedEventTypes().isEmpty())
            {
                invokers.get(Object.class).add(listener, invoker);
            }
            else
            {
                // if it it empty, we won't loop, otherwise register the invoker against all its classes
                for (final Class<?> eventClass : invoker.getSupportedEventTypes())
                {
                    invokers.get(eventClass).add(listener, invoker);
                }
            }
        }

        void remove(final Object listener)
        {
            for (final Invokers entry : invokers.values())
            {
                entry.remove(listener);
            }
        }

        void clear()
        {
            invokers.clear();
        }

        public Iterable<ListenerInvoker> get(final Class<?> eventClass)
        {
            return invokers.get(eventClass).all();
        }
    }

    /**
     * map of Key to Set of ListenerInvoker
     */
    static final class Invokers
    {
        private final ConcurrentMap<Object, ListenerInvoker> listeners = new MapMaker().weakKeys().makeMap();

        Iterable<ListenerInvoker> all()
        {
            return listeners.values();
        }

        public void remove(final Object key)
        {
            listeners.remove(key);
        }

        public void add(final Object key, final ListenerInvoker invoker)
        {
            listeners.put(key, invoker);
        }
    }

    /**
     * Responsible for publishing an event.
     * <p>
     * Must first get the Set of all ListenerInvokers that 
     * are registered for that event and then use the 
     * {@link EventDispatcher} to send the event to them.
     */
    static final class Publisher
    {
        private final Logger log = LoggerFactory.getLogger(this.getClass());
        private final Listeners listeners;
        private final EventDispatcher dispatcher;

        /**
         * transform an event class into the relevant invokers
         */
        @SuppressWarnings("unchecked")
        private final Function<Class, Iterable<ListenerInvoker>> eventClassToInvokersTransformer = new Function<Class, Iterable<ListenerInvoker>>()
        {
            public Iterable<ListenerInvoker> apply(final Class eventClass)
            {
                return listeners.get(eventClass);
            }
        };

        Publisher(final EventDispatcher dispatcher, final Listeners listeners)
        {
            this.dispatcher = checkNotNull(dispatcher);
            this.listeners = checkNotNull(listeners);
        }

        public void dispatch(final Object event)
        {
            for (final ListenerInvoker invoker : getInvokers(event))
            {
                // EVENT-14 -  we should continue to process all listeners even if one throws some horrible exception
                try
                {
                    dispatcher.dispatch(invoker, event);
                }
                catch(Throwable t)
                {
                    log.error("There was an exception thrown trying to dispatch event '" + event + "' from the invoker '" + invoker + "'.", t);
                }
            }
        }

        /**
         * Get all classes and interfaces an object extends or implements and then find all ListenerInvokers that apply
         * @param event to find its classes/interfaces
         * @return an iterable of the invokers for those classes.
         */
        Iterable<ListenerInvoker> getInvokers(final Object event)
        {
            final Set<Class<?>> allEventTypes = ClassUtils.findAllTypes(event.getClass());
            return ImmutableSet.copyOf(concat(transform(allEventTypes, eventClassToInvokersTransformer)));
        }
    };

    /**
     * Holds all configured {@link ListenerHandler handlers}
     */
    static final class InvokerBuilder
    {
        private final Iterable<ListenerHandler> listenerHandlers;

        InvokerBuilder(final @NotNull Iterable<ListenerHandler> listenerHandlers)
        {
            this.listenerHandlers = checkNotNull(listenerHandlers);
        }

        Iterable<ListenerInvoker> build(final Object listener) throws IllegalArgumentException
        {
            final ImmutableList.Builder<ListenerInvoker> builder = ImmutableList.builder();
            for (final ListenerHandler listenerHandler : listenerHandlers)
            {
                builder.addAll(listenerHandler.getInvokers(listener));
            }
            final List<ListenerInvoker> invokers = builder.build();
            if (invokers.isEmpty())
            {
                throw new IllegalArgumentException("No listener invokers were found for listener <" + listener + ">");
            }
            return invokers;
        }
    }
}