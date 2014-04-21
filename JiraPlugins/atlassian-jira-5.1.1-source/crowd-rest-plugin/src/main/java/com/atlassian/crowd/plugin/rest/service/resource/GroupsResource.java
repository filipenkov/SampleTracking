package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.crowd.plugin.rest.service.controller.GroupsController;
import com.atlassian.crowd.plugin.rest.service.controller.MembershipsController;
import com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Path("group")
@Consumes({APPLICATION_XML, APPLICATION_JSON})
@Produces({APPLICATION_XML, APPLICATION_JSON})
@AnonymousAllowed
public class GroupsResource extends AbstractResource
{
    private final GroupsController groupsController;
    private final MembershipsController membershipsController;

    public GroupsResource(GroupsController groupsController, MembershipsController membershipsController)
    {
        this.groupsController = groupsController;
        this.membershipsController = membershipsController;
    }

    @GET
    public Response getGroup(@QueryParam("groupname") String groupName)
            throws GroupNotFoundException, OperationFailedException
    {
        final GroupEntity group = groupsController.findGroupByName(getApplicationName(), groupName, expandAttributes(), getBaseUri());
        return Response.ok(group).build();
    }

    /**
     * Adds a new group.
     *
     * @param restGroup the group to create
     */
    @POST
    public Response addGroup(GroupEntity restGroup)
            throws GroupNotFoundException, InvalidGroupException, ApplicationPermissionException, OperationFailedException
    {
        String groupName = groupsController.addGroup(getApplicationName(), restGroup);
        return Response.created(LinkUriHelper.buildGroupUri(getBaseUri(), groupName)).build();
    }

    /**
     * Updates an existing group.
     *
     * @param restGroup the group to update
     */
    @PUT
    public Response updateGroup(@QueryParam("groupname") String groupName, GroupEntity restGroup)
            throws GroupNotFoundException, InvalidGroupException, ApplicationPermissionException, OperationFailedException
    {
        if (!StringUtils.equalsIgnoreCase(groupName, restGroup.getName()))
        {
            throw new InvalidGroupException(EntityTranslator.toGroup(restGroup), "The names of the resource location <" + uriInfo + "> and object <" + restGroup.getName() + "> are not equal");
        }

        GroupEntity updatedGroup = groupsController.updateGroup(getApplicationName(), restGroup, getBaseUri());
        return Response.ok(updatedGroup).build();
    }

