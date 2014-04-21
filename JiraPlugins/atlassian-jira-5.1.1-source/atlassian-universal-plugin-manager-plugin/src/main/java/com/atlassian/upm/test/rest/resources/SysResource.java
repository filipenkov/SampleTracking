package com.atlassian.upm.test.rest.resources;

import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.UPM_JSON;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Provides a REST resource for setting the system onDemand state
 */
@Path("/sys")
public class SysResource
{
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;
    private static IsOnDemandRepresentation isOnDemand = null;

    public SysResource(RepresentationFactory representationFactory, PermissionEnforcer permissionEnforcer)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    /*
     * This method returns isOnDemand if set (true or false), or no value if unset
     */
    @GET
    @Produces(UPM_JSON)
    @Path("on-demand")
    public Response getIsOnDemandResource()
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
        return Response.ok(isOnDemand).build();
    }

    @PUT
    @Consumes(UPM_JSON)
    @Path("on-demand")
    public Response setIsOnDemand(IsOnDemandRepresentation isOnDemandRepresentation)
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        isOnDemand = isOnDemandRepresentation;
        return Response.ok(isOnDemand)
            .type(UPM_JSON)
            .build();
    }

    @DELETE
    @Path("on-demand")
    public Response resetIsOnDemand()
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        isOnDemand = null;
        return Response.ok().build();
    }

    public static Boolean getIsOnDemand()
    {
        if (isOnDemand != null)
        {
            return isOnDemand.getIsOnDemand();
        }
        else
        {
            return null;
        }
    }

    public static final class IsOnDemandRepresentation
    {
        @JsonProperty private Boolean isOnDemand;

        @JsonCreator
        public IsOnDemandRepresentation(@JsonProperty("isOnDemand") Boolean isOnDemand)
        {
            this.isOnDemand = isOnDemand;
        }
        
        public Boolean getIsOnDemand()
        {
            return isOnDemand;
        }
    }
}
