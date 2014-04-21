package com.atlassian.crowd.event;

/**
 * <p>Base event for Crowd <em>legacy</em> events.</p>
 * <p>New events might not want to tie themselves to this event which sole purpose is to keep some semblance of
 * backward compatibility for those events that uses to extends Spring's
 * {@code org.springframework.context.ApplicationEvent}</p>
 */
public abstract class Event
{
    private final Object source;
    private long timestamp;

    public Event(Object source)
    {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    public Object getSource()
    {
        return source;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}
