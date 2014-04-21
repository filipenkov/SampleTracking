package com.atlassian.upm.osgi.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.atlassian.upm.rest.MediaTypes.OSGI_BUNDLE_COLLECTION_JSON;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.google.common.base.Preconditions.checkNotNull;

@Path("/bundles")
public class BundleCollectionResource
{
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;

    public BundleCollectionResource(RepresentationFactory representationFactory,
        PermissionEnforcer permissionEnforcer)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    public Response get(@QueryParam("q") String term)
    {
        permissionEnforcer.enforcePermission(GET_OSGI_STATE);
        return Response
            .ok(term == null ?
                    representationFactory.createOsgiBundleCollectionRepresentation() :
                    representationFactory.createOsgiBundleCollectionRepresentation(term))
            .type(OSGI_BUNDLE_COLLECTION_JSON)
            .build();
    }
}
