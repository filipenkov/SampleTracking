package com.atlassian.upm.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.COMPATIBILITY_JSON;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resource for finding the compatibility statuses of the currently installed plugins against particular product update
 * versions.
 */
@Path("/product-updates/{build-number}/compatibility")
public class ProductUpdatePluginCompatibilityResource
{
    private final RepresentationFactory representationFactory;
    private final PacClient client;
    private final PermissionEnforcer permissionEnforcer;

    public ProductUpdatePluginCompatibilityResource(RepresentationFactory factory, PacClient client,
                                                    PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(factory, "representationFactory");
        this.client = checkNotNull(client, "client");
    }

    @GET
    @Produces(COMPATIBILITY_JSON)
    public Response get(@PathParam("build-number") String buildNumber)
    {
        permissionEnforcer.enforcePermission(GET_PRODUCT_UPDATE_COMPATIBILITY);
        Long buildNumberLong = Long.parseLong(buildNumber);
        ProductUpdatePluginCompatibility pluginCompatibility = client.getProductUpdatePluginCompatibility(buildNumberLong);
        return Response.ok(representationFactory.createProductUpdatePluginCompatibilityRepresentation(pluginCompatibility, buildNumberLong)).build();
    }
}
