package com.atlassian.upm.osgi.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.BundleAccessor;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.OSGI_BUNDLE_JSON;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/bundles/{id}")
public class BundleResource
{
    private final BundleAccessor bundleAccessor;
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;

    public BundleResource(BundleAccessor bundleAccessor,
        RepresentationFactory representationFactory,
        PermissionEnforcer permissionEnforcer)
    {
        this.bundleAccessor = checkNotNull(bundleAccessor, "bundleAccessor");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    public Response get(@PathParam("id") long id)
    {
        permissionEnforcer.enforcePermission(GET_OSGI_STATE);
        Bundle bundle = bundleAccessor.getBundle(id);
        if (bundle != null)
        {
            return Response.ok(representationFactory.createOsgiBundleRepresentation(bundle)).type(OSGI_BUNDLE_JSON).build();
        }
        else
        {
            return Response.status(NOT_FOUND).build();
        }
    }
}
