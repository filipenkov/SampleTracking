package com.atlassian.upm.osgi.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.ServiceAccessor;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.OSGI_SERVICE_JSON;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/services/{id}")
public class ServiceResource
{
    private final ServiceAccessor serviceAccessor;
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;

    public ServiceResource(ServiceAccessor serviceAccessor,
        RepresentationFactory representationFactory,
        PermissionEnforcer permissionEnforcer)
    {
        this.serviceAccessor = checkNotNull(serviceAccessor, "serviceAccessor");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    public Response get(@PathParam("id") long id)
    {
        permissionEnforcer.enforcePermission(GET_OSGI_STATE);
        Service service = serviceAccessor.getService(id);
        if (service != null)
        {
            return Response.ok(representationFactory.createOsgiServiceRepresentation(service)).type(OSGI_SERVICE_JSON).build();
        }
        else
        {
            return Response.status(NOT_FOUND).build();
        }
    }
}
