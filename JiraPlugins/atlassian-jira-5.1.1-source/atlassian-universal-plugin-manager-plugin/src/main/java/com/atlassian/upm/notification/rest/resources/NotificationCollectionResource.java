package com.atlassian.upm.notification.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.notification.NotificationCollection;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.notification.cache.NotificationCache;
import com.atlassian.upm.notification.rest.representations.NotificationGroupRepresentation;
import com.atlassian.upm.notification.rest.representations.NotificationRepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.rest.MediaTypes.UPM_JSON;
import static com.atlassian.upm.permission.Permission.GET_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.MANAGE_NOTIFICATIONS;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Provides a REST resource for notification collections
 */
@Path("/notifications")
public class NotificationCollectionResource
{
    public final static String HIDE_DISMISSED = "hide-dismissed";
    public final static String DEFAULT_HIDE_DISMISSED = "false";

    private final NotificationRepresentationFactory notificationRepresentationFactory;
    private final NotificationCache cache;
    private final PermissionEnforcer permissionEnforcer;
    private final UserManager userManager;

    public NotificationCollectionResource(NotificationRepresentationFactory notificationRepresentationFactory,
                                          NotificationCache cache,
                                          PermissionEnforcer permissionEnforcer,
                                          UserManager userManager)
    {
        this.notificationRepresentationFactory = checkNotNull(notificationRepresentationFactory, "notificationRepresentationFactory");
        this.cache = checkNotNull(cache, "cache");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.userManager = checkNotNull(userManager, "userManager");
    }

    /**
     * Returns all notifications for the entire system (including all notifications dismissed by various users).
     */
    @GET
    @Produces(UPM_JSON)
    public Response getNotifications()
    {
        permissionEnforcer.enforcePermission(GET_NOTIFICATIONS);

        Iterable<NotificationCollection> notifications = cache.getNotifications();
        return Response.ok(notificationRepresentationFactory.getNotificationGroupCollection(notifications, none(String.class))).build();
    }

    /**
     * Returns notifications for the specified user.
     *
     * @param username the user
     * @param hideDismissed true if dismissed notifications should be hidden, false if all notifications should be returned
     * @return notifications for the specified user
     */
    @GET
    @Path("{username}")
    @Produces(UPM_JSON)
    public Response getNotifications(@PathParam("username") String username,
                                     @QueryParam(HIDE_DISMISSED) @DefaultValue(DEFAULT_HIDE_DISMISSED) Boolean hideDismissed)
    {
        permissionEnforcer.enforcePermission(GET_NOTIFICATIONS);
        if (username == null || !username.equals(userManager.getRemoteUsername()))
        {
            return Response.status(FORBIDDEN).build();
        }

        Iterable<NotificationCollection> notifications = cache.getNotifications(username, hideDismissed);
        return Response.ok(notificationRepresentationFactory.getNotificationGroupCollection(notifications, some(username))).build();
    }

    /**
     * Returns notifications of the specified type for the specified user.
     *
     * @param username the user
     * @param typeKey the notification type
     * @param hideDismissed true if dismissed notifications should be hidden, false if all notifications should be returned
     * @return notifications of the specified type for the specified user
     */
    @GET
    @Path("{username}/{typeKey}")
    @Produces(UPM_JSON)
    public Response getNotifications(@PathParam("username") String username,
                                     @PathParam("typeKey") String typeKey,
                                     @QueryParam(HIDE_DISMISSED) @DefaultValue(DEFAULT_HIDE_DISMISSED) Boolean hideDismissed)
    {
        permissionEnforcer.enforcePermission(GET_NOTIFICATIONS);
        NotificationType type = NotificationType.fromKey(typeKey);
        if (type == null)
        {
            return Response.status(BAD_REQUEST).build();
        }
        else if (username == null || !username.equals(userManager.getRemoteUsername()))
        {
            return Response.status(FORBIDDEN).build();
        }
        else if (!permissionEnforcer.hasPermission(type.getRequiredPermission()))
        {
            return Response.status(UNAUTHORIZED).build();
        }

        NotificationCollection notifications = cache.getNotifications(type, username, hideDismissed);
        return Response.ok(notificationRepresentationFactory.getNotificationGroup(notifications, some(username))).build();
    }

    /**
     * Updates the notification group to the specified value. Can be used to dismiss notification types.
     *
     * @param username the user
     * @param typeKey the notification type
     * @param notificationGroup the notification group to update to
     * @return the notification collection post-update
     */
    @POST
    @Path("{username}/{typeKey}")
    @Consumes(UPM_JSON)
    @Produces(UPM_JSON)
    public Response setNotifications(@PathParam("username") String username,
                                    @PathParam("typeKey") String typeKey,
                                    NotificationGroupRepresentation notificationGroup)
    {
        permissionEnforcer.enforcePermission(MANAGE_NOTIFICATIONS);
        NotificationType type = NotificationType.fromKey(typeKey);
        if (type == null)
        {
            return Response.status(BAD_REQUEST).build();
        }
        else if (username == null || !username.equals(userManager.getRemoteUsername()))
        {
            return Response.status(FORBIDDEN).build();
        }
        else if (!permissionEnforcer.hasPermission(type.getRequiredPermission()))
        {
            return Response.status(UNAUTHORIZED).build();
        }

        cache.setNotificationTypeDismissal(type, username, notificationGroup.isDismissed());
        if (cache.isNotificationTypeDismissed(type, username) != notificationGroup.isDismissed())
        {
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }

        //return the notification collection (excluding dismissed notifications)
        return Response.ok(notificationRepresentationFactory.getNotificationGroup(cache.getNotifications(type, username, true), some(username))).build();
    }
}