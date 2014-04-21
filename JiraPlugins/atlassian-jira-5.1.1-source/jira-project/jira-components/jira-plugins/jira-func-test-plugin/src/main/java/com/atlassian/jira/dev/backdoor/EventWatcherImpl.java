package com.atlassian.jira.dev.backdoor;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.extension.Startable;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @since v5.0
 */
public class EventWatcherImpl implements EventWatcher, Startable
{
    private final EventPublisher eventPublisher;
    private final CircularFifoBuffer events = new CircularFifoBuffer();

    public EventWatcherImpl(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void listen(final Object object)
    {
        synchronized (events)
        {
            events.add(new Event(object));
        }
    }

    public Collection<String> getEvents()
    {
        synchronized (events)
        {
            @SuppressWarnings ({ "unchecked" })
            Collection<String> transform = Collections2.<Event, String>transform(events, Functions.toStringFunction());

            return ImmutableList.copyOf(transform);
        }
    }

    @XmlType
    public static class Event
    {
        @XmlElement
        public String timestamp;
        @XmlElement
        public String source;
        @XmlElement
        public String description;

        public Event()
        {
        }

        public Event(final Object event)
        {
            this.source = event.getClass().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            this.timestamp = sdf.format(new Date());

            //Runtime exceptions can be thrown when plugins and/or JIRA are going down.
            try
            {
                this.description = ToStringBuilder.reflectionToString(event);
            }
            catch (RuntimeException ignored1)
            {
                try
                {
                    this.description = event.toString();
                }
                catch (RuntimeException ignored2)
                {
                    this.description = String.format("%s@%d", event.getClass().getName(), System.identityHashCode(event));
                }
            }
        }

        @Override
        public String toString()
        {
            return String.format("%s: [%s] %s", timestamp, source, description);
        }
    }
}
