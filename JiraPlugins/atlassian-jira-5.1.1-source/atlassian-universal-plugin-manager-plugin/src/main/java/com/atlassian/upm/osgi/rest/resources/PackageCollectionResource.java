package com.atlassian.upm.osgi.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.OSGI_PACKAGE_COLLECTION_JSON;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.google.common.base.Preconditions.checkNotNull;

@Path("/packages")
public class PackageCollectionResource
{
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;

    public PackageCollectionResource(RepresentationFactory representationFactory,
        PermissionEnforcer permissionEnforcer)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    public Response get()
    {
        permissionEnforcer.enforcePermission(GET_OSGI_STATE);
        return Response.ok(representationFactory.createOsgiPackageCollectionRepresentation()).type(OSGI_PACKAGE_COLLECTION_JSON).build();
    }
}
