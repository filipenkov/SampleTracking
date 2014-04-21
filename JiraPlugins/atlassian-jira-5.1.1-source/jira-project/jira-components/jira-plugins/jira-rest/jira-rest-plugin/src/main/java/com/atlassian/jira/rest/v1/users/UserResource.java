package com.atlassian.jira.rest.v1.users;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarsDisabledException;
import com.atlassian.jira.avatar.NoPermissionException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * User REST resource.
 *
 * @since v4.2
 */
@Path ("user")
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UserResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final AvatarService avatarService;

    public UserResource(JiraAuthenticationContext authenticationContext, AvatarService avatarService)
    {
        this.authenticationContext = authenticationContext;
        this.avatarService = avatarService;
    }

    @POST
    @Path ("{username}/avatar/{avatarid}")
    public Response updateUserAvatar(@PathParam ("username") String username, @PathParam ("avatarid") Long avatarId)
    {
        if (StringUtils.isBlank(username) || avatarId == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("username and avatarid are required path parameters!").cacheControl(NO_CACHE).build();
        }

        try
        {
            avatarService.setCustomUserAvatar(authenticationContext.getLoggedInUser(), username, avatarId);

            return Response.ok().cacheControl(NO_CACHE).build();
        }
        catch (AvatarsDisabledException e)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        catch (NoPermissionException e)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
    }
}
