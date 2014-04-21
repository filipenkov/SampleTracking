package com.atlassian.jira.rest.v2;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("serverInfo")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ServerInfoResource
{
    private final ApplicationProperties properties;
    private final BuildUtilsInfo buildUtils;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;

    public ServerInfoResource(final ApplicationProperties properties, final BuildUtilsInfo buildUtils, final JiraAuthenticationContext authContext, final PermissionManager permissionManager)
    {
        this.properties = properties;
        this.buildUtils = buildUtils;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns general information about the current JIRA server.
     *
     * @return a Response containing a ServerInfoBean
     *
     * @response.representation.200.qname
     *      serverInfo
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.example
     *      {@link ServerInfoBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a full representation of the server info in JSON format
     */
    @GET
    public Response getServerInfo()
    {
        final boolean canUse = permissionManager.hasPermission(Permissions.USE, authContext.getLoggedInUser());
        return Response.ok(new ServerInfoBean(properties, buildUtils, canUse)).cacheControl(never()).build();
    }

}
