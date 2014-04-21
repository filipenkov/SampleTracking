package com.atlassian.upm.osgi.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.osgi.impl.Versions.fromString;
import static com.atlassian.upm.rest.MediaTypes.OSGI_PACKAGE_JSON;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/packages/{bundleId}/{name}/{version}")
public class PackageResource
{
    private final PackageAccessor packageAccessor;
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;

    public PackageResource(PackageAccessor packageAccessor,
        RepresentationFactory representationFactory,
        PermissionEnforcer permissionEnforcer)
    {
        this.packageAccessor = checkNotNull(packageAccessor, "packageAccessor");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    public Response get(@PathParam("bundleId") long bundleId, @PathParam("name") String name, @PathParam("version") String version)
    {
        permissionEnforcer.enforcePermission(GET_OSGI_STATE);
        Package pkg = packageAccessor.getExportedPackage(bundleId, name, fromString(version));
        if (pkg != null)
        {
            return Response.ok(representationFactory.createOsgiPackageRepresentation(pkg)).type(OSGI_PACKAGE_JSON).build();
        }
        else
        {
            return Response.status(NOT_FOUND).build();
        }
    }
}
