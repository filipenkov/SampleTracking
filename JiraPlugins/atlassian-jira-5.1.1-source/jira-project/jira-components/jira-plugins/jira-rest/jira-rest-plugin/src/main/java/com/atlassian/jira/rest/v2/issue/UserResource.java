package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.event.user.UserAvatarUpdatedEvent;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.AttachmentHelper;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.lang.Math.max;
import static java.lang.Math.min;


/**
 * @since 4.2
 */
@Path ("user")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class UserResource
{
    public static final int DEFAULT_USERS_RETURNED = 50;
    public static final int MAX_USERS_RETURNED = 1000;

    private final UserUtil userUtil;
    private final ContextI18n i18n;
    private final EmailFormatter emailFormatter;
    private final JiraAuthenticationContext authContext;
    private final TimeZoneManager timeZoneManager;
    private final AvatarService avatarService;
    private final AvatarResourceHelper avatarResourceHelper;
    private final UserPropertyManager userPropertyManager;
    private final UserPickerSearchService userPickerSearchService;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final ProjectManager projectManager;
    private final AvatarManager avatarManager;
    private final EventPublisher eventPublisher;
    private final AssigneeService assigneeService;
    private final IssueManager issueManager;

    public UserResource(UserUtil userUtil, ContextI18n i18n, EmailFormatter emailFormatter,
            JiraAuthenticationContext authContext, TimeZoneManager timeZoneManager,
            AvatarPickerHelper avatarPickerHelper, AvatarManager avatarManager, AvatarService avatarService, 
            AttachmentHelper attachmentHelper, UserPropertyManager userPropertyManager,
            UserPickerSearchService userPickerSearchService, PermissionManager permissionManager,
            ProjectService projectService, IssueService issueService, ProjectManager projectManager,
            EventPublisher eventPublisher, AssigneeService assigneeService, IssueManager issueManager)
    {
        this.userPropertyManager = userPropertyManager;
        this.userPickerSearchService = userPickerSearchService;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.issueService = issueService;
        this.projectManager = projectManager;
        this.avatarManager = avatarManager;
        this.eventPublisher = eventPublisher;
        this.assigneeService = assigneeService;
        this.issueManager = issueManager;
        this.avatarResourceHelper = new AvatarResourceHelper(authContext, avatarManager, avatarPickerHelper, attachmentHelper);
        this.userUtil = userUtil;
        this.i18n = i18n;
        this.emailFormatter = emailFormatter;
        this.authContext = authContext;
        this.timeZoneManager = timeZoneManager;
        this.avatarService = avatarService;
    }

    /**
     * Returns a user. This resource cannot be accessed anonymously.
     *
     * @param name the username
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     */
    @GET
    public Response getUser(@QueryParam("username") final String name, @Context UriInfo uriInfo)
    {
        if (authContext.getLoggedInUser() == null)
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.authentication.no.user.logged.in")));
        }

        User user = getUserObject(name);
        UserBeanBuilder builder = new UserBeanBuilder().user(user).context(uriInfo)
                .groups(new ArrayList<String>(userUtil.getGroupNamesForUser(user.getName())))
                .loggedInUser(authContext.getLoggedInUser())
                .emailFormatter(emailFormatter)
                .timeZone(timeZoneManager.getLoggedInUserTimeZone())
                .avatarService(avatarService);

        return Response.ok(builder.buildFull()).cacheControl(never()).build();
    }

    /**
     * Returns a list of users that match the search string. This resource cannot be accessed anonymously.
     *
     *
     * @param username A string used to search username, Name or e-mail address
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param uriInfo context used for creating urls in user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @return A list of user objects that match the username provided
     */
    @GET
    @Path ("search")
    public Response findUsers(@QueryParam("username") final String username, @QueryParam ("startAt") Integer startAt,
            @QueryParam ("maxResults") Integer maxResults, @Context UriInfo uriInfo)
    {
        final List<User> page = limitUserSearch(startAt, maxResults, findUsers(username));
        return Response.ok(makeUserBeans(page, uriInfo)).cacheControl(never()).build();
    }

    /**
     * Returns a list of users that match the search string. This resource cannot be accessed anonymously.
     * Please note that this resource should be called with an issue key when a list of assignable users is retrieved
     * for editing.  For create only a project key should be supplied.  The list of assignable users may be incorrect
     * if it's called with the project key for editing.
     *
     * @param name the username
     * @param projectKey the key of the project we are finding assignable users for
     * @param issueKey the issue key for the issue being edited we need to find assignable users for.
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if no project or issue key was provided
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     * @return a Response with the users matching the query
     */
    @GET
    @Path ("assignable/search")
    public Response findAssignableUsers(@QueryParam("username") final String name, @QueryParam("project") final String projectKey,
            @QueryParam("issueKey") final String issueKey, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @QueryParam("actionDescriptorId") Integer actionDescriptorId, @Context UriInfo uriInfo)
    {
        ActionDescriptor actionDescriptor = null;
        if (actionDescriptorId != null)
        {
            actionDescriptor = getActionDescriptorById(issueKey, actionDescriptorId);
        }

        final List<User> usersWithPermission = findAssignableUsers(name, projectKey, issueKey, actionDescriptor);

        final List<User> page = limitUserSearch(startAt, maxResults, usersWithPermission);
        return Response.ok(makeUserBeans(page, uriInfo)).cacheControl(never()).build();
    }

    protected ActionDescriptor getActionDescriptorById(String issueKey, Integer actionDescriptorId)
    {
        WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
        workflowTransitionUtil.setIssue(issueManager.getIssueObject(issueKey));
        workflowTransitionUtil.setAction(actionDescriptorId);

        return workflowTransitionUtil.getActionDescriptor();
    }

    private List<User> findAssignableUsers(String name, String projectKey, String issueKey, ActionDescriptor actionDescriptor)
    {
        Collection<User> users = null;
        if(StringUtils.isNotBlank(issueKey))
        {
            final IssueService.IssueResult issueResult = issueService.getIssue(authContext.getLoggedInUser(), issueKey);

            if(!issueResult.isValid())
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(issueResult.getErrorCollection()));
            }
            if (!permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issueResult.getIssue(), authContext.getLoggedInUser()))
            {
                throw new NotAuthorisedWebException();
            }

            users = assigneeService.findAssignableUsers(name, issueResult.getIssue(), actionDescriptor);
        }
        else if(StringUtils.isNotBlank(projectKey))
        {
            //get the project without any permission checks.  This code path will most likely get executed when
            //creating issues.  The projectService only allows getting projects that a user can browse or administer.
            //When creating issues these permissions aren't necessary.
            final Project project = projectManager.getProjectObjByKey(projectKey);
            if (project == null)
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(authContext.getI18nHelper().getText("rest.must.provide.valid.project")));
            }
            if (!permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, project, authContext.getLoggedInUser()))
            {
                throw new NotAuthorisedWebException();
            }

            users = assigneeService.findAssignableUsers(name, project);
        }
        else
        {
            throwWebException(authContext.getI18nHelper().getText("rest.must.provide.project.or.issue"),
                    com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        return new ArrayList<User>(users);
    }
    
    /**
     * Returns a list of users that match the search string. This resource cannot be accessed anonymously.
     * Given an issue key this resource will provide a list of users that match the search string and have
     * the browse issue permission for the issue provided.
     *
     * @param name the username
     * @param issueKey the issue key for the issue being edited we need to find viewable users for.
     * @param projectKey the optional project key to search for users with if no issueKey is supplied.
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.400.doc
     *     Returned if no project or issue key was provided
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     * @return a Response with the users matching the query
     */
    @GET
    @Path ("viewissue/search")
    public Response findUsersWithBrowsePermission(@QueryParam("username") final String name,
            @QueryParam ("issueKey") final String issueKey, @QueryParam ("projectKey") final String projectKey,
            @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @Context UriInfo uriInfo)
    {
        final List<User> usersWithPermission = findUsersWithPermission(Permissions.BROWSE, name, projectKey, issueKey, uriInfo);

        final List<User> page = limitUserSearch(startAt, maxResults, usersWithPermission);
        return Response.ok(makeUserBeans(page, uriInfo)).cacheControl(never()).build();
    }

    private List<User> findUsersWithPermission(int permission, String name, String projectKey, String issueKey, UriInfo uriInfo)
    {
        final List<User> users = new ArrayList<User>();
        if(StringUtils.isNotBlank(issueKey))
        {
            final IssueService.IssueResult issueResult = issueService.getIssue(authContext.getLoggedInUser(), issueKey);

            if(!issueResult.isValid())
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(issueResult.getErrorCollection()));
            }

            for (final User user : findUsers(name))
            {
                if (permissionManager.hasPermission(permission, issueResult.getIssue(), user))
                {
                    users.add(user);
                }
            }
        }
        else if(StringUtils.isNotBlank(projectKey))
        {
            //get the project without any permission checks.  This code path will most likely get executed when
            //creating issues.  The projectService only allows getting projects that a user can browse or administer.
            //When creating issues these permissions aren't necessary.
            final Project project = projectManager.getProjectObjByKey(projectKey);
            if (project == null)
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(authContext.getI18nHelper().getText("rest.must.provide.valid.project")));
            }

            for (final User user : findUsers(name))
            {
                if (permissionManager.hasPermission(permission, project, user, true))
                {
                    users.add(user);
                }
            }
        }
        else
        {
            throwWebException(authContext.getI18nHelper().getText("rest.must.provide.project.or.issue"),
                    com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        return users;
    }

    /**
     * Returns a list of users that match the search string and can be assigned issues for all the given projects.
     * This resource cannot be accessed anonymously.
     *
     * @param name the username
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param projectKeysStr the keys of the projects we are finding assignable users for, comma-separated
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     */
    @GET
    @Path ("assignable/multiProjectSearch")
    public Response findBulkAssignableUsers(@QueryParam("username") final String name,
            @QueryParam("projectKeys") final String projectKeysStr, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @Context UriInfo uriInfo)
    {
        // 1. Get projects for keys, aborting on any errors
        String[] projectKeys = projectKeysStr.split(",");
        List<Project> projects = new ArrayList<Project>(projectKeys.length);
        for (String projectKey : projectKeys)
        {
            final ProjectService.GetProjectResult projectResult =
                    projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), projectKey, ProjectAction.VIEW_PROJECT);

            if (projectResult.getErrorCollection().hasAnyErrors())
            {
                final ErrorCollection errors = ErrorCollection.of(projectResult .getErrorCollection());
                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
            projects.add(projectResult.getProject());
        }

        // 2. Get users matching the search that are assignable for all projects
        List<User> users = null;
        for (final Project project : projects)
        {
            Collection<User> projectAssignableUsers = assigneeService.findAssignableUsers(name, project);

            if (users == null)
            {
                users = new ArrayList<User>(projectAssignableUsers);
            }
            else
            {
                users.retainAll(projectAssignableUsers);
            }
        }

        // 3. Bean them up, Scottie.
        final List<User> page = limitUserSearch(startAt, maxResults, users);
        return Response.ok(makeUserBeans(page, uriInfo)).cacheControl(never()).build();
    }

    /**
     * Returns all avatars which are visible for the currently logged in user.
     *
     * @since v5.0
     * @param name username
     *
     * @return all avatars for given user, which the logged in user has permission to see
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of avatars for both custom an system avatars
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *      Returned if the current user is not authenticated.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    @GET
    @Path ("avatars")
    public Response getAllAvatars(@QueryParam("username") final String name)
    {
        final User user = getUserObject(name);
        Long selectedAvatarId = null;
        final Avatar selectedAvatar = avatarService.getAvatar(authContext.getLoggedInUser(), user.getName());
        if (selectedAvatar != null) {
            selectedAvatarId = selectedAvatar.getId();
        }
        return avatarResourceHelper.getAllAvatars(Avatar.Type.USER, name, selectedAvatarId);
    }



    /**
     * Converts temporary avatar into a real avatar
     *
     * @since v5.0
     *
     * @param username username
     * @param croppingInstructions cropping instructions
     * @return created avatar
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EDIT_EXAMPLE}
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returns created avatar
     *
     * @response.representation.201.example
     *      {@link AvatarBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the cropping coordinates are invalid
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to pick avatar
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Path ("avatar")
    public Response createAvatarFromTemporary(@QueryParam ("username") final String username,
                                              final AvatarCroppingBean croppingInstructions)
    {
        getUserObject(username);
        return avatarResourceHelper.createAvatarFromTemporary(Avatar.Type.USER, username, croppingInstructions);
    }


    @PUT
    @Path ("avatar")
    public Response updateProjectAvatar(final @QueryParam ("username") String username, final AvatarBean avatarBean)
    {
        final User userObject = getUserObject(username);
        final PropertySet propertySet = userPropertyManager.getPropertySet(userObject);
        String id = avatarBean.getId();
        Long avatarId;
        try
        {
            avatarId = id == null ? null : Long.valueOf(id);
        }
        catch (NumberFormatException e)
        {
            avatarId = null;
        }
        if (!avatarManager.hasPermissionToEdit(authContext.getLoggedInUser(), Avatar.Type.USER, userObject.getName()))
        {
            throw new NotAuthorisedWebException();
        }
        propertySet.setLong(AvatarManager.USER_AVATAR_ID_KEY, avatarId);
        
        eventPublisher.publish(new UserAvatarUpdatedEvent(userObject, avatarId));
        
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }


    /**
     * Creates temporary avatar. Creating a temporary avatar is part of a 3-step process in uploading a new
     * avatar for a user: upload, crop, confirm.
     *
     * <p>
     *     The following examples shows these three steps using curl.
     *     The cookies (session) need to be preserved between requests, hence the use of -b and -c.
     *     The id created in step 2 needs to be passed to step 3
     *     (you can simply pass the whole response of step 2 as the request of step 3).
     * </p>
     *
     * <pre>
     * curl -c cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: image/png" --data-binary @mynewavatar.png \
     *   'http://localhost:8090/jira/rest/api/2/user/avatar/temporary?username=admin&amp;filename=mynewavatar.png'
     *
     * curl -b cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: application/json" --data '{"cropperWidth": "65","cropperOffsetX": "10","cropperOffsetY": "16"}' \
     *   -o tmpid.json \
     *   http://localhost:8090/jira/rest/api/2/user/avatar?username=admin
     *
     * curl -b cookiejar.txt -X PUT -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: application/json" --data-binary @tmpid.json \
     *   http://localhost:8090/jira/rest/api/2/user/avatar?username=admin
     * </pre>
     *
     * @since v5.0
     *
     * @param username username
     * @param filename name of file being uploaded
     * @param size size of file
     * @param request servlet request
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the request does not conain a valid XSRF token
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("avatar/temporary")
    public Response storeTemporaryAvatar(final @QueryParam ("username") String username,
                                         final @QueryParam ("filename") String filename,
                                         final @QueryParam ("size") Long size,
                                         final @Context HttpServletRequest request)
    {
        getUserObject(username);
        return avatarResourceHelper.storeTemporaryAvatar(Avatar.Type.USER, username,  filename, size, request);
    }


    /**
     * Creates temporary avatar using multipart. The response is sent back as JSON stored in a textarea. This is because
     * the client uses remote iframing to submit avatars using multipart. So we must send them a valid HTML page back from
     * which the client parses the JSON from.
     *
     * <p>
     * Creating a temporary avatar is part of a 3-step process in uploading a new
     * avatar for a user: upload, crop, confirm. This endpoint allows you to use a multipart upload
     * instead of sending the image directly as the request body.
     * </p>
     *
     * <p>You *must* use "avatar" as the name of the upload parameter:</p>
     *
     * <pre>
     * curl -c cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -F "avatar=@mynewavatar.png;type=image/png" \
     *   'http://localhost:8090/jira/rest/api/2/user/avatar/temporary?username=admin'
     * </pre>
     *
     * @since v5.0
     *
     * @param username Username
     * @param request servlet request
     *
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      text/html
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions embeded in HTML page. Error messages will also be embeded in the page.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if user does NOT exist
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @Path ("avatar/temporary")
    @Produces ( { MediaType.TEXT_HTML })
    public Response storeTemporaryAvatarUsingMultiPart(@QueryParam ("username") String username,
                                                       final @MultipartFormParam("avatar") FilePart filePart,
                                                       final @Context HttpServletRequest request)
    {
        getUserObject(username);
        return avatarResourceHelper.storeTemporaryAvatarUsingMultiPart(Avatar.Type.USER, username, filePart, request);
    }

    /**
     * Deletes avatar
     *
     * @since v5.0
     *
     * @param username username
     * @param id database id for avatar
     * @return temporary avatar cropping instructions
     *
     * @response.representation.204.mediaType
     *      application/json
     *
     * @response.representation.204.doc
     *      Returned if the avatar is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the avatar.
     *
     * @response.representation.404.doc
     *      Returned if the avatar does not exist or the currently authenticated user does not have permission to
     *      delete it.
     */
    @DELETE
    @Path ("avatar/{id}")
    public Response deleteAvatar(final @QueryParam ("username") String username, final @PathParam ("id") Long id)
    {
        getUserObject(username);
        return avatarResourceHelper.deleteAvatar(id);
    }

    private List<User> findUsers(final String searchString)
    {
        if (searchString == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        return userPickerSearchService.findUsers(getContext(), searchString);
    }

    private User getUserObject(final String name)
    {
        if (name == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        final User user = userUtil.getUser(name);
        if (user == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.user.error.not.found", name)));
        }

        return user;
    }

    private List<UserBean> makeUserBeans(Collection<User> users, UriInfo uriInfo)
    {
        List<UserBean> beans =  new ArrayList<UserBean>();
        for (User user : users)
        {
            UserBeanBuilder builder = new UserBeanBuilder().user(user).context(uriInfo);
            builder.loggedInUser(authContext.getLoggedInUser());
            builder.emailFormatter(emailFormatter);
            builder.timeZone(timeZoneManager.getLoggedInUserTimeZone());
            beans.add(builder.buildMid());
        }
        return beans;
    }

    private void throwWebException(String message, com.atlassian.jira.util.ErrorCollection.Reason reason)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message, reason);
        throwWebException(errorCollection);
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

    private List<User> limitUserSearch(Integer startAt, Integer maxResults, List<User> users)
    {
        int start = startAt != null ? max(0, startAt) : 0;
        int end = (maxResults != null ? min(MAX_USERS_RETURNED, maxResults) : DEFAULT_USERS_RETURNED) + start;

        return users.subList(start, min(users.size(), end));
    }

    JiraServiceContext getContext()
    {
        User user = authContext.getLoggedInUser();
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(user, errorCollection);
    }
}
