package com.atlassian.upm.notification.rest.representations;

import java.net.URI;
import java.util.Map;

import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.rest.representations.InstalledPluginEntry;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Json representation for a single notification.
 */
public class NotificationRepresentation
{
    @JsonProperty private final String notificationType;
    @JsonProperty private final InstalledPluginEntry plugin;
    @JsonProperty private final Boolean dismissed;
    @JsonProperty private final String title;
    @JsonProperty private final String message;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    public NotificationRepresentation(
        @JsonProperty("notificationType") String notificationType,
        @JsonProperty("plugin") InstalledPluginEntry plugin,
        @JsonProperty("dismissed") Boolean dismissed,
        @JsonProperty("title") String title,
        @JsonProperty("message") String message,
        @JsonProperty("links") Map<String, URI> links)
    {
        this(NotificationType.fromKey(notificationType), plugin, dismissed, title, message, links);
    }

    public NotificationRepresentation(
        NotificationType notificationType,
        InstalledPluginEntry plugin,
        Boolean dismissed,
        String title,
        String message,
        Map<String, URI> links)
    {
        this.notificationType = checkNotNull(notificationType, "notificationType").getKey();
        this.plugin = checkNotNull(plugin, "plugin");
        this.dismissed = dismissed; //will be null if not user-specific
        this.title = checkNotNull(title, "title");
        this.message = checkNotNull(message, "message");
        this.links = ImmutableMap.copyOf(links);
    }

    public String getNotificationType()
    {
        return notificationType;
    }

    public InstalledPluginEntry getPlugin()
    {
        return plugin;
    }

    public Boolean isDismissed()
    {
        return dismissed;
    }

    public String getTitle()
    {
        return title;
    }

    public String getMessage()
    {
        return message;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }
}
