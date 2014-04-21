package com.atlassian.upm.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.AVAILABLE_PLUGIN_JSON;
import static com.atlassian.upm.rest.UpmUriEscaper.unescape;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/available/{plugin-key}")
public class AvailablePluginResource
{
    private final RepresentationFactory factory;
    private final PacClient client;
    private final PermissionEnforcer permissionEnforcer;

    public AvailablePluginResource(RepresentationFactory factory, PacClient client, PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.factory = checkNotNull(factory, "factory");
        this.client = checkNotNull(client, "client");
    }

    @GET
    @Produces(AVAILABLE_PLUGIN_JSON)
    public Response get(@PathParam("plugin-key") String key)
    {
        // Ideally we would get the plugin to do a permission check here but this resource is dealing with possible
        // plugins referenced from PAC which may not be installed in the system. Therefore I believe it is fair to
        // not check the permission with a plugin context.
        permissionEnforcer.enforcePermission(GET_AVAILABLE_PLUGINS);
        key = unescape(key);
        PluginVersion plugin = client.getAvailablePlugin(key);
        if (plugin == null)
        {
            return Response.status(NOT_FOUND).build();
        }
        return Response.ok(factory.createAvailablePluginRepresentation(plugin)).build();
    }
}
