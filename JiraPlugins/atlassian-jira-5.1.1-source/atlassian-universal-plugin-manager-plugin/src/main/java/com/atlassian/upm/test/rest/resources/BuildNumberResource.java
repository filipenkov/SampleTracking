package com.atlassian.upm.test.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.BUILD_NUMBER_JSON;
import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Provides a REST resource for setting and clearing the build number (for test purposes only) - UPM-871.
 */
@Path("/build-number")
public class BuildNumberResource
{
    private final RepresentationFactory representationFactory;
    private final PermissionEnforcer permissionEnforcer;
    private static BuildNumberRepresentation buildNumber = null;

    public BuildNumberResource(RepresentationFactory representationFactory, PermissionEnforcer permissionEnforcer)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @GET
    @Produces(BUILD_NUMBER_JSON)
    public Response getBuildNumberResource()
    {
        permissionEnforcer.enforcePermission(GET_AUDIT_LOG);
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
        return Response.ok(buildNumber).build();
    }

    @PUT
    @Consumes(BUILD_NUMBER_JSON)
    public Response setBuildNumber(BuildNumberRepresentation buildNumberRepresentation)
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        buildNumber = buildNumberRepresentation;
        return Response.ok(buildNumber)
            .type(BUILD_NUMBER_JSON)
            .build();
    }

    @DELETE
    public Response clearBuildNumber()
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        buildNumber = representationFactory.createBuildNumberRepresentation(null);
        return Response.ok(buildNumber)
            .type(BUILD_NUMBER_JSON)
            .build();
    }

    public static String getBuildNumber()
    {
        if (buildNumber != null)
        {
            return buildNumber.getBuildNumber();
        }
        else
        {
            return null;
        }
    }

    public static final class BuildNumberRepresentation
    {
        @JsonProperty private String buildNumber;

        @JsonCreator
        public BuildNumberRepresentation(@JsonProperty("build-number") String buildNumber)
        {
            this.buildNumber = buildNumber;
        }

        public String getBuildNumber()
        {
            return buildNumber;
        }
    }
}
