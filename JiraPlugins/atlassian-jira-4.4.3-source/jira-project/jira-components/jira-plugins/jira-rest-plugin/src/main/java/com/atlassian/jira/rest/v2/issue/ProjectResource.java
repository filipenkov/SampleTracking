package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    private VersionService versionService;
    private ProjectComponentService projectComponentService;
    private ProjectBeanFactory projectBeanFactory;
    private VersionBeanFactory versionBeanFactory;
    private PermissionManager permissionManager;
    private AvatarService avatarService;

    private ProjectResource()
    {
        // this constructor used by tooling
    }

    public ProjectResource(final ProjectService projectService, final JiraAuthenticationContext authContext, final  UriInfo uriInfo,
            final VersionService versionService, final  ProjectComponentService projectComponentService, final AvatarService avatarService, UserManager userManager,
            final ProjectBeanFactory projectBeanFactory,
            final VersionBeanFactory versionBeanFactory,
            final PermissionManager permissionManager,
            final ProjectManager projectManager)
    {
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
            final ErrorCollection errors = ErrorCollection.builder()
                    .addErrorCollection(result.getErrorCollection())
                    .build();

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
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
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
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EXAMPLE}
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
     *      Returns a list of projects for which the user has the BROWSE project permission.
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
            final ErrorCollection errors = ErrorCollection.builder()
                    .addErrorCollection(outcome.getErrorCollection())
                    .build();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errors).cacheControl(never()).build();
        }
        else
        {
            final List<ProjectBean> beans = new ArrayList<ProjectBean>();
            for (Project project : outcome.getReturnedValue())
            {
                final ProjectBean projectBean = projectBeanFactory.shortProject(project);
                beans.add(projectBean);
            }
            return Response.ok(beans).cacheControl(never()).build();
        }
    }
}