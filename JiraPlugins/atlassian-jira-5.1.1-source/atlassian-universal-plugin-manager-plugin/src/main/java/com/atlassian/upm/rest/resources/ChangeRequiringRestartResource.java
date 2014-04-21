package com.atlassian.upm.rest.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.upm.Change;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/requires-restart/{plugin-key}")
public class ChangeRequiringRestartResource
{
    private final PermissionEnforcer permissionEnforcer;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final RepresentationFactory representationFactory;

    public ChangeRequiringRestartResource(PermissionEnforcer permissionEnforcer, PluginAccessorAndController pluginAccessorAndController, RepresentationFactory representationFactory)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
    }

    @DELETE
    public Response delete(@PathParam("plugin-key") String key)
    {
        key = unescape(key);
        try
        {
            Change change = pluginAccessorAndController.getRestartRequiredChange(key);
            if (change == null)
            {
                return Response.status(NOT_FOUND)
                    .entity(representationFactory.createI18nErrorRepresentation("upm.messages.requiresRestart.no.such.plugin"))
                    .type(ERROR_JSON)
                    .build();
            }
            permissionEnforcer.enforcePermission(change.getRequiredPermission(), change.getPlugin());

            pluginAccessorAndController.revertRestartRequiredChange(key);
        }
        catch (Exception e)
        {
            return Response.serverError()
                .entity(representationFactory.createErrorRepresentation(e.getMessage()))
                .type(ERROR_JSON)
                .build();
        }
        return Response.noContent().build();
    }
}
