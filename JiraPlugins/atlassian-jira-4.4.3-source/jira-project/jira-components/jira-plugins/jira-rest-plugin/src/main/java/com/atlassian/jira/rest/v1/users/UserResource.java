package com.atlassian.jira.rest.v1.users;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private final UserUtil userUtil;
    private final AvatarManager avatarManager;

    public UserResource(JiraAuthenticationContext authenticationContext,
            final UserUtil userUtil, final AvatarManager avatarManager)
    {
        this.authenticationContext = authenticationContext;
        this.userUtil = userUtil;
        this.avatarManager = avatarManager;
    }

    @POST
    @Path ("{username}/avatar/{avatarid}")
    public Response updateUserAvatar(@PathParam ("username") String username, @PathParam ("avatarid") Long avatarId)
    {
        if (StringUtils.isBlank(username) || avatarId == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("username and avatarid are required path parameters!").cacheControl(NO_CACHE).build();
        }

        if (!avatarManager.hasPermissionToEdit(authenticationContext.getUser(), Avatar.Type.USER, username))
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }

        final User user = userUtil.getUser(username);
        user.getPropertySet().setLong(AvatarManager.USER_AVATAR_ID_KEY, avatarId);

        return Response.ok().cacheControl(NO_CACHE).build();
    }
}
