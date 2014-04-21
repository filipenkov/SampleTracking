package com.atlassian.upm.notification.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.cache.NotificationCache;
import com.atlassian.upm.notification.rest.representations.NotificationRepresentation;
import com.atlassian.upm.notification.rest.representations.NotificationRepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.atlassian.upm.notification.NotificationType;

import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static com.atlassian.upm.rest.MediaTypes.UPM_JSON;
import static com.atlassian.upm.permission.Permission.GET_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.MANAGE_NOTIFICATIONS;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Provides a REST resource for individual notifications
 */
@Path("/notifications/{username}/{typeKey}/{pluginKey}")
public class NotificationResource
{
    private final NotificationRepresentationFactory notificationRepresentationFactory;
    private final NotificationCache cache;
    private final PermissionEnforcer permissionEnforcer;
    private final UserManager userManager;
    private final PluginAccessorAndController pluginAccessorAndController;

    public NotificationResource(NotificationRepresentationFactory notificationRepresentationFactory,
                                NotificationCache cache,
                                PermissionEnforcer permissionEnforcer,
                                UserManager userManager,
                                PluginAccessorAndController pluginAccessorAndController)
    {
        this.notificationRepresentationFactory = checkNotNull(notificationRepresentationFactory, "notificationRepresentationFactory");
        this.cache = checkNotNull(cache, "cache");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.userManager = checkNotNull(userManager, "userManager");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    /**
     * Returns a single notification, if one exists, for the given notification type and plugin key.
     *
     * @param username the user wanting to see the notification (must be the currently logged in user)
     * @param typeKey the notification type
     * @param pluginKey the plugin
     * @return a single notification, if one exists, for the given notification type and plugin key.
     */
    @GET
    @Produces(UPM_JSON)
    public Response getNotification(@PathParam("username") String username,
                                    @PathParam("typeKey") String typeKey,
                                    @PathParam("pluginKey") String pluginKey)
    {
        permissionEnforcer.enforcePermission(GET_NOTIFICATIONS);
        pluginKey = unescape(pluginKey);

        NotificationType type = NotificationType.fromKey(typeKey);
        if (type == null)
        {
            return Response.status(BAD_REQUEST).build();
        }
        else if (username == null || !username.equals(userManager.getRemoteUsername()))
        {
            return Response.status(FORBIDDEN).build();
        }
        else if (!permissionEnforcer.hasPermission(type.getRequiredPermission(), pluginAccessorAndController.getPlugin(pluginKey)))
        {
            return Response.status(UNAUTHORIZED).build();
        }

        for (Notification notification : cache.getNotification(type, username, pluginKey))
        {
            return Response.ok(notificationRepresentationFactory.getNotification(notification, some(username))).build();
        }

        //no such notification exists
        return Response.status(NOT_FOUND).build();
    }


    /**
     * Updates the notification state for the given notification. Can be used to dismiss a single notification.
     *
     * @param username the user
     * @param typeKey the notification type
     * @param pluginKey the plugin
     * @param notification the notification to update to
     * @return the notification post-update
     */
    @POST
    @Consumes(UPM_JSON)
    @Produces(UPM_JSON)
    public Response setNotification(@PathParam("username") String username,
                                    @PathParam("typeKey") String typeKey,
                                    @PathParam("pluginKey") String pluginKey,
                                    NotificationRepresentation notification)
    {
        permissionEnforcer.enforcePermission(MANAGE_NOTIFICATIONS);
        pluginKey = unescape(pluginKey);

        NotificationType type = NotificationType.fromKey(typeKey);
        if (type == null)
        {
            return Response.status(BAD_REQUEST).build();
        }
        else if (username == null || !username.equals(userManager.getRemoteUsername()))
        {
            return Response.status(FORBIDDEN).build();
        }
        else if (!permissionEnforcer.hasPermission(type.getRequiredPermission(), pluginAccessorAndController.getPlugin(pluginKey)))
        {
            return Response.status(UNAUTHORIZED).build();
        }

        //no such notification exists
        if (!cache.getNotification(type, username, pluginKey).isDefined())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        cache.setNotificationDismissal(type, username, pluginKey, notification.isDismissed());
        if (cache.isNotificationDismissed(type, username, pluginKey) != notification.isDismissed())
        {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

        for (Notification updatedNotification : cache.getNotification(type, username, pluginKey))
        {
            return Response.ok(notificationRepresentationFactory.getNotification(updatedNotification, some(username))).build();
        }

        //for some unknown reason, the notification no longer exists
        return Response.status(INTERNAL_SERVER_ERROR).build();
    }
}

