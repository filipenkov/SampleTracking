package com.atlassian.upm.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.UPM_JSON;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides list of product versions newer than the current one.
 */
@Path("/product-version")
public class ProductVersionResource
{
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;
    private final PacClient client;

    public ProductVersionResource(RepresentationFactory factory, PacClient client, PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(factory, "representationFactory");
        this.client = checkNotNull(client, "client");
    }

    @GET
    @Produces(UPM_JSON)
    public Response get()
    {
        permissionEnforcer.enforcePermission(GET_AVAILABLE_PLUGINS);
        return Response.ok(representationFactory.createProductVersionRepresentation(client.isDevelopmentProductVersion().getOrElse(false), client.isUnknownProductVersion().getOrElse(false))).build();
    }
}
