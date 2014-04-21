package com.atlassian.upm.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.PacException;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.collect.ImmutableList;

import static com.atlassian.upm.rest.MediaTypes.PRODUCT_UPDATES_JSON;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides list of product versions newer than the current one.
 */
@Path("/product-updates")
public class ProductUpdatesResource
{
    private final RepresentationFactory representationFactory;
    private final PacClient client;
    private final PermissionEnforcer permissionEnforcer;

    public ProductUpdatesResource(RepresentationFactory factory, PacClient client, PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(factory, "representationFactory");
        this.client = checkNotNull(client, "client");
    }

    @GET
    @Produces(PRODUCT_UPDATES_JSON)
    public Response get()
    {
        permissionEnforcer.enforcePermission(GET_PRODUCT_UPDATE_COMPATIBILITY);
        
        Iterable<Product> productVersions;
        boolean pacUnreachable = false;
        try
        {
            productVersions = client.getProductUpdates();
        }
        catch (PacException e)
        {
            productVersions = ImmutableList.of();
            pacUnreachable = true;
        }
        
        return Response.ok(representationFactory.createProductUpdatesRepresentation(productVersions, pacUnreachable)).build();
    }
}
