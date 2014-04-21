package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.crowd.plugin.rest.service.controller.UsersController;
import com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ConcurrentModificationException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Path("user")
@Consumes({APPLICATION_XML, APPLICATION_JSON})
@Produces({APPLICATION_XML, APPLICATION_JSON})
@AnonymousAllowed
public class UsersResource extends AbstractResource
{
    private final static String USER_NAME_QUERY_PARAM = "username";
    private final static String USER_NAME_NULL_ERROR_MSG = "username query parameter must be given";
    private final static String EMAIL_QUERY_PARAM = "email";
    private final static String EMAIL_NULL_ERROR_MSG = "email query parameter must be given";

    private final UsersController usersController;

    public UsersResource(final UsersController usersController)
    {
        this.usersController = usersController;
    }

    /**
     * Retrieves the user.
     *
     * @param userName name of the user.
     */
    @GET
    public Response getUser(@QueryParam (USER_NAME_QUERY_PARAM) final String userName) throws UserNotFoundException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);

        final Link userLink = LinkUriHelper.buildUserLink(uriInfo.getBaseUri(), userName);
        final UserEntity userEntity = usersController.findUserByName(applicationName, userName, userLink, expandAttributes());
        return Response.ok(userEntity).build();
    }

    /**
     * Adds a new user.
     *
     * @param userEntity the user to create
     */
    @POST
    public Response addUser(final UserEntity userEntity)
            throws InvalidUserException, InvalidCredentialException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();

        Validate.notNull(userEntity);
        Validate.notNull(userEntity.getName());
        Validate.notNull(userEntity.getPassword());
        Validate.notNull(userEntity.getPassword().getValue());

        final String canonicalUsername = usersController.addUser(applicationName, userEntity);
        final URI userUri = LinkUriHelper.buildUserUri(uriInfo.getBaseUri(), canonicalUsername);
        return Response.created(userUri).build();
    }

    /**
     * Updates a user.
     *
     * @param userName name of the user to update
     * @param userEntity the user to create
     */
    @PUT
    public Response updateUser(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, final UserEntity userEntity)
            throws InvalidUserException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);

        if (!StringUtils.equalsIgnoreCase(userName, userEntity.getName()))
        {
            throw new IllegalArgumentException("The names of the resource location <" + uriInfo.getPath() + "> and object <" + userEntity.getName() + "> are not equal");
        }

        usersController.updateUser(applicationName, userEntity);

        return Response.noContent().build();
    }

    /**
     * Removes a user.
     *
     * @param userName name of the user to delete
     */
    @DELETE
    public Response removeUser(@QueryParam(USER_NAME_QUERY_PARAM) final String userName)
            throws UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        usersController.removeUser(applicationName, userName);

        return Response.noContent().build();
    }

    /**
     * Retrieves the user attributes.
     *
     * @param userName name of the user.
     */
    @GET
    @Path("attribute")
    public Response getUserAttributes(@QueryParam(USER_NAME_QUERY_PARAM) final String userName)
            throws UserNotFoundException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        final Link userLink = LinkUriHelper.buildUserLink(uriInfo.getBaseUri(), userName);
        final UserEntity userEntity = usersController.findUserByName(applicationName, userName, userLink, true);
        return Response.ok(userEntity.getAttributes()).build();
    }

    /**
     * Stores the user attributes.
     *
     * @param userName name of the user
     * @param attributes new attributes of the user
     */
    @POST
    @Path("attribute")
    public Response storeUserAttributes(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, final MultiValuedAttributeEntityList attributes)
            throws UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        usersController.storeUserAttributes(applicationName, userName, attributes);

        return Response.noContent().build();
    }

    /**
     * Deletes a user attribute.
     *
     * @param userName name of the user
     * @param attributeName name of the attribute to delete
     */
    @DELETE
    @Path("attribute")
    public Response removeUserAttribute(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, @QueryParam("attributename") final String attributeName)
            throws UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        Validate.notNull(attributeName, "attributename query parameter must be given");
        usersController.removeUserAttribute(applicationName, userName, attributeName);

        return Response.noContent().build();
    }

    /**
     * Updates a user password.
     *
     * @param userName the name of the user to update the password for
     * @param password the password to set against the user
     */
    @PUT
    @Path("password")
    public Response updateUserPassword(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, final PasswordEntity password)
            throws InvalidCredentialException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        usersController.updateUserPassword(applicationName, userName, password.getValue());

        return Response.noContent().build();
    }

    /**
     * Requests a password reset.
     *
     * @param userName name of the user to request a password reset
     */
    @POST
    @Path("mail/password")
    public Response requestPasswordReset(@QueryParam(USER_NAME_QUERY_PARAM) final String userName)
            throws InvalidEmailAddressException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);

        try
        {
            usersController.requestPasswordReset(applicationName, userName);
        }
        catch (ApplicationNotFoundException e)
        {
            // should not occur since the application has been validated by the BasicAuth filter
            throw new ConcurrentModificationException("Application removed while requesting password reset");
        }

        return Response.noContent().build();
    }

   /**
     * Requests an email to be sent containing usernames associated with the given email address.
     *
     * @param email email address of the user
     */
    @POST
    @Path("mail/usernames")
    public Response requestUsernames(@QueryParam(EMAIL_QUERY_PARAM) final String email)
            throws InvalidEmailAddressException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(email, EMAIL_NULL_ERROR_MSG);

        try
        {
            usersController.requestUsernames(applicationName, email);
        }
        catch (ApplicationNotFoundException e)
        {
            // should not occur since the application has been validated by the BasicAuth filter
            throw new ConcurrentModificationException("Application removed while requesting usernames");
        }

        return Response.noContent().build();
    }

    /**
     * Returns the direct group(s) of the user.
     * 
     * @param userName name of the user
     * @param groupName name of the group (optional). If null, then all the groups that the user is a direct member of, are returned.
     * @param maxResults maximum number of results to return
     * @param startIndex start index of the result
     */
    @GET
    @Path("group/direct")
    public Response getDirectGroups(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, @QueryParam("groupname") final String groupName, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) final int maxResults, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) final int startIndex)
            throws MembershipNotFoundException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);

        if (StringUtils.isEmpty(groupName))
        {
            final GroupEntityList directGroups = usersController.getDirectGroups(applicationName, userName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(directGroups).build();
        }
        else
        {
            final GroupEntity directGroup = usersController.getDirectGroup(applicationName, userName, groupName, getBaseUri());
            return Response.ok(directGroup).build();
        }
    }

    /**
     * Adds a user to a group.
     *
     * @param userName name of the user
     * @param parentGroup parent group entity
     */
    @POST
    @Path("group/direct")
    public Response addUserToGroup(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, final GroupEntity parentGroup)
            throws UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);
        try
        {
            usersController.addUserToGroup(applicationName, userName, parentGroup.getName());
            return Response.created(LinkUriHelper.buildDirectParentGroupOfUserUri(getBaseUri(), userName, parentGroup.getName())).build();
        }
        catch (GroupNotFoundException e)
        {
            final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            // Parent group was not found
            return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
        }
    }

    /**
     * Removes a user from a group.
     *
     * @param userName name of the user
     * @param groupName name of the group
     */
    @DELETE
    @Path("group/direct")
    public Response removeUserFromGroup(@QueryParam(USER_NAME_QUERY_PARAM) String userName, @QueryParam("groupname") String groupName)
            throws GroupNotFoundException, MembershipNotFoundException, UserNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        usersController.removeUserFromGroup(getApplicationName(), userName, groupName);
        return Response.noContent().build();
    }

    /**
     * Returns the nested group(s) of the user.
     *
     * @param userName name of the user
     * @param groupName name of the group (optional). If null, then all the groups that the user is a nested member of, are returned.
     * @param maxResults maximum number of results to return
     * @param startIndex start index of the result
     */
    @GET
    @Path("group/nested")
    public Response getNestedGroups(@QueryParam(USER_NAME_QUERY_PARAM) final String userName, @QueryParam("groupname") final String groupName, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) final int maxResults, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) final int startIndex)
            throws MembershipNotFoundException
    {
        final String applicationName = getApplicationName();
        Validate.notNull(userName, USER_NAME_NULL_ERROR_MSG);

        if (StringUtils.isEmpty(groupName))
        {
            final GroupEntityList directGroups = usersController.getNestedGroups(applicationName, userName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(directGroups).build();
        }
        else
        {
            final GroupEntity directGroup = usersController.getNestedGroup(applicationName, userName, groupName, getBaseUri());
            return Response.ok(directGroup).build();
        }
    }

    private boolean expandAttributes()
    {
        return EntityExpansionUtil.shouldExpandField(UserEntity.class, UserEntity.ATTRIBUTES_FIELD_NAME, request);
    }

    private boolean expandGroups()
    {
        return EntityExpansionUtil.shouldExpandField(GroupEntityList.class, GroupEntityList.GROUP_LIST_FIELD_NAME, request);
    }
}
