package com.atlassian.jira.collector.plugin.rest;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.collector.plugin.components.ErrorLog;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path ("collector")
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class CollectorResource
{
    private final CollectorService collectorService;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;
    private final ErrorLog errorLog;
    private final PermissionManager permissionManager;

    public CollectorResource(final CollectorService collectorService, final JiraAuthenticationContext authenticationContext,
            final ProjectService projectService, final ErrorLog errorLog, final PermissionManager permissionManager)
    {
        this.collectorService = collectorService;
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
        this.errorLog = errorLog;
        this.permissionManager = permissionManager;
    }

    @POST
    @Path ("{projectKey}/{collectorId}/status")
    public Response enableCollector(@PathParam ("projectKey") String projectKey, @PathParam ("collectorId") String collectorId)
    {
        final Project project = findProjectByKey(projectKey);
        final Collector collector = findCollectorById(collectorId);

        collectorService.enableCollector(authenticationContext.getLoggedInUser(), project, collector.getId());
        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
    }

    @DELETE
    @Path ("{projectKey}/{collectorId}/status")
    public Response disableCollector(@PathParam ("projectKey") String projectKey, @PathParam ("collectorId") String collectorId)
    {
        final Project project = findProjectByKey(projectKey);
        final Collector collector = findCollectorById(collectorId);

        collectorService.disableCollector(authenticationContext.getLoggedInUser(), project, collector.getId());
        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
    }

    @DELETE
    @Path ("{projectKey}/{collectorId}")
    public Response deleteCollector(@PathParam ("projectKey") String projectKey, @PathParam ("collectorId") String collectorId)
    {
        final Project project = findProjectByKey(projectKey);
        final Collector collector = findCollectorById(collectorId);

        final ServiceOutcome<Collector> validateDelete = collectorService.validateDeleteCollector(authenticationContext.getLoggedInUser(), project, collector.getId());
        if (validateDelete.isValid())
        {
            collectorService.deleteCollector(authenticationContext.getLoggedInUser(), validateDelete);
            return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
        }
        else
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(validateDelete.getErrorCollection()).cacheControl(CacheControl.NO_CACHE).build();
        }
    }

    @DELETE
    @Path ("{projectKey}/errors")
    public Response clearErrors(@PathParam ("projectKey") String projectKey)
    {
        final Project project = findProjectByKey(projectKey);
        if (!hasProjectAdminPermission(project))
        {
            return Response.status(Response.Status.FORBIDDEN).cacheControl(CacheControl.NO_CACHE).build();
        }

        errorLog.clearErrors(project);
        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
    }

    private boolean hasProjectAdminPermission(Project project)
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, authenticationContext.getLoggedInUser()) ||
                permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
    }

    /**
     * Returns the {@code Project} with the given key.
     *
     * @param projectKey a String containing a project key
     * @return a Project
     * @throws WebApplicationException if the project does not exist or the user does not have permission to view it
     */
    @Nonnull
    private Project findProjectByKey(String projectKey) throws WebApplicationException
    {
        ProjectService.GetProjectResult outcome = projectService.getProjectByKey(authenticationContext.getLoggedInUser(), projectKey);
        if (!outcome.isValid() || outcome.getProject() == null)
        {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).cacheControl(CacheControl.NO_CACHE).build());
        }

        return outcome.getProject();
    }

    /**
     * Returns the collector with the given {@code id}.
     *
     * @param collectorId a String containing a collector id
     * @return a Collector
     * @throws WebApplicationException if the collector does not exist or the user does not have permission to view it
     */
    @Nonnull
    private Collector findCollectorById(String collectorId) throws WebApplicationException
    {
        final ServiceOutcome<Collector> outcome = collectorService.getCollector(collectorId);
        if (!outcome.isValid() || outcome.getReturnedValue() == null)
        {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).cacheControl(CacheControl.NO_CACHE).build());
        }

        return outcome.getReturnedValue();
    }
}
