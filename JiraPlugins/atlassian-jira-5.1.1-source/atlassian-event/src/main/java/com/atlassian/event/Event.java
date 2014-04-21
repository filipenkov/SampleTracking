package com.atlassian.event;

import org.springframework.context.ApplicationEvent;

/**
 * @since 1.0
 * @deprecated since 2.0, you can now use POJO's using {@link com.atlassian.event.api.EventPublisher}!
 */
public class Event extends ApplicationEvent
{
    public Event(Object source)
    {
        super(source);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Event))
        {
            return false;
        }

        final Event event = (Event) o;

        if (source != null ? !source.equals(event.source) : event.source != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (source != null ? source.hashCode() : 0);
    }
}