    @DELETE
    public Response removeGroup(@QueryParam("groupname") String groupname)
            throws GroupNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        groupsController.removeGroup(getApplicationName(), groupname);
        return Response.noContent().build();
    }

    @GET
    @Path("attribute")
    public Response getGroupAttributes(@QueryParam("groupname") String groupName)
            throws GroupNotFoundException, OperationFailedException
    {
        final GroupEntity group = groupsController.findGroupByName(getApplicationName(), groupName, true, getBaseUri());
        return Response.ok(group.getAttributes()).build();
    }

    /**
     * Stores the group attributes
     *
     * @param groupname name of the group
     * @param attributes group attributes
     */
    @POST
    @Path("attribute")
    public Response storeGroupAttributes(@QueryParam("groupname") String groupname, MultiValuedAttributeEntityList attributes)
            throws GroupNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        groupsController.storeGroupAttributes(getApplicationName(), groupname, attributes);
        return Response.noContent().build();
    }

    @DELETE
    @Path("attribute")
    public Response deleteGroupAttribute(@QueryParam("groupname") String groupname, @QueryParam("attributename") String attributeName)
            throws GroupNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        groupsController.removeGroupAttributes(getApplicationName(), groupname, attributeName);
        return Response.noContent().build();
    }

    @GET
    @Path("user/direct")
    public Response getDirectUsers(@QueryParam("groupname") String groupName, @QueryParam("username") String username, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(username))
        {
            final UserEntityList directUsers = groupsController.getDirectUsers(getApplicationName(), groupName, expandUsers(), maxResults, startIndex, getBaseUri());
            return Response.ok(directUsers).build();
        }
        else
        {
            final UserEntity directUser = groupsController.getDirectUser(getApplicationName(), groupName, username, getBaseUri());
            return Response.ok(directUser).build();
        }
    }

    @POST
    @Path("user/direct")
    public Response addDirectUser(@QueryParam("groupname") String groupName, UserEntity user)
            throws GroupNotFoundException, OperationFailedException, ApplicationPermissionException
    {
        try
        {
            groupsController.addDirectUser(getApplicationName(), groupName, user.getName());
            return Response.created(LinkUriHelper.buildDirectUserGroupUri(getBaseUri(), groupName, user.getName())).build();
        }
        catch (UserNotFoundException e)
        {
            final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
        }
    }

    @DELETE
    @Path("user/direct")
    public Response deleteDirectUser(@QueryParam("groupname") String groupName, @QueryParam("username") String username)
            throws GroupNotFoundException, MembershipNotFoundException, UserNotFoundException, OperationFailedException, ApplicationPermissionException
    {
        groupsController.deleteDirectUser(getApplicationName(), groupName, username);
        return Response.noContent().build();
    }

    @GET
    @Path("user/nested")
    public Response getNestedUsers(@QueryParam("groupname") String groupName, @QueryParam("username") String username, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(username))
        {
            final UserEntityList nestedUsers = groupsController.getNestedUsers(getApplicationName(), groupName, expandUsers(), maxResults, startIndex, getBaseUri());
            return Response.ok(nestedUsers).build();
        }
        else
        {
            final UserEntity nestedUser = groupsController.getNestedUser(getApplicationName(), groupName, username, getBaseUri());
            return Response.ok(nestedUser).build();
        }
    }

    @GET
    @Path("parent-group/direct")
    public Response getDirectParentGroups(@QueryParam("groupname") String groupName, @QueryParam("child-groupname") String childGroupName, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(childGroupName))
        {
            final GroupEntityList directParentGroups = groupsController.getDirectParentGroups(getApplicationName(), groupName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(directParentGroups).build();
        }
        else
        {
            final GroupEntity directParentGroup = groupsController.getDirectParentGroup(getApplicationName(), groupName, childGroupName, getBaseUri());
            return Response.ok(directParentGroup).build();
        }
    }

    @POST
    @Path("parent-group/direct")
    public Response addDirectParentGroup(@QueryParam("groupname") String groupName, GroupEntity parentGroup)
            throws InvalidMembershipException, OperationFailedException, ApplicationPermissionException
    {
        try
        {
            groupsController.addDirectParentGroup(getApplicationName(), groupName, parentGroup.getName());
            return Response.created(LinkUriHelper.buildDirectParentGroupUri(getBaseUri(), groupName, parentGroup.getName())).build();
        }
        catch (GroupNotFoundException e)
        {
            final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            if (e.getGroupName().equals(groupName))
            {
                // Group was not found
                return Response.status(Response.Status.NOT_FOUND).entity(errorEntity).build();
            }
            else
            {
                // Parent group was not found
                return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
            }
        }
    }

    @GET
    @Path("parent-group/nested")
    public Response getNestedParentGroups(@QueryParam("groupname") String groupName, @QueryParam("parent-groupname") String parentGroupName, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(parentGroupName))
        {
            final GroupEntityList nestedParentGroups = groupsController.getNestedParentGroups(getApplicationName(), groupName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(nestedParentGroups).build();
        }
        else
        {
            final GroupEntity nestedParentGroup = groupsController.getNestedParentGroup(getApplicationName(), groupName, parentGroupName, getBaseUri());
            return Response.ok(nestedParentGroup).build();
        }
    }

    @GET
    @Path("child-group/direct")
    public Response getDirectChildGroups(@QueryParam("groupname") String groupName, @QueryParam("child-groupname") String childGroupName, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(childGroupName))
        {
            final GroupEntityList directChildGroups = groupsController.getDirectChildGroups(getApplicationName(), groupName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(directChildGroups).build();
        }
        else
        {
            final GroupEntity directChildGroup = groupsController.getDirectChildGroup(getApplicationName(), groupName, childGroupName, getBaseUri());
            return Response.ok(directChildGroup).build();
        }
    }

    @POST
    @Path("child-group/direct")
    public Response addDirectChildGroup(@QueryParam("groupname") String groupName, GroupEntity childGroup)
            throws InvalidMembershipException, ApplicationPermissionException, OperationFailedException
    {
        try
        {
            groupsController.addDirectChildGroup(getApplicationName(), groupName, childGroup.getName());
            return Response.created(LinkUriHelper.buildDirectChildGroupUri(getBaseUri(), groupName, childGroup.getName())).build();
        }
        catch (GroupNotFoundException e)
        {
            final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            if (e.getGroupName().equals(groupName))
            {
                // Group was not found
                return Response.status(Response.Status.NOT_FOUND).entity(errorEntity).build();
            }
            else
            {
                // Child group was not found
                return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
            }
        }
    }

    @DELETE
    @Path("child-group/direct")
    public Response deleteDirectChildGroup(@QueryParam("groupname") String groupName, @QueryParam("child-groupname") String childGroupName)
            throws MembershipNotFoundException, GroupNotFoundException, ApplicationPermissionException, OperationFailedException
    {
        groupsController.deleteDirectChildGroup(getApplicationName(), groupName, childGroupName);
        return Response.noContent().build();
    }

    @GET
    @Path("child-group/nested")
    public Response getNestedChildGroups(@QueryParam("groupname") String groupName, @QueryParam("child-groupname") String childGroupName, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) int startIndex, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE) @QueryParam(MAX_RESULTS_PARAM) int maxResults)
            throws MembershipNotFoundException, OperationFailedException
    {
        if (StringUtils.isEmpty(childGroupName))
        {
            final GroupEntityList nestedChildGroups = groupsController.getNestedChildGroups(getApplicationName(), groupName, expandGroups(), maxResults, startIndex, getBaseUri());
            return Response.ok(nestedChildGroups).build();
        }
        else
        {
            final GroupEntity nestedChildGroup = groupsController.getNestedChildGroup(getApplicationName(), groupName, childGroupName, getBaseUri());
            return Response.ok(nestedChildGroup).build();
        }
    }

    /**
     * This method will only return XML, not JSON.
     */
    @GET
    @Path("membership")
    public Response getMemberships() throws GroupNotFoundException
    {
        StreamingOutput memberships = membershipsController.searchGroups(getApplicationName());
        return Response.ok(memberships).type(MediaType.APPLICATION_XML_TYPE).build();
    }
    
    private boolean expandAttributes()
    {
        return EntityExpansionUtil.shouldExpandField(GroupEntity.class, "attributes", request);
    }

    private boolean expandGroups()
    {
        return EntityExpansionUtil.shouldExpandField(GroupEntityList.class, "groups", request);
    }

    private boolean expandUsers()
    {
        return EntityExpansionUtil.shouldExpandField(UserEntityList.class, "users", request);
    }
}
