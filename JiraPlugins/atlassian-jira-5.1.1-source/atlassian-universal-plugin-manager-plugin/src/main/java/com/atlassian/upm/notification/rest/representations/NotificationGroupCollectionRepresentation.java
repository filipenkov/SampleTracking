package com.atlassian.upm.notification.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Json representation for all notifications at the current system state.
 */
public class NotificationGroupCollectionRepresentation
{
    @JsonProperty private final Collection<NotificationGroupRepresentation> notificationGroups;
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private int totalNotificationCount = 0;

    @JsonCreator
    public NotificationGroupCollectionRepresentation(
        @JsonProperty("notificationGroups") Collection<NotificationGroupRepresentation> notificationGroups,
        @JsonProperty("links") Map<String, URI> links)
    {
        this.notificationGroups = ImmutableList.copyOf(notificationGroups);
        this.links = ImmutableMap.copyOf(links);

        for (NotificationGroupRepresentation notificationGroup : notificationGroups)
        {
            totalNotificationCount += notificationGroup.getNotificationCount();
        }
    }

    public Collection<NotificationGroupRepresentation> getNotificationGroups()
    {
        return notificationGroups;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public int getTotalNotificationCount()
    {
        return totalNotificationCount;
    }
}
