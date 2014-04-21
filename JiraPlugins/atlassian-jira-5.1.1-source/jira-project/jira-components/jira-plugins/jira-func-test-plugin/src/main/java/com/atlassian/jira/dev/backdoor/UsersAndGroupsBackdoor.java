package com.atlassian.jira.dev.backdoor;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.util.concurrent.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Use this backdoor to manipulate Users and Groups as part of setup for tests
 * NOT specfically testing the Admin UI.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.UsersAndGroupsControl}.
 *
 * @since v5.0
 */
@Path ("usersAndGroups")
public class UsersAndGroupsBackdoor
{
    private static final Logger log = Logger.getLogger(UsersAndGroupsBackdoor.class);

    private final CrowdService crowdService;
    private final LoginService loginService;
    private final UserUtil userUtil;

    public UsersAndGroupsBackdoor(UserUtil userUtil, CrowdService crowdService, LoginService loginService)
    {
        this.crowdService = crowdService;
        this.userUtil = userUtil;
        this.loginService = loginService;
    }

    @GET
    @AnonymousAllowed
    @Path("user/addEvenIfUserExists")
    public Response addEvenIfuserExists(
            @QueryParam ("userName") String username,
            @QueryParam ("password") String password,
            @QueryParam ("email") String email,
            @QueryParam ("displayName") String displayName,
            @QueryParam ("sendEmail") boolean sendEmail)
    {
        if (userUtil.userExists(username))
        {
            deleteUser(username);
        }
        return addUser(username, password, email, displayName, sendEmail);
    }

    @GET
    @AnonymousAllowed
    @Path("user/add")
    public Response addUser(
            @QueryParam ("userName") String username,
            @QueryParam ("password") String password,
            @QueryParam ("email") String email,
            @QueryParam ("displayName") String displayName,
            @QueryParam ("sendEmail") boolean sendEmail)
    {
        try
        {
            if (sendEmail)
            {
                userUtil.createUserWithNotification(username, password, email, displayName, UserEventType.USER_CREATED);
            }
            else
            {
                userUtil.createUserNoNotification(username, password, email, displayName);
            }
        }
        catch (PermissionException e)
        {
            log.warn("PermissionException adding user", e);
            throw new RuntimeException(e);
        }
        catch (CreateException e)
        {
            log.warn("CreateException adding user", e);
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/delete")
    public Response deleteUser(@QueryParam ("userName") String username)
    {
        User admin = userUtil.getUser("admin");     // shouldn't have to pass admin in for permissions at this level...
        User userToRemove = userUtil.getUser(username);

        userUtil.removeUser(admin, userToRemove);

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/addToGroup")
    public Response addUserToGroup(
            @QueryParam ("userName") String userName,
            @QueryParam ("groupName") String groupName)
    {
        Group group = crowdService.getGroup(groupName);
        User userToAdd = userUtil.getUser(userName);

        try
        {
            userUtil.addUserToGroup(group, userToAdd);
        }
        catch (PermissionException e)
        {
            log.warn("PermissionException adding user to group", e);
            throw new RuntimeException(e);
        }
        catch (AddException e)
        {
            log.warn("PermissionException adding user to group", e);
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/removeFromGroup")
    public Response removeUserFromGroup(
            @QueryParam ("userName") String userName,
            @QueryParam ("groupName") String groupName)
    {
        Group group = crowdService.getGroup(groupName);
        User userToRemove = userUtil.getUser(userName);

        try
        {
            userUtil.removeUserFromGroup(group, userToRemove);
        }
        catch (PermissionException e)
        {
            log.warn("PermissionException removing user from group", e);
            throw new RuntimeException(e);
        }
        catch (RemoveException e)
        {
            log.warn("RemoveExceptionNotPermittedException removing user from group", e);
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("group/add")
    public Response addGroup(@QueryParam ("groupName") String groupName) {
        try
        {
            Group group = new ImmutableGroup(groupName);
            crowdService.addGroup(group);
        }
        catch (InvalidGroupException e)
        {
            log.warn("InvalidGroupException adding group", e);
            throw new RuntimeException(e);
        }
        catch (OperationNotPermittedException e)
        {
            log.warn("OperationNotPermittedException adding group", e);
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/addMany")
    public Response addManyUsers(
            @QueryParam ("usernamePrefix") String usernamePrefix,
            @QueryParam ("displayNamePrefix") String displayNamePrefix,
            @QueryParam ("numberOfNewUsers") int numberOfNewUsers,
            @Nullable @QueryParam ("groupName") String groupName)
    {
        Group group = null;
        if (StringUtils.isNotBlank(groupName))
        {
            group = crowdService.getGroup(groupName);
        }
        try
        {
            for (int i = 0; i < numberOfNewUsers; i++)
            {
                String username = usernamePrefix + i;
                userUtil.createUserNoNotification(username,
                        username,
                        "e" + username + "@example.com",     // make the email slightly different to the username
                        displayNamePrefix + i);

                if (group != null)
                {
                    User user = userUtil.getUser(username);
                    userUtil.addUserToGroup(group, user);
                }
            }
        }
        catch (PermissionException e)
        {
            log.warn("Permission exception adding users", e);
            throw new RuntimeException(e);
        }
        catch (CreateException e)
        {
            log.warn("Create exception adding users", e);
            throw new RuntimeException(e);
        }
        catch (AddException e)
        {
            log.warn("Add exception adding users", e);
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/resetLoginCount")
    public Response resetLoginCount(@QueryParam("user") String username)
    {
        User user = crowdService.getUser(username);
        loginService.resetFailedLoginCount(user);
        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("user/count")
    public Response numberOfUsers()
    {
        String count = Long.toString(userUtil.getTotalUserCount());
        
        return Response.ok(count).build();
    }

    @GET
    @AnonymousAllowed
    @Path("group/count")
    public Response numberOfGroups()
    {
        final EntityQuery<String> membershipQuery =
                QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(EntityQuery.ALL_RESULTS);

        Iterable<String> groups = crowdService.search(membershipQuery);

        int count = 0;
        
        for (String group : groups) {
            ++count;
        }

        String stringCount = Long.toString(count);

        return Response.ok(stringCount).build();
    }
    
}
