package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.AttachmentHelper;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

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

/**
 * @since 4.2
 */
@Path ("project")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ProjectResource
{
    private ProjectService projectService;
    private UserManager userManager;
    private JiraAuthenticationContext authContext;
    private UriInfo uriInfo;
    private ProjectManager projectManager;
    private AvatarResourceHelper avatarResourceHelper;
    private VersionService versionService;
    private ProjectComponentService projectComponentService;
    private ProjectBeanFactory projectBeanFactory;
    private VersionBeanFactory versionBeanFactory;
    private PermissionManager permissionManager;
    private AvatarService avatarService;
    private JiraBaseUrls jiraBaseUrls;

    private ProjectResource()
    {
        // this constructor used by tooling
    }

    public ProjectResource(
            final ProjectService projectService,
            final JiraAuthenticationContext authContext,
            final UriInfo uriInfo,
            final VersionService versionService,
            final ProjectComponentService projectComponentService,
            final AvatarService avatarService,
            final UserManager userManager,
            final ProjectBeanFactory projectBeanFactory,
            final VersionBeanFactory versionBeanFactory,
            final PermissionManager permissionManager,
            final ProjectManager projectManager,
            final AvatarManager avatarManager,
            final AvatarPickerHelper avatarPickerHelper,
            final AttachmentHelper attachmentHelper,
            final JiraBaseUrls jiraBaseUrls)
    {
        this.avatarResourceHelper = new AvatarResourceHelper(authContext, avatarManager, avatarPickerHelper, attachmentHelper);
        this.permissionManager = permissionManager;
        this.avatarService = avatarService;
        this.projectService = projectService;
        this.authContext = authContext;
        this.versionService = versionService;
        this.projectComponentService = projectComponentService;
        this.userManager = userManager;
        this.projectBeanFactory = projectBeanFactory;
        this.versionBeanFactory = versionBeanFactory;
        this.uriInfo = uriInfo;
        this.projectManager = projectManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    /**
     * Contains a full representation of a project in JSON format.
     *
     * @param key the project key
     * @return a project
     *
     * @response.representation.200.qname
     *      project
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view it. Contains a full representation
     *      of a project in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{key}")
    public Response getProject(@PathParam ("key") final String key)
    {
        final ProjectService.GetProjectResult result = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.VIEW_PROJECT);

        if (result.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(result.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }
        else
        {
            return Response.ok(projectBeanFactory.fullProject(result.getProject())).cacheControl(never()).build();
        }
    }

    /**
     * Contains a full representation of a the specified project's versions.
     *
     * @param key the project key
     * @return the passed project's versions.
     *
     * @response.representation.200.qname
     *      versions
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view its versions. Contains a full
     *      representation of the project's versions in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{key}/versions")
    public Response getProjectVersions(@PathParam ("key") final String key, @QueryParam ("expand") String expand)
    {
        User loggedInUser = authContext.getLoggedInUser();
        ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(loggedInUser, key, ProjectAction.VIEW_PROJECT);
        if (!projectResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(projectResult.getErrorCollection()));
        }

        VersionService.VersionsResult versionResult = versionService.getVersionsByProject(loggedInUser, projectResult.getProject());
        if (!versionResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(versionResult.getErrorCollection()));
        }

        boolean expandOps = expand != null && expand.contains("operations");
        return Response.ok(versionBeanFactory.createVersionBeans(versionResult.getVersions(), expandOps)).cacheControl(never()).build();
    }

    /**
     * Contains a full representation of a the specified project's components.
     *
     * @param key the project key
     * @return the passed project's components.
     *
     * @response.representation.200.qname
     *      components
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view its components. Contains a full
     *      representation of the project's components in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{key}/components")
    public Response getProjectComponents(@PathParam ("key") final String key)
    {
        User loggedInUser = authContext.getLoggedInUser();
        ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(loggedInUser, key, ProjectAction.VIEW_PROJECT);
        if (!projectResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(projectResult.getErrorCollection()));
        }

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Project project = projectResult.getProject();
        Collection<ProjectComponent> projectComponents = projectComponentService.findAllForProject(errorCollection, project.getId());
        if (errorCollection.hasAnyErrors())
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }

        final Long assigneeType = project.getAssigneeType();
        final long safeAssigneeType = assigneeType == null ? AssigneeTypes.PROJECT_LEAD : assigneeType;
        return Response.ok(ComponentBean.asFullBeans(projectComponents, uriInfo, project.getLeadUserName(), safeAssigneeType, userManager, avatarService, permissionManager, projectManager)).cacheControl(never()).build();
    }

    /**
     * Returns all projects which are visible for the currently logged in user. If no user is logged in, it returns the
     * list of projects that are visible when using anonymous access.
     *
     * @since v4.3
     *
     * @return all projects for which the user has the BROWSE project permission. If no user is logged in,
     *         it returns all projects, which are visible when using anonymous access.
     *
     * @response.representation.200.qname
     *      projects
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of projects for which the user has the BROWSE, ADMINISTER or PROJECT_ADMIN project permission.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectBean#PROJECTS_EXAMPLE}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of projects.
     */
    @GET
    public Response getAllProjects()
    {
        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjectsForAction(authContext.getLoggedInUser(), ProjectAction.VIEW_PROJECT);
        if (outcome.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(outcome.getErrorCollection());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errors).cacheControl(never()).build();
        }
        else
        {
            final List<ProjectJsonBean> beans = new ArrayList<ProjectJsonBean>();
            for (Project project : outcome.getReturnedValue())
            {
                final ProjectJsonBean projectBean = ProjectJsonBean.shortBean(project, jiraBaseUrls);
                beans.add(projectBean);
            }
            return Response.ok(beans).cacheControl(never()).build();
        }
    }

    /**
     * Returns all avatars which are visible for the currently logged in user.  The avatars are grouped into
     * system and custom.
     *
     * @since v5.0
     * @param key project key
     *
     * @return all avatars for which the user has the BROWSE project permission.
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of avatars for both custom an system avatars, which the user has the BROWSE project permission.
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have VIEW PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    @GET
    @Path ("{key}/avatars")
    public Response getAllAvatars(@PathParam ("key") final String key)
    {
        final ProjectService.GetProjectResult result = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.VIEW_PROJECT);

        if (!result.isValid())
        {
            final ErrorCollection errors = ErrorCollection.of(result.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }
        else
        {
            final Project project = result.getProject();
            final Avatar selectedAvatar = project.getAvatar();

            Long selectedAvatarId = null;

            if (selectedAvatar != null)
            {
                selectedAvatarId = selectedAvatar.getId();
            }

            return avatarResourceHelper.getAllAvatars(Avatar.Type.PROJECT, project.getId().toString(), selectedAvatarId);
        }
    }

    /**
     * Converts temporary avatar into a real avatar
     *
     * @param key project key
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
    @Path ("{key}/avatar")
    public Response createAvatarFromTemporary(@PathParam ("key") final String key, final AvatarCroppingBean croppingInstructions)
    {
        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }

        final Project project = projectResult.getProject();
        return avatarResourceHelper.createAvatarFromTemporary(Avatar.Type.PROJECT, project.getId().toString(), croppingInstructions);
    }


    @PUT
    @Path ("{key}/avatar")
    public Response updateProjectAvatar(final @PathParam ("key") String key, final AvatarBean avatarBean)
    {
        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }

        final Project project = projectResult.getProject();

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
        final ProjectService.UpdateProjectValidationResult updateProjectValidationResult =
                projectService.validateUpdateProject(authContext.getLoggedInUser(), project.getName(), key,
                        project.getDescription(), project.getLeadUserName(), project.getUrl(), project.getAssigneeType(),
                        avatarId);

        if (!updateProjectValidationResult.isValid())
        {
            throwWebException(updateProjectValidationResult.getErrorCollection());
        }

        projectService.updateProject(updateProjectValidationResult);
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }


    /**
     * Creates temporary avatar
     *
     * @since v5.0
     *
     * @param key Project key
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
     * @response.representation.400.doc
     *      Valiation failed. For example filesize is beyond max attachment size.
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("{key}/avatar/temporary")
    public Response storeTemporaryAvatar(final @PathParam ("key") String key, final @QueryParam ("filename") String filename,
            final @QueryParam ("size") Long size, final @Context HttpServletRequest request)
    {

        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }

        final Project project = projectResult.getProject();

        return avatarResourceHelper.storeTemporaryAvatar(Avatar.Type.PROJECT, project.getId().toString(), filename, size, request);
    }

    /**
     * Creates temporary avatar using multipart. The response is sent back as JSON stored in a textarea. This is because
     * the client uses remote iframing to submit avatars using multipart. So we must send them a valid HTML page back from
     * which the client parses the JSON.
     *
     * @since v5.0
     *
     * @param key Project key
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
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @Path ("{key}/avatar/temporary")
    @Produces ({ MediaType.TEXT_HTML })
    public Response storeTemporaryAvatarUsingMultiPart(@PathParam ("key") String key, final @MultipartFormParam ("avatar") FilePart filePart, final @Context HttpServletRequest request)
    {

        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }

        final Project project = projectResult.getProject();
        return avatarResourceHelper.storeTemporaryAvatarUsingMultiPart(Avatar.Type.PROJECT, project.getId().toString(), filePart, request);
    }


    /**
     * Deletes avatar
     *
     * @since v5.0
     *
     * @param key Project key
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
     *      Returned if the currently authenticated user does not have permission to delete the component.
     *
     * @response.representation.404.doc
     *      Returned if the avatar does not exist or the currently authenticated user does not have permission to
     *      delete it.
     */
    @DELETE
    @Path ("{key}/avatar/{id}")
    public Response deleteAvatar(final @PathParam ("key") String key, final @PathParam ("id") Long id)
    {

        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), key,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());

            return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
        }

        return avatarResourceHelper.deleteAvatar(id);
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

}
