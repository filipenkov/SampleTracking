package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Gets or sets system properties with GET or POST. Always returns latest value for named property.
 *
 * @since v5.0
 */
@Path ("systemproperty")
public class SystemPropertyBackdoor
{
    private static final Logger log = Logger.getLogger(SystemPropertyBackdoor.class);

    @GET
    @AnonymousAllowed
    @Path ("{name}")
    public Response get(@PathParam ("name") final String propertyName)
    {
        return Response.ok(System.getProperty(propertyName)).build();
    }

    @POST
    @AnonymousAllowed
    @Path ("{name}")
    public Response set(@PathParam ("name") final String propertyName, @QueryParam ("value") String propertyValue)
    {
        if (propertyName != null && propertyValue != null)
        {
            System.setProperty(propertyName, propertyValue);
            JiraSystemProperties.resetReferences();
        }
        else
        {
            log.warn("cannot set null property: '" + propertyName + "=" + propertyValue + "'");
        }
        return get(propertyName);
    }

}
