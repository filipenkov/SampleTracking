package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Use this backdoor to manipulate Permissions as part of setup for tests.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.PermissionsControl}.
 *
 * @since v5.0
 */
@Path ("permissions")
public class PermissionsBackdoor
{
    private final GlobalPermissionManager globalPermissionManager;

    public PermissionsBackdoor(GlobalPermissionManager globalPermissionManager)
    {
        this.globalPermissionManager = globalPermissionManager;
    }

    @GET
    @AnonymousAllowed
    @Path("global/add")
    public Response addGlobalPermission(@QueryParam ("type") int permissionType, @QueryParam ("group") String group)
    {
        try
        {
            globalPermissionManager.addPermission(permissionType, group);
        }
        catch (CreateException e)
        {
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("global/remove")
    public Response removeGlobalPermission(@QueryParam ("type") int permissionType, @QueryParam ("group") String group)
    {
        try
        {
            globalPermissionManager.removePermission(permissionType, group);
        }
        catch (RemoveException e)
        {
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }
}
