package com.atlassian.upm.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.SafeModeException;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.internal.PluginLicenseError;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.notification.cache.NotificationCacheUpdater;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALLED_PLUGIN_JSON;
import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static com.atlassian.upm.rest.resources.UpmResources.licensingPreconditionFailed;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_LICENSE;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_UNINSTALL;
import static com.atlassian.upm.Sys.isOnDemand;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Provides REST resources for working with plugins
 */
@Path("/{pluginKey}")
public class PluginResource
{
    private final RepresentationFactory representationFactory;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PermissionEnforcer permissionEnforcer;
    private final PluginLicenseRepository licenseRepository;
    private final NotificationCacheUpdater notificationCacheUpdater;
    private final AuditLogService auditLogger;

    public PluginResource(RepresentationFactory representationFactory,
        PluginAccessorAndController pluginAccessorAndController, PermissionEnforcer permissionEnforcer,
        PluginLicenseRepository licenseRepository, NotificationCacheUpdater notificationCacheUpdater,
        AuditLogService auditLogger)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
        this.notificationCacheUpdater = checkNotNull(notificationCacheUpdater, "notificationCacheUpdater");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
    }

    /**
     * Retrieves a JSON representation of the plugin specified by the {@code pluginKey}.
     * Anyone who has access to UPM has permission to access this resource method.
     *
     * @param pluginKey key of the plugin whose representation will be retrieved
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @GET
    @Produces(INSTALLED_PLUGIN_JSON)
    public Response get(@PathParam("pluginKey") String pluginKey)
    {
        permissionEnforcer.enforceAdmin();
        pluginKey = unescape(pluginKey);
        Plugin plugin = pluginAccessorAndController.getPlugin(pluginKey);
        if (plugin != null)
        {
            return Response.ok(representationFactory.createPluginRepresentation(plugin)).build();
        }
        return Response.status(NOT_FOUND).build();
    }

    /**
     * Updates a plugin with the given representation.
     *
     * @param pluginRepresentation representation of the plugin to update
     * @param pluginKey key of the plugin to update
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @PUT
    @Consumes(INSTALLED_PLUGIN_JSON)
    public Response put(@PathParam("pluginKey") String pluginKey, PluginRepresentation pluginRepresentation)
    {
        pluginKey = unescape(pluginKey);
        Plugin plugin = pluginAccessorAndController.getPlugin(pluginKey);
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_ENABLEMENT, plugin);

        if (plugin == null)
        {
            return Response.status(NOT_FOUND).build();
        }

        if (pluginAccessorAndController.isUpmPlugin(plugin))
        {
            return Response.status(CONFLICT)
                .entity(representationFactory.createI18nErrorRepresentation(
                    "upm.plugin.error.invalid.upm.plugin.action"))
                .type(ERROR_JSON)
                .build();
        }

        // check if enablement status needs to be toggled, and if so do it
        if (pluginAccessorAndController.isPluginEnabled(pluginKey) != pluginRepresentation.isEnabled())
        {
            if (pluginRepresentation.isEnabled())
            {
                if (!pluginAccessorAndController.enablePlugin(pluginKey))
                {
                    return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(representationFactory.createI18nErrorRepresentation(
                            "upm.plugin.error.failed.to.enable"))
                        .type(ERROR_JSON)
                        .build();
                }
            }
            else
            {
                if (!pluginAccessorAndController.disablePlugin(pluginKey))
                {
                    return Response.status(INTERNAL_SERVER_ERROR)
                        .entity(representationFactory.createI18nErrorRepresentation(
                            "upm.plugin.error.failed.to.disable"))
                        .type(ERROR_JSON)
                        .build();
                }
            }
        }


        // UPM-1699 When in onDemand mode, the UPM front end does not include licensing information in its plugin
        // representation. Within this code block, including no licensing info would be interpreted as an attempt to delete
        // any already installed license. However, in onDemand mode, no one is allowed to manage licenses via UPM so the
        // attempt would cause an error (and be incorrect if it worked).
        if (!isOnDemand())
        {
            //check if the license needs to be modified (added/removed/updated), and if so do it
            boolean licenseCurrentlyDefined = plugin.getLicense().isDefined();
            String newRawLicense = pluginRepresentation.getLicenseDetails() != null ? pluginRepresentation.getLicenseDetails().getRawLicense() : "";

            //adding a new license
            if (!licenseCurrentlyDefined && !isBlank(newRawLicense))
            {
                permissionEnforcer.enforcePermission(MANAGE_PLUGIN_LICENSE, plugin);
                for (Response error : licensingPreconditionFailed(pluginAccessorAndController, plugin, representationFactory))
                {
                    return error;
                }

                for (PluginLicenseError e : licenseRepository.setPluginLicense(pluginKey, newRawLicense).left())
                {
                    return Response.status(e.getType().getStatusCode())
                            .entity(representationFactory.createI18nErrorRepresentation(e.getType().getSubCode()))
                            .type(ERROR_JSON)
                            .build();
                }
                notificationCacheUpdater.updateAllNotifications();
                auditLogger.logI18nMessage("upm.auditLog.plugin.license.add", plugin.getName(), pluginKey);
            }
            //removing an existing license
            else if (licenseCurrentlyDefined && isBlank(newRawLicense))
            {
                permissionEnforcer.enforcePermission(MANAGE_PLUGIN_LICENSE, plugin);
                for (Response error : licensingPreconditionFailed(pluginAccessorAndController, plugin, representationFactory))
                {
                    return error;
                }
                for (PluginLicense license : plugin.getLicense())
                {
                    if (license.isEmbeddedWithinHostLicense())
                    {
                        return Response.status(PRECONDITION_FAILED)
                                .entity(representationFactory.createI18nErrorRepresentation(
                                        "upm.plugin.error.cannot.remove.embedded.license"))
                                .type(ERROR_JSON)
                                .build();
                    }
                }

                licenseRepository.removePluginLicense(pluginKey);
                notificationCacheUpdater.updateAllNotifications();
                auditLogger.logI18nMessage("upm.auditLog.plugin.license.remove", plugin.getName(), pluginKey);
            }
            //update an existing license
            else if (licenseCurrentlyDefined && !plugin.getLicense().get().getRawLicense().equals(newRawLicense))
            {
                permissionEnforcer.enforcePermission(MANAGE_PLUGIN_LICENSE, plugin);
                for (Response error : licensingPreconditionFailed(pluginAccessorAndController, plugin, representationFactory))
                {
                    return error;
                }

                for (PluginLicenseError e : licenseRepository.setPluginLicense(pluginKey, newRawLicense).left())
                {
                    return Response.status(e.getType().getStatusCode())
                            .entity(representationFactory.createI18nErrorRepresentation(e.getType().getSubCode()))
                            .type(ERROR_JSON)
                            .build();
                }
                notificationCacheUpdater.updateAllNotifications();
                auditLogger.logI18nMessage("upm.auditLog.plugin.license.update", plugin.getName(), pluginKey);
            }
        }


        Plugin updatedPlugin = pluginAccessorAndController.getPlugin(pluginKey);
        return Response.ok(representationFactory.createPluginRepresentation(updatedPlugin)).type(INSTALLED_PLUGIN_JSON).build();
    }

    /**
     * Uninstalls a plugin with the given {@code pluginKey}. System and bundled plugins are not uninstallable, and
     * a {@code FORBIDDEN} response will be returned if user tries to uninstall system and bundled plugins. An
     * {@code OK} response with the {@code PluginRepresentation} entity will be returned on a successful plugin
     * uninstall.
     *
     * @param pluginKey key of the plugin to uninstall
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @DELETE
    public Response uninstallPlugin(@PathParam("pluginKey") String pluginKey)
    {
        pluginKey = unescape(pluginKey);
        Plugin plugin = pluginAccessorAndController.getPlugin(pluginKey);
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_UNINSTALL, plugin);

        if (plugin == null)
        {
            return Response.status(NOT_FOUND).build();
        }

        if (pluginAccessorAndController.isUpmPlugin(plugin))
        {
            return Response.status(405)
                .entity(representationFactory.createI18nErrorRepresentation(
                    "upm.plugin.error.invalid.upm.plugin.action"))
                .type(ERROR_JSON)
                .build();
        }

        // Check first if plugin can be uninstalled
        checkSystemPlugin(plugin, "upm.pluginUninstall.error.plugin.is.system");
        checkStaticPlugin(plugin, "upm.pluginUninstall.error.plugin.is.static");
        checkUninstallable(plugin, "upm.pluginUninstall.error.plugin.not.uninstallable");

        try
        {
            pluginAccessorAndController.uninstallPlugin(plugin);
        }
        catch (SafeModeException e)
        {
            return Response.status(CONFLICT)
                .entity(representationFactory.createI18nErrorRepresentation("upm.pluginUninstall.error.safe.mode"))
                .type(ERROR_JSON)
                .build();
        }
        if (pluginAccessorAndController.getPlugin(pluginKey) == null)
        {
            // remove any license which existed for the plugin
            licenseRepository.removePluginLicense(pluginKey);
            // update all cached notifications
            notificationCacheUpdater.handlePluginUninstallation(pluginKey);
            // if the plugin was completely removed, return OK
            return Response.ok(representationFactory.createPluginRepresentation(plugin)).type(INSTALLED_PLUGIN_JSON).build();
        }
        else if (PluginRestartState.REMOVE.equals(pluginAccessorAndController.getRestartState(plugin)))
        {
            // remove any license which existed for the plugin
            licenseRepository.removePluginLicense(pluginKey);
            // update all cached notifications
            notificationCacheUpdater.handlePluginUninstallation(pluginKey);
            // if the plugin is scheduled for removal on next restart, return Accepted
            return Response.status(Status.ACCEPTED).entity(representationFactory.createPluginRepresentation(plugin)).type(INSTALLED_PLUGIN_JSON).build();
        }
        else
        {
            // otherwise, removing the plugin failed but no exception was thrown
            return Response.status(INTERNAL_SERVER_ERROR)
                .entity(representationFactory.createI18nErrorRepresentation(
                    "upm.pluginUninstall.error.failed.to.uninstall"
                )).type(ERROR_JSON).build();
        }
    }

    private void checkSystemPlugin(Plugin plugin, String i18nErrorKey)
    {
        checkPluginActionAllowed(!pluginAccessorAndController.isUserInstalled(plugin), i18nErrorKey);
    }

    private void checkStaticPlugin(Plugin plugin, String i18nErrorKey)
    {
        checkPluginActionAllowed(plugin.isStaticPlugin(), i18nErrorKey);
    }

    private void checkUninstallable(Plugin plugin, String i18nErrorKey)
    {
        checkPluginActionAllowed(!plugin.isUninstallable(), i18nErrorKey);
    }

    private void checkPluginActionAllowed(boolean isNotAllowed, String i18nErrorKey)
    {
        if (isNotAllowed)
        {
            throw new WebApplicationException(Response.status(FORBIDDEN)
                .type(ERROR_JSON)
                .entity(representationFactory.createI18nErrorRepresentation(i18nErrorKey)).build());
        }
    }
}
