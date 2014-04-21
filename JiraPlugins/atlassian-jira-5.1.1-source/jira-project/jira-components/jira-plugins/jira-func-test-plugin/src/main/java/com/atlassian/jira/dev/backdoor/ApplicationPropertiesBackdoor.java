package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

/**
 * Use this backdoor to manipulate ApplicationProperties as part of setup for tests.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.ApplicationPropertiesControl}.
 *
 * @since v5.0
 */
@Produces ({MediaType.APPLICATION_JSON})
@Consumes ({MediaType.APPLICATION_JSON})
@Path ("applicationProperties")
public class ApplicationPropertiesBackdoor
{
    private final ApplicationProperties applicationProperties;

    public ApplicationPropertiesBackdoor(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @GET
    @AnonymousAllowed
    @Path("option/set")
    public Response setOption(@QueryParam ("key") String key, @QueryParam ("value") boolean value)
    {
        applicationProperties.setOption(key, value);
        return Response.ok(null).build();
    }

    @POST
    @AnonymousAllowed
    @Path("text/set")
    public Response setText(KeyValueHolder holder)
    {
        applicationProperties.setText(holder.key, holder.value);
        return Response.ok(null).build();
    }

    @POST
    @AnonymousAllowed
    @Path("string/set")
    public Response setString(KeyValueHolder holder)
    {
        applicationProperties.setString(holder.key, holder.value);
        return Response.ok(null).build();
    }

    private static class KeyValueHolder
    {
        public String key;
        public String value;
    }
}
