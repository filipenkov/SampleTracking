package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;


/**
 * @since 4.4
 */
@Path ("websudo")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class WebSudoBackdoor
{
    private final ApplicationProperties applicationProperties;

    public WebSudoBackdoor(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @GET
    public Response status()
    {
        return Response.ok(getCurrentState()).cacheControl(never()).build();
    }

    @POST
    public Response setStatus(boolean state)
    {
        if (getCurrentState() != state)
        {
            setState(state);
        }
        return Response.ok(state).cacheControl(never()).build();
    }

    private void setState(boolean enabled)
    {
        System.setProperty(SystemPropertyKeys.WEBSUDO_IS_DISABLED, Boolean.toString(!enabled));
        JiraSystemProperties.resetReferences();
        applicationProperties.setOption(APKeys.WebSudo.IS_DISABLED, !enabled);
    }

    private boolean getCurrentState()
    {
        return !(Boolean.getBoolean(SystemPropertyKeys.WEBSUDO_IS_DISABLED) ||
                applicationProperties.getOption(APKeys.WebSudo.IS_DISABLED));
    }
}
