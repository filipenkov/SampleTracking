package com.atlassian.upm.test.rest.resources;

import com.atlassian.upm.Sys;
import com.atlassian.upm.pac.PacClient;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.PAC_MODE_JSON;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;


/** This REST resource is only used for testing purpose: it helps enabling / disabling PAC and
 * setting a url.
 */
@Path("/pac-mode")
public class PacModeResource
{
    private final PacClient client;

    public PacModeResource(PacClient client)
    {
        this.client = client;
    }

    @GET
    @Produces(PAC_MODE_JSON)
    public Response get()
    {
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
        return Response.ok(new PacModeRepresentation(getPacMode())).build();
    }

    @PUT
    @Consumes(PAC_MODE_JSON)
    public Response put(PacModeRepresentation pacMode)
    {
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
        setPacMode(pacMode.isDisabled());
        return Response.ok().build();
    }

    private void setPacMode(boolean disabled)
    {
        System.setProperty(Sys.UPM_PAC_DISABLE, String.valueOf(disabled));
    }

    private boolean getPacMode()
    {
        return client.isPacDisabled();
    }

    // This is PAC_MODE_JSON
    public static final class PacModeRepresentation
    {
        @JsonProperty
        private boolean disabled;

        @JsonCreator
        public PacModeRepresentation(@JsonProperty("disabled") boolean disabled)
        {
            this.disabled = disabled;
        }

        public boolean isDisabled()
        {
            return disabled;
        }
    }
}