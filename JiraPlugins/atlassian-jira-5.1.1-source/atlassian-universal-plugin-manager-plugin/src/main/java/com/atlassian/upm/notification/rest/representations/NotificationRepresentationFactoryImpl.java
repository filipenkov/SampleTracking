package com.atlassian.upm.notification.rest.representations;

import java.net.URI;
import java.util.Collection;

import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.NotificationCollection;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.notification.cache.NotificationCache;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.InstalledPluginEntry;
import com.atlassian.upm.rest.representations.LinkBuilder;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.atlassian.upm.rest.UpmUriEscaper.escape;
import static com.atlassian.upm.rest.representations.InstalledPluginEntry.toEntry;
import static com.atlassian.upm.rest.resources.PluginMediaResource.IMAGES_PUZZLE_PIECE_PNG;
import static com.atlassian.upm.permission.Permission.GET_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.MANAGE_NOTIFICATIONS;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * Builds {@link NotificationGroupCollectionRepresentation}s.
 */
public class NotificationRepresentationFactoryImpl implements NotificationRepresentationFactory
{
    private final NotificationCache notificationCache;
    private final I18nResolver i18nResolver;
    private final UpmUriBuilder uriBuilder;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final LinkBuilder linkBuilder;
    private final UserManager userManager;
    private final WebResourceManager webResourceManager;
    private final PermissionEnforcer permissionEnforcer;

    public NotificationRepresentationFactoryImpl(
        NotificationCache notificationCache,
        I18nResolver i18nResolver,
        UpmUriBuilder uriBuilder,
        PluginAccessorAndController pluginAccessorAndController,
        LinkBuilder linkBuilder,
        UserManager userManager,
        WebResourceManager webResourceManager,
        PermissionEnforcer permissionEnforcer)
    {
        this.notificationCache = checkNotNull(notificationCache, "notificationCache");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.linkBuilder = checkNotNull(linkBuilder, "linkBuilder");
        this.userManager = checkNotNull(userManager, "userManager");
        this.webResourceManager = checkNotNull(webResourceManager, "webResourceManager");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    private String getMessageI18n(NotificationType type, int count)
    {
        return i18nResolver.getText(count == 1 ? type.getSingularMessageI18nKey() : type.getPluralMessageI18nKey(), count, uriBuilder.buildUpmUri());
    }

    @Override
    public NotificationGroupCollectionRepresentation getNotificationGroupCollection(
        Iterable<NotificationCollection> notificationCollections, Option<String> username)
    {
        ImmutableList.Builder<NotificationGroupRepresentation> notificationGroups = ImmutableList.builder();

        //only add notification groups which the current user is permitted to act upon
        for (NotificationCollection notificationCollection : notificationCollections)
        {
            if (permissionEnforcer.hasPermission(notificationCollection.getType().getRequiredPermission()))
            {
                notificationGroups.add(getNotificationGroup(notificationCollection, username));
            }
        }

        //add links
        final LinkBuilder.LinksMapBuilder links;
        if (username.isDefined())
        {
            links = linkBuilder.buildLinkForSelf(uriBuilder.buildNotificationCollectionUri(username.get()));

            for (NotificationType type : NotificationType.values())
            {
                links.putIfPermitted(GET_NOTIFICATIONS, type.getKey() + "-notifications", uriBuilder.buildNotificationCollectionUri(username.get(), type));
            }
        }
        else
        {
            links = linkBuilder.buildLinkForSelf(uriBuilder.buildNotificationCollectionUri());

            //since this is a non-user-specific get, add a link to get the current user's notifications
            links.putIfPermitted(GET_NOTIFICATIONS, "my-notifications", uriBuilder.buildNotificationCollectionUri(userManager.getRemoteUsername()));
        }

        return new NotificationGroupCollectionRepresentation(notificationGroups.build(), links.build());
    }

    @Override
    public NotificationGroupRepresentation getNotificationGroup(NotificationCollection notificationCollection, Option<String> username)
    {
        NotificationType type = notificationCollection.getType();
        //add notifications
        Collection<NotificationRepresentation> notifications = ImmutableList.copyOf(
            transform(notificationCollection, toNotificationRepresentation(username)));

        final Boolean dismissed;
        final LinkBuilder.LinksMapBuilder links;

        //add links
        if (username.isDefined())
        {
            links = linkBuilder.buildLinkForSelf(uriBuilder.buildNotificationCollectionUri(username.get(), type));
            links.putIfPermitted(MANAGE_NOTIFICATIONS, "post-notifications", uriBuilder.buildNotificationCollectionUri(username.get(), type));

            dismissed = notificationCache.isNotificationTypeDismissed(type, username.get());
        }
        else
        {
            links = linkBuilder.builder();

            dismissed = null; //can only dismiss notifications in user-specific contexts
        }

        links.put("default-icon", URI.create(webResourceManager.getStaticPluginResource(
                pluginAccessorAndController.getUpmPluginKey() + ":upm-plugin-resources", IMAGES_PUZZLE_PIECE_PNG, UrlMode.AUTO)))
            .put("target", uriBuilder.buildUpmTabUri("manage"));

        return new NotificationGroupRepresentation(
            type,
            notifications,
            dismissed,
            i18nResolver.getText(type.getTitleI18nKey()),
            getMessageI18n(type, notifications.size()),
            links.build());
    }

    @Override
    public NotificationRepresentation getNotification(Notification notification, Option<String> username)
    {
        return toNotificationRepresentation(username).apply(notification);
    }

    private Function<Notification, NotificationRepresentation> toNotificationRepresentation(Option<String> username)
    {
        return new ToNotificationRepresentation(username);
    }

    private class ToNotificationRepresentation implements Function<Notification, NotificationRepresentation>
    {
        private final Option<String> username;

        ToNotificationRepresentation(Option<String> username)
        {
            this.username = username;
        }

        @Override
        public NotificationRepresentation apply(Notification notification)
        {
            NotificationType type = notification.getType();
            String pluginKey = notification.getPluginKey();
            Plugin plugin = pluginAccessorAndController.getPlugin(pluginKey);

            final Boolean dismissed;
            final LinkBuilder.LinksMapBuilder links;
            if (username.isDefined())
            {
                links = linkBuilder.buildLinkForSelf(uriBuilder.buildNotificationUri(username.get(), type, escape(pluginKey)));
                links.putIfPermitted(GET_NOTIFICATIONS, type.getKey() + "-notifications",
                                     uriBuilder.buildNotificationCollectionUri(username.get(), type));
                links.putIfPermitted(MANAGE_NOTIFICATIONS, "post-notifications",
                                     uriBuilder.buildNotificationUri(username.get(), type, escape(pluginKey)));

                dismissed = notificationCache.isNotificationDismissed(type, username.get(), pluginKey);
            }
            else
            {
                links = linkBuilder.builder();

                dismissed = null;
            }
            links.put("target", uriBuilder.buildUpmTabPluginUri("manage", pluginKey));

            InstalledPluginEntry pluginEntry = toEntry(pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer).apply(plugin);
            return new NotificationRepresentation(type,
                pluginEntry,
                dismissed,
                i18nResolver.getText(type.getTitleI18nKey()),
                i18nResolver.getText(type.getIndividualNotificationI18nKey(), plugin.getName()),
                links.build());
        }
    }
}
