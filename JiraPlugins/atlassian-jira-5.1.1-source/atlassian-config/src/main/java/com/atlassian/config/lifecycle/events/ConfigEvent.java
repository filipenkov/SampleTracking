package com.atlassian.config.lifecycle.events;

/**
 * <p>Base event for Atlassian Config's <em>legacy</em> events.</p>
 * <p>New events might not want to tie themselves to this event which sole purpose is to keep some semblance of
 * backward compatibility for those events that uses to extends Spring's
 * {@code org.springframework.context.ApplicationEvent}</p>
 */
abstract class ConfigEvent implements LifecycleEvent
{
    private final Object source;
    private long timestamp;

    public ConfigEvent(Object source)
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
