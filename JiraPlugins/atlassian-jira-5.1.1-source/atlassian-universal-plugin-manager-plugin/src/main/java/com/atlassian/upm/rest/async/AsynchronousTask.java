package com.atlassian.upm.rest.async;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class AsynchronousTask<T extends TaskStatus> implements Callable<URI>
{
    protected static final int DEFAULT_POLL_DELAY_MS = 100;

    protected final String id;
    protected volatile T status;
    protected final Type type;
    protected final Date timestamp;
    protected final String username;

    public enum Type
    {
        CANCELLABLE,
        PLUGIN_INSTALL,
        UPDATE_ALL;
    }

    public AsynchronousTask(Type type, String username)
    {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.username = username;
        this.timestamp = new DateTime(DateTimeZone.UTC).toDate();
    }

    public String getId()
    {
        return id;
    }

    public int getPollDelay()
    {
        return DEFAULT_POLL_DELAY_MS;
    }

    public final Type getType()
    {
        return type;
    }

    public String getUsername()
    {
        return username;
    }

    public Representation<T> getRepresentation(UpmUriBuilder uriBuilder)
    {
        return new Representation<T>(this, uriBuilder);
    }

    /**
     * Accept the task, setting up any initial state. This is called before the task
     * is actually executed by calling {@code run}
     */
    public abstract void accept();

    public final static class Representation<T extends TaskStatus>
    {
        @JsonProperty private final Type type;
        @JsonProperty private final Integer pingAfter;
        @JsonProperty private final T status;
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final String username;
        @JsonProperty private final Date timestamp;

        @JsonCreator
        public Representation(@JsonProperty("type") Type type,
            @JsonProperty("pingAfter") Integer pingAfter,
            @JsonProperty("status") T status,
            @JsonProperty("links") Map<String, URI> links,
            @JsonProperty("timestamp") Date timestamp,
            @JsonProperty("username") String username)
        {
            this.type = type;
            this.pingAfter = pingAfter;
            this.status = status;
            this.links = links;
            this.timestamp = timestamp;
            this.username = username;
        }

        public Representation(AsynchronousTask<T> task, UpmUriBuilder uriBuilder)
        {
            this.type = task.type;
            this.status = task.status;
            this.username = task.username;
            this.timestamp = task.timestamp;
            this.pingAfter = status == null || !status.isDone() ? task.getPollDelay() : null;
            this.links = ImmutableMap.of("self", uriBuilder.buildPendingTaskUri(task.id));
        }

        public Integer getPingAfter()
        {
            return pingAfter;
        }

        public Type getType()
        {
            return type;
        }

        public T getStatus()
        {
            return status;
        }

        public String getUsername()
        {
            return username;
        }

        public Date getTimestamp()
        {
            return timestamp;
        }

        public String getContentType()
        {
            return status.getContentType();
        }

        public URI getSelf()
        {
            return links.get("self");
        }
    }
}

