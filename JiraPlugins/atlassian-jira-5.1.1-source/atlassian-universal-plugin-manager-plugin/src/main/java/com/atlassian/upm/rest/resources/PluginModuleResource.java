package com.atlassian.upm.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.plugin.PluginState;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.PluginModuleRepresentation;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.PLUGIN_MODULE_JSON;
import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static com.atlassian.upm.permission.Permission.GET_PLUGIN_MODULES;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Provides a REST resource for getting plugin modules.
 */
@Path("/{pluginKey}/modules/{moduleKey}")
public class PluginModuleResource
{
    private final RepresentationFactory representationFactory;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PermissionEnforcer permissionEnforcer;

    public PluginModuleResource(RepresentationFactory representationFactory, PluginAccessorAndController pluginAccessorAndController,
        PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
    }

    /**
     * Retrieves a JSON representation of the module specified by the {@code moduleKey} within the plugin specified by the {@code pluginKey}
     *
     * @param pluginKey key of the plugin that contains the module whose representation will be retrieved
     * @param moduleKey key of the module whose representation will be retrieved
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @GET
    @Produces(PLUGIN_MODULE_JSON)
    public Response get(@PathParam("pluginKey") String pluginKey, @PathParam("moduleKey") String moduleKey)
    {
        pluginKey = unescape(pluginKey);
        moduleKey = unescape(moduleKey);
        Module module = pluginAccessorAndController.getPluginModule(pluginKey, moduleKey);
        permissionEnforcer.enforcePermission(GET_PLUGIN_MODULES, module);
        if (module == null)
        {
            return Response.status(NOT_FOUND).build();
        }

        return Response.ok(representationFactory.createPluginModuleRepresentation(module)).build();
    }

    @PUT
    @Consumes(PLUGIN_MODULE_JSON)
    public Response updateModuleState(@PathParam("pluginKey") String pluginKey,
        @PathParam("moduleKey") String moduleKey,
        PluginModuleRepresentation module)
    {
        pluginKey = unescape(pluginKey);
        moduleKey = unescape(moduleKey);
        Module pluginModule = pluginAccessorAndController.getPluginModule(pluginKey, moduleKey);
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_MODULE_ENABLEMENT, pluginModule);
        Plugin plugin = pluginAccessorAndController.getPlugin(pluginKey);
        if (pluginModule == null)
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
        else if (!pluginModule.hasRecognisableType())
        {
            return Response.status(PRECONDITION_FAILED)
                .entity(representationFactory.createI18nErrorRepresentation("upm.pluginModule.error.cannot.recognise.type"))
                .type(ERROR_JSON)
                .build();
        }

        if (module.isEnabled())
        {
            if (!plugin.getPluginState().equals(PluginState.ENABLED))
            {
                return Response.status(CONFLICT)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.pluginModule.error.failed.to.enable"))
                    .type(ERROR_JSON).build();
            }
            else if (!pluginAccessorAndController.enablePluginModule(pluginModule.getCompleteKey()))
            {
                return Response.status(INTERNAL_SERVER_ERROR)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.pluginModule.error.failed.to.enable"))
                    .type(ERROR_JSON)
                    .build();
            }
        }
        else
        {
            if (!plugin.getPluginState().equals(PluginState.ENABLED))
            {
                return Response.status(CONFLICT)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.pluginModule.error.failed.to.disable"))
                    .type(ERROR_JSON)
                    .build();
            }
            // We need to check the annotation as the plugins system enforces that, however we do not want to check
            // the plugin metadata since we want the REST backdoor
            else if (pluginModule.canNotBeDisabled())
            {
                return Response.status(FORBIDDEN)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.pluginModule.error.cannot.be.disabled"))
                    .type(ERROR_JSON).build();
            }
            else if (!pluginAccessorAndController.disablePluginModule(pluginModule.getCompleteKey()))
            {
                return Response.status(INTERNAL_SERVER_ERROR)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.pluginModule.error.failed.to.disable"))
                    .type(ERROR_JSON)
                    .build();
            }
        }

        return Response.ok(representationFactory.createPluginModuleRepresentation(pluginModule)).type(PLUGIN_MODULE_JSON).build();
    }
}
