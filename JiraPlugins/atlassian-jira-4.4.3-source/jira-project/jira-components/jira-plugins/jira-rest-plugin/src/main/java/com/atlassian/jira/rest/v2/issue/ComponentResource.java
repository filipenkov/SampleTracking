package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.BadRequestWebException;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.ServerErrorWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.component.ComponentIssueCountsBeanFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
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

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @since 4.2
 */
@Path ("component")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ComponentResource
{
    private static final String COMPONENT_LEAD = "componentLead";
    private static final String LEAD_USER_NAME = "leadUserName";
    private ProjectComponentService projectComponentService;
    private ProjectService projectService;
    private UserManager userManager;
    private AvatarService avatarService;
    private IssueManager issueManager;
    private JiraAuthenticationContext authContext;
    private ContextI18n i18n;
    private PermissionManager permissionManager;
    private ProjectManager projectManager;
    private ComponentIssueCountsBeanFactory componentIssueCountsBeanFactory;

    private ComponentResource()
    {
        // this constructor used by tooling
    }

    public ComponentResource(final ProjectComponentService projectComponentService, final ProjectService projectService,
            final UserManager userManager, final AvatarService avatarService, final IssueManager issueManager, final ComponentIssueCountsBeanFactory componentIssueCountsBeanFactory,
            final JiraAuthenticationContext authContext, ContextI18n i18n, final PermissionManager permissionManager, final ProjectManager projectManager)
    {
        this.projectComponentService = projectComponentService;
        this.projectService = projectService;
        this.userManager = userManager;
        this.avatarService = avatarService;
        this.issueManager = issueManager;
        this.componentIssueCountsBeanFactory = componentIssueCountsBeanFactory;
        this.authContext = authContext;
        this.i18n = i18n;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
    }

    /**
     * Returns a project component.
     *
     * @param id a String containing the component key
     * @param uriInfo a UriInfo
     *
     * @return a response containing a JSOn representation of a project component, or a 404 NOT FOUND if there is no
     *      component with the given id or the calling user does not have permission to view it.
     *
     * @response.representation.200.qname
     *      component
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a full JSON representation of a project component.
     *
     * @response.representation.404.doc
     *      Returned if there is no component with the given key, or if the calling user does not have permission to
     *      view the component.
     */
    @GET
    @Path ("{id}")
    public Response getComponent(@PathParam ("id") final String id, @Context UriInfo uriInfo)
    {
        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            final ProjectComponent component = projectComponentService.find(authContext.getUser(), errorCollection, Long.parseLong(id));
            if (errorCollection.hasAnyErrors())
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }
            Project project = getProject(component);
            final ComponentBean componentBean = getComponentBean(uriInfo, component, project);

            return Response.ok(componentBean).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nosuchcomponent.withid", id)));
        }
    }

    private ComponentBean getComponentBean(UriInfo uriInfo, ProjectComponent component, Project project)
    {// Get the component assignee details
        Long projectAssigneeType = project.getAssigneeType();
        final long safeAssigneeType = projectAssigneeType == null ? AssigneeTypes.PROJECT_LEAD : projectAssigneeType;

        String projectLeadUserName = project.getLeadUserName();

        return ComponentBean.fullComponent(
                component, uriInfo, projectLeadUserName, safeAssigneeType, userManager, avatarService, permissionManager, projectManager);
    }

    /**
     * Create a component via POST. Any fields present in the PUT will override existing values. As a convenience, if a field
     * is not present, it is silently ignored.
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returned if the component is created successfully.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EXAMPLE}
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_CREATE_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the component.
     *
     * @response.representation.404.doc
     *      Returned if the component does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @POST
    public Response createComponent(final ComponentBean bean, @Context UriInfo uriInfo)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (isBlank(bean.getProject()))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nullprojectid")));
        }
        if (StringUtils.isEmpty(bean.getName()))
        {
            errorCollection.addError("name", i18n.getText("admin.projects.component.namenotset"));
            throw new BadRequestWebException(ErrorCollection.of(errorCollection));
        }

        User user = authContext.getLoggedInUser();
        ProjectService.GetProjectResult getResult = projectService.getProjectByKeyForAction(user, bean.getProject(),
                ProjectAction.EDIT_PROJECT_CONFIG);
        if (!getResult.isValid())
        {
            throwWebException(getResult.getErrorCollection());
        }
        Project project = getResult.getProject();

        // Get the component lead user name, from either the lead, which is a bean, or the leadUserName
        String lead = bean.getLead() != null ? bean.getLead().getName() : bean.getLeadUserName();
        ProjectComponent newComponent =
                projectComponentService.create(user, errorCollection, bean.getName(),
                        bean.getDescription(), lead, project.getId());

        if (errorCollection.hasAnyErrors())
        {
            throwWebException(errorCollection);
        }

        // Now set the assignee type
        MutableProjectComponent projectComponent = MutableProjectComponent.copy(newComponent);
        if (bean.getAssigneeType() != null)
        {
            projectComponent.setAssigneeType(bean.getAssigneeType().getId());
        }

        ProjectComponent updatedComponent = projectComponentService.update(user, errorCollection, projectComponent);
//        project = getProject(updatedComponent);
        // Get the component assignee details
        final ComponentBean componentBean = getComponentBean(uriInfo, updatedComponent, project);
        return Response.status(Response.Status.CREATED)
                .entity(componentBean)
                .location(componentBean.getSelf())
                .cacheControl(never()).build();
    }

    private Project getProject(ProjectComponent component)
    {
        ProjectService.GetProjectResult projectResult = projectService.getProjectById(authContext.getUser(), component.getProjectId());
        if (!projectResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(projectResult.getErrorCollection()));
        }

        return projectResult.getProject();
    }
    private Project getProjectForAdmin(ProjectComponent component)
    {
        ProjectService.GetProjectResult projectResult = projectService.getProjectByIdForAction(authContext.getUser(), component.getProjectId(), ProjectAction.EDIT_PROJECT_CONFIG);
        if (!projectResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(projectResult.getErrorCollection()));
        }

        return projectResult.getProject();
    }

    /**
     * Modify a component via PUT. Any fields present in the PUT will override existing values. As a convenience, if a field
     * is not present, it is silently ignored.
     *
     * If leadUserName is an empty string ("") the component lead will be removed.
     *
     * @response.representation.200.doc
     *      Returned if the component exists and the currently authenticated user has permission to edit it.
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EDIT_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the component.
     *
     * @response.representation.404.doc
     *      Returned if the component does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @PUT
    @Path ("{id}")
    public Response updateComponent(@PathParam ("id") final String id, @Context UriInfo uriInfo, final ComponentBean bean)
    {
        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            final ProjectComponent currentComponent = projectComponentService.find(authContext.getUser(), errorCollection, Long.parseLong(id));
            if (!errorCollection.hasAnyErrors())
            {
                // First update the component's name and description
                MutableProjectComponent mutableComponent = MutableProjectComponent.copy(currentComponent);
                update(bean, mutableComponent);
                final ProjectComponent projectComponent = projectComponentService.update(authContext.getUser(), errorCollection, mutableComponent);
                if (errorCollection.hasAnyErrors())
                {
                    throwWebException(errorCollection);
                }
                final ComponentBean componentBean = getComponentBean(uriInfo, projectComponent, getProjectForAdmin(projectComponent));
                return Response.ok(componentBean).cacheControl(never()).build();
            }
            else
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nosuchcomponent.withid", id)));
        }

    }

    private void update(ComponentBean bean, MutableProjectComponent currentComponent)
    {
        if (bean.getDescription() != null)
        {
            currentComponent.setDescription(bean.getDescription());
        }
        if (bean.getName() != null)
        {
            currentComponent.setName(bean.getName());
        }
        // Get the component lead user name, from either the lead, which is a bean, or the leadUserName
        if (bean.getLead() != null)
        {
            currentComponent.setLead(bean.getLead().getName());
        }
        else if (bean.getLeadUserName() != null)
        {
            // An empty string for the lead user name means "remove the component lead"
            if (StringUtils.isEmpty(bean.getLeadUserName()))
            {
                currentComponent.setLead(null);
            }
            else
            {
                currentComponent.setLead(bean.getLeadUserName());
            }
        }
        if (bean.getAssigneeType() != null)
        {
            currentComponent.setAssigneeType(bean.getAssigneeType().getId());
        }
    }


    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {

        if (errorCollection.getErrors().containsKey(COMPONENT_LEAD))
        {
            errorCollection.getErrors().put(LEAD_USER_NAME, errorCollection.getErrors().get(COMPONENT_LEAD));
        }

        if (errorCollection.getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(errorCollection));
        }
        if (errorCollection.getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND))
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }
        if (errorCollection.getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR))
        {
            throw new ServerErrorWebException(ErrorCollection.of(errorCollection));
        }
        throw new BadRequestWebException(ErrorCollection.of(errorCollection));
    }

    /**
     * Returns a project component.
     *
     * @param id a String containing the component id
     * @return an issue counts bean
     *
     * @response.representation.200.qname
     *      issue Count Bean
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the component exists and the currently authenticated user has permission to view it. Contains
     *      counts of issues fixed in and affecting this component.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentIssueCountsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the component does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @AnonymousAllowed
    @GET
    @Path ("{id}/relatedIssueCounts")
    public Response getComponentRelatedIssues(@PathParam ("id") final String id)
    {
        try
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            final ProjectComponent component = projectComponentService.find(authContext.getUser(), errorCollection, Long.parseLong(id));
            if (errorCollection.hasAnyErrors())
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }
            else
            {
                try
                {
                    List<GenericValue> issues = issueManager.getIssuesByEntity(IssueRelationConstants.COMPONENT, component.getGenericValue());
                    return Response.ok(componentIssueCountsBeanFactory.createComponentBean(
                                    component, issues.size())).cacheControl(never()).build();
                }
                catch (GenericEntityException e)
                {
                    throw new ServerErrorWebException(e);
                }
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nosuchcomponent.withid", id)));
        }
     }

    /**
     * Delete a project component.
     *
     * @response.representation.204.doc
     *      Returned if the component is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the component.
     *
     * @response.representation.404.doc
     *      Returned if the component does not exist or the currently authenticated user does not have permission to
     *      view it.

     * @param id The component to delete.
     * @param moveIssuesTo The new component applied to issues whose 'id' component will be deleted.
     * If this value is null, then the 'id' component is simply deleted.
     * @return An empty or error response.
     */
    @DELETE
    @Path ("{id}")
    public Response delete(@PathParam ("id") final String id, @QueryParam ("moveIssuesTo") String moveIssuesTo)
    {
        long componentId = -1;
        try
        {
            componentId = Long.parseLong(id);
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nosuchcomponent.withid", componentId)));
        }
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ProjectComponent component = projectComponentService.find(authContext.getUser(), errorCollection,componentId);
        if (errorCollection.hasAnyErrors())
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }

        JiraServiceContext serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser());
        // Get the actions to handle on delete
        if (moveIssuesTo != null)
        {
            projectComponentService.deleteAndSwapComponentForIssues(serviceContext, componentId, getComponentIdFromSelfLink(moveIssuesTo));
        }
        else
        {
            projectComponentService.deleteComponentForIssues(serviceContext, componentId);
        }

        if (serviceContext.getErrorCollection().hasAnyErrors())
        {
            throwWebException(serviceContext.getErrorCollection());
        }
        return Response.noContent().cacheControl(never()).build();
    }

    private long getComponentIdFromSelfLink(String path)
    {
        String componentIdString = path.substring(path.lastIndexOf('/') + 1);
        long afterComponentId = -1;
        try
        {
            afterComponentId = Long.parseLong(componentIdString);
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.projects.component.nosuchcomponent.withid", afterComponentId)));
        }
        return afterComponentId;
    }

}
