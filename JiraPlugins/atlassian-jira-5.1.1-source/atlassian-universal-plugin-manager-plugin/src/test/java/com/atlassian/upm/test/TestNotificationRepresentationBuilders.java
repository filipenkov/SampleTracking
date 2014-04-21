package com.atlassian.upm.test;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.notification.rest.representations.NotificationGroupRepresentation;
import com.atlassian.upm.notification.rest.representations.NotificationRepresentation;
import com.atlassian.upm.rest.representations.InstalledPluginEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Ignore;

import static com.atlassian.upm.notification.rest.resources.NotificationCollectionResource.DEFAULT_HIDE_DISMISSED;

public class TestNotificationRepresentationBuilders
{
    @Ignore
    public static final class NotificationRepresentationBuilder
    {
        private NotificationType notificationType = NotificationType.PLUGIN_UPDATE_AVAILABLE;
        private InstalledPluginEntry plugin = new InstalledPluginEntry(true, ImmutableMap.<String, URI>of(),
                                                                       "name", false, false, null, "description",
                                                                       "some.plugin", false, null);
        private Boolean dismissed = Boolean.valueOf(DEFAULT_HIDE_DISMISSED);
        private String title = "notification title";
        private String message = "notification message";
        private Map<String, URI> links = ImmutableMap.of();

        // add additional setter methods as needed

        public NotificationRepresentationBuilder dismissed(boolean dismissed)
        {
            this.dismissed = dismissed;
            return this;
        }

        public NotificationRepresentation build()
        {
            return new NotificationRepresentation(notificationType, plugin, dismissed, title, message, links);
        }
    }

    @Ignore
    public static final class NotificationGroupRepresentationBuilder
    {
        private NotificationType notificationType = NotificationType.PLUGIN_UPDATE_AVAILABLE;
        private Collection<NotificationRepresentation> notifications = ImmutableList.of();
        private Boolean dismissed = Boolean.valueOf(DEFAULT_HIDE_DISMISSED);
        private String title = "notification group title";
        private String message = "notification group message";
        private Map<String, URI> links = ImmutableMap.of();

        // add additional setter methods as needed
        
        public NotificationGroupRepresentationBuilder dismissed(boolean dismissed)
        {
            this.dismissed = dismissed;
            return this;
        }

        public NotificationGroupRepresentation build()
        {
            return new NotificationGroupRepresentation(notificationType, notifications, dismissed, title, message, links);
        }
    }
}
