package com.atlassian.upm.test.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.PAC_BASE_URL_JSON;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Provides a REST resource for setting and clearing the PAC base URL (for test purposes only) - UPM-1004.
 */
@Path("/pac-base-url")
public class PacBaseUrlResource
{
    private final PermissionEnforcer permissionEnforcer;
    private static volatile PacBaseUrlRepresentation pacBaseUrl = null;

    public PacBaseUrlResource(PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    @PUT
    @Consumes(PAC_BASE_URL_JSON)
    public Response setPacBaseUrl(PacBaseUrlRepresentation pacBaseUrlrRepresentation)
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        pacBaseUrl = pacBaseUrlrRepresentation;
        return Response.ok(pacBaseUrl)
            .type(PAC_BASE_URL_JSON)
            .build();
    }

    @DELETE
    public Response resetPacBaseUrl()
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        // Lets just null out the over-ride value that is kept here so we will fallback to the system value
        pacBaseUrl = null;
        return Response.ok().build();
    }

    public static String getPacBaseUrl()
    {
        if (pacBaseUrl != null)
        {
            return pacBaseUrl.getPacBaseUrl();
        }
        else
        {
            return null;
        }
    }

    public static final class PacBaseUrlRepresentation
    {
        @JsonProperty
        private String pacBaseUrl;

        @JsonCreator
        public PacBaseUrlRepresentation(@JsonProperty("pac-base-url") String pacBaseUrl)
        {
            this.pacBaseUrl = pacBaseUrl;
        }

        public String getPacBaseUrl()
        {
            return pacBaseUrl;
        }
    }

}
