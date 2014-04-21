package com.atlassian.jira.dev.backdoor;

import com.atlassian.plugin.PluginController;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */

@Path ("plugins")
public class PluginsBackdoor
{

    private final PluginController pluginController;

    public PluginsBackdoor(PluginController pluginController)
    {
        this.pluginController = pluginController;
    }

    @GET
    @AnonymousAllowed
    @Path ("disable")
    public Response disablePlugin(@QueryParam ("key") String key)
    {
        pluginController.disablePlugin(key);
        return Response.ok().build();
    }
}
