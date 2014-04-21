package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.version.RemoveVersionAction;
import com.atlassian.jira.bc.project.version.SwapVersionAction;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.BadRequestWebException;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionIssueCountsBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionMoveBean;
import com.atlassian.jira.rest.v2.issue.version.VersionUnresolvedIssueCountsBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.FORBIDDEN;
import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @since 4.2
 */
@Path ("version")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class VersionResource
{
    private VersionService versionService;
    private ProjectService projectService;
    private JiraAuthenticationContext authContext;
    private ContextI18n i18n;
    private VersionBeanFactory versionBeanFactory;
    private VersionIssueCountsBeanFactory versionIssueCountsBeanFactory;
    private VersionUnresolvedIssueCountsBeanFactory versionUnresolvedIssueCountsBeanFactory;

    @SuppressWarnings ( { "UnusedDeclaration" })
    private VersionResource()
    {
        // this constructor used by tooling
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    public VersionResource(final VersionService versionService, ProjectService projectService,
            final JiraAuthenticationContext authContext, final ContextI18n i18n, final VersionBeanFactory versionBeanFactory,
            final VersionIssueCountsBeanFactory versionIssueCountsBeanFactory, final VersionUnresolvedIssueCountsBeanFactory versionUnresolvedIssueCountsBeanFactory)
    {
        this.projectService = projectService;
        this.authContext = authContext;
        this.versionService = versionService;
        this.i18n = i18n;
        this.versionBeanFactory = versionBeanFactory;
        this.versionIssueCountsBeanFactory = versionIssueCountsBeanFactory;
        this.versionUnresolvedIssueCountsBeanFactory = versionUnresolvedIssueCountsBeanFactory;
    }

    /**
     * Returns a project version.
     *
     * @param id a String containing the version id
     * @return a project version
     *
     * @response.representation.200.qname
     *      project
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains a
     *      full representation of the version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path ("{id}")
    public Response getVersion(@PathParam ("id") final String id, @QueryParam("expand") String expand)
    {
        try
        {
            final VersionService.VersionResult result = versionService.getVersionById(authContext.getLoggedInUser(), Long.parseLong(id));
            if (!result.isValid())
            {
                throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
            }

            boolean expandOps = expand != null && expand.contains("operations");
            return Response.ok(versionBeanFactory.createVersionBean(result.getVersion(), expandOps)).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", id)));
        }
    }

    /**
     * Modify a version via PUT. Any fields present in the PUT will override existing values. As a convenience, if a field
     * is not present, it is silently ignored.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to edit it.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @PUT
    @Path ("{id}")
    public Response updateVersion(@PathParam ("id") final String id, final VersionBean bean)
    {
        if (bean.isReleaseDateSet() && bean.getUserReleaseDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.two.dates");
        }
        try
        {
            final VersionService.VersionResult result = versionService.getVersionById(authContext.getLoggedInUser(), Long.parseLong(id));
            if (result.isValid())
            {
                com.atlassian.jira.util.ErrorCollection errors = validateUpdate(bean, result.getVersion());
                if (errors.hasAnyErrors())
                {
                    throw new BadRequestWebException(ErrorCollection.of(errors));
                }
            }
            else
            {
                throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
            }
            performUpdate(bean, result.getVersion());
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", id)));
        }

        return getVersion(id, bean.getExpand());
    }

    private com.atlassian.jira.util.ErrorCollection validateUpdate(VersionBean bean, Version currentVersion)
    {
        com.atlassian.jira.util.ErrorCollection errors = validateNameDescription(bean, currentVersion);

        if (bean.isArchived() != null)
        {
            errors.addErrorCollection(validateArchived(bean, currentVersion));
        }

        if (bean.isReleased() != null)
        {
            errors.addErrorCollection(validateReleased(bean, currentVersion));
        }

        if (bean.isReleaseDateSet() &&
                (bean.getReleaseDate() == null || !bean.getReleaseDate().equals(currentVersion.getReleaseDate())))
        {
            // nothing to validate
        }
        else if (bean.getUserReleaseDate() != null)
        {
            errors.addErrorCollection(validateReleaseDate(bean, currentVersion));
        }
        return errors;
    }

    private void performUpdate(VersionBean bean, Version currentVersion)
    {
        // First update the version's name and description
        updateNameDescription(bean, currentVersion);

        if (bean.isArchived() != null)
        {
            updateArchived(bean, currentVersion);
        }

        if (bean.isReleased() != null)
        {
            updateReleased(bean, currentVersion);
        }

        if (bean.isReleaseDateSet() &&
                (bean.getReleaseDate() == null || !bean.getReleaseDate().equals(currentVersion.getReleaseDate())))
        {
            updateReleaseDate(bean, currentVersion);
        }
        else if (bean.getUserReleaseDate() != null)
        {
            updateReleaseDate(bean, currentVersion);
        }
    }

    private com.atlassian.jira.util.ErrorCollection validateReleased(VersionBean bean, Version currentVersion)
    {
        com.atlassian.jira.util.ErrorCollection errors = validateNameDescription(bean, currentVersion);
        // Only perform the release if we are currently unreleased
        if (bean.isReleased() && !currentVersion.isReleased())
        {
            VersionService.ReleaseVersionValidationResult result;
            if (bean.isReleaseDateSet())
            {
                result = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                result = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                result = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, currentVersion.getReleaseDate());
            }
            errors.addErrorCollection(result.getErrorCollection());
        }
        // Only perform unrelease if we are currently released
        else if (!bean.isReleased() && currentVersion.isReleased())
        {
            VersionService.ReleaseVersionValidationResult result;
            if (bean.isReleaseDateSet())
            {
                result = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                result = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                result = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, currentVersion.getReleaseDate());
            }
            errors.addErrorCollection(result.getErrorCollection());
        }
        return errors;
    }

    private void updateReleased(VersionBean bean, Version currentVersion)
    {
        // Only perform the release if we are currently unreleased
        VersionService.ReleaseVersionValidationResult validationResult;
        if (bean.isReleased() && !currentVersion.isReleased())
        {
            if (bean.isReleaseDateSet())
            {
                validationResult = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                validationResult = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                validationResult = versionService.validateReleaseVersion(authContext.getLoggedInUser(), currentVersion, currentVersion.getReleaseDate());
            }
            if (validationResult.isValid())
            {
                if (bean.getMoveUnfixedIssuesTo() != null)
                {
                    long moveToId = getVersionIdFromSelfLink(bean.getMoveUnfixedIssuesTo().getPath());
                    final VersionService.VersionResult result = versionService.getVersionById(authContext.getLoggedInUser(), moveToId);
                    if (!result.isValid())
                    {
                        throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
                    }

                    versionService.moveUnreleasedToNewVersion(authContext.getLoggedInUser(), currentVersion, result.getVersion());
                }
                versionService.releaseVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
        // Only perform unrelease if we are currently released
        else if (!bean.isReleased() && currentVersion.isReleased())
        {
            if (bean.isReleaseDateSet())
            {
                validationResult = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                validationResult = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                validationResult = versionService.validateUnreleaseVersion(authContext.getLoggedInUser(), currentVersion, currentVersion.getReleaseDate());
            }
            if (validationResult.isValid())
            {
                versionService.unreleaseVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }

    private com.atlassian.jira.util.ErrorCollection validateReleaseDate(VersionBean bean, Version currentVersion)
    {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        if (bean.isReleaseDateSet())
        {
        }
        else
        {
            ServiceOutcome<Version> outcome = versionService.setReleaseDate(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
            errors.addErrorCollection(outcome.getErrorCollection());
        }
        return errors;
    }

    private void updateReleaseDate(VersionBean bean, Version currentVersion)
    {
        final ServiceOutcome<Version> outcome;
        if (bean.isReleaseDateSet())
        {
            outcome = versionService.setReleaseDate(authContext.getLoggedInUser(), currentVersion, bean.getReleaseDate());
        }
        else
        {
            outcome = versionService.setReleaseDate(authContext.getLoggedInUser(), currentVersion, bean.getUserReleaseDate());
        }
        if (!outcome.isValid())
        {
            throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    private com.atlassian.jira.util.ErrorCollection validateArchived(VersionBean bean, Version currentVersion)
    {
        // Only perform archiving if we are not already archived.
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        if (bean.isArchived() && !currentVersion.isArchived())
        {
            VersionService.ArchiveVersionValidationResult validationResult = versionService.validateArchiveVersion(authContext.getLoggedInUser(), currentVersion);
            errors.addErrorCollection(validationResult.getErrorCollection());
        }
        // Only perform unarchive is we are currently archived
        else if (!bean.isArchived() && currentVersion.isArchived())
        {
            VersionService.ArchiveVersionValidationResult validationResult = versionService.validateUnarchiveVersion(authContext.getLoggedInUser(), currentVersion);
            errors.addErrorCollection(validationResult.getErrorCollection());
        }
        return errors;
    }

    private void updateArchived(VersionBean bean, Version currentVersion)
    {
        // Only perform archiving if we are not already archived.
        if (bean.isArchived() && !currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateArchiveVersion(authContext.getLoggedInUser(), currentVersion);
            if (validationResult.isValid())
            {
                versionService.archiveVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
        // Only perform unarchive is we are currently archived
        else if (!bean.isArchived() && currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateUnarchiveVersion(authContext.getLoggedInUser(), currentVersion);
            if (validationResult.isValid())
            {
                versionService.unarchiveVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }

    private com.atlassian.jira.util.ErrorCollection validateNameDescription(VersionBean bean, Version currentVersion)
    {
        String name = currentVersion.getName();
        String description = currentVersion.getDescription();

        if (bean.getDescription() != null)
        {
            description = bean.getDescription();
        }
        if (bean.getName() != null)
        {
            name = bean.getName();
        }
        com.atlassian.jira.util.ErrorCollection errors = versionService.validateVersionDetails(authContext.getLoggedInUser(), currentVersion, name, description);
        return errors;
    }

    private void updateNameDescription(VersionBean bean, Version currentVersion)
    {
        String name = currentVersion.getName();
        String description = currentVersion.getDescription();

        if (bean.getDescription() != null)
        {
            description = bean.getDescription();
        }
        if (bean.getName() != null)
        {
            name = bean.getName();
        }
        final ServiceOutcome<Version> outcome = versionService.setVersionDetails(authContext.getLoggedInUser(), currentVersion, name, description);
        if (!outcome.isValid())
        {
            throwWebException(outcome.getErrorCollection());
        }
    }

    /**
     * Create a version via POST.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_CREATE_EXAMPLE}
     *      Supply only one of releaseDate or userReleaseDate but not both.
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returned if the version is created successfully.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_CREATE_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @POST
    public Response createVersion(final VersionBean bean)
    {
        if (isBlank(bean.getProject()))
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.no.project");
        }
        if (bean.isReleaseDateSet() && bean.getUserReleaseDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.two.dates");
        }

        User user = authContext.getLoggedInUser();
        //We must use the manager because you can create a version without browse permission. The service tries to
        //ensure the user has browse permission.
        ProjectService.GetProjectResult getResult = projectService.getProjectByKeyForAction(user, bean.getProject(),
                ProjectAction.EDIT_PROJECT_CONFIG);
        if (!getResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.version.no.create.permission", bean.getProject())));
        }

        final VersionService.CreateVersionValidationResult createResult;
        if (bean.isReleaseDateSet())
        {
            createResult = versionService.validateCreateVersion(user, getResult.getProject(), bean.getName(),
                    bean.getReleaseDate(), bean.getDescription(), null);
        }
        else
        {
            createResult = versionService.validateCreateVersion(user, getResult.getProject(), bean.getName(),
                    bean.getUserReleaseDate(), bean.getDescription(), null);
        }

        if (!createResult.isValid())
        {
            if (createResult.getReasons().contains(FORBIDDEN))
            {
                ErrorCollection errors = ErrorCollection.of(
                        i18n.getText("rest.version.no.create.permission", bean.getProject()));

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errors)
                        .cacheControl(never()).build();
            }
            else
            {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorCollection.of(createResult.getErrorCollection())).cacheControl(never()).build();
            }
        }
        else
        {
            boolean expandOps = bean.getExpand() != null && bean.getExpand().contains("operations");
            Version newVersion = versionService.createVersion(user, createResult);
            final VersionBean versionBean = versionBeanFactory.createVersionBean(newVersion, expandOps);
            return Response.status(Response.Status.CREATED)
                    .entity(versionBean)
                    .location(versionBean.getSelf())
                    .cacheControl(never()).build();
        }
    }

    private Response createErrorResponse(Response.Status status, String key)
    {
        ErrorCollection errorCollection = ErrorCollection.of(i18n.getText(key));
        return Response.status(status).entity(errorCollection).cacheControl(never()).build();
    }

    /**
     * Delete a project version.
     * @param id The version to delete
     * @param moveFixIssuesTo The version to set fixVersion to on issues where the deleted version is the fix version,
     * If null then the fixVersion is removed.
     * @param moveAffectedIssuesTo The version to set affectedVersion to on issues where the deleted version is the affected version,
     * If null then the affectedVersion is removed.
     * @return An empty or error response.
     *
     *
     * @response.representation.204.doc
     *      Returned if the version is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @DELETE
    @Path ("{id}")
    public Response delete(@PathParam ("id") final String id, @QueryParam ("moveFixIssuesTo") String moveFixIssuesTo,
            @QueryParam("moveAffectedIssuesTo") String moveAffectedIssuesTo)
    {
        long versionId = -1;
        try
        {
            versionId = Long.parseLong(id);
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", versionId)));
        }
        final VersionService.VersionResult result = versionService.getVersionById(authContext.getLoggedInUser(), versionId);
        if (!result.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
        }

        // Get the actions to handle on delete
        VersionService.VersionAction fixAction;
        VersionService.VersionAction affectedAction;
        if (moveFixIssuesTo != null)
        {
            fixAction = new SwapVersionAction(getVersionIdFromSelfLink(moveFixIssuesTo));
        }
        else
        {
            fixAction = new RemoveVersionAction();
        }
        if (moveAffectedIssuesTo != null)
        {
            affectedAction = new SwapVersionAction(getVersionIdFromSelfLink(moveAffectedIssuesTo));
        }
        else
        {
            affectedAction = new RemoveVersionAction();
        }
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser());
        VersionService.ValidationResult deleteValidationResult =
                versionService.validateDelete(serviceContext, versionId, affectedAction, fixAction);

        checkDeleteResult(deleteValidationResult);

        versionService.delete(serviceContext, deleteValidationResult);

        return Response.noContent().cacheControl(never()).build();
    }

    private void checkDeleteResult(VersionService.ValidationResult validationResult)
    {
        if (!validationResult.isValid())
        {
            if (validationResult.getReasons().contains(VersionService.ValidationResult.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(validationResult.getErrorCollection()));
            }
            if (validationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.NOT_FOUND))
            {
                throw new NotFoundWebException(ErrorCollection.of(validationResult.getErrorCollection()));
            }
            else
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }

    /**
     * Returns a bean containing the number of fixed in and affected issues for the given version.
     *
     * @param id a String containing the version id
     * @return an issue counts bean
     *
     * @response.representation.200.qname
     *      issue Count Bean
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains
     *      counts of issues fixed in and affecting this version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionIssueCountsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path ("{id}/relatedIssueCounts")
    public Response getVersionRelatedIssues(@PathParam ("id") final String id)
    {
        try
        {
            final VersionService.VersionResult result = versionService.getVersionById(authContext.getLoggedInUser(), Long.parseLong(id));
            if (!result.isValid())
            {
                throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
            }

            long fixIssueCount = versionService.getFixIssuesCount(result.getVersion());
            long affectsIssueCount = versionService.getAffectsIssuesCount(result.getVersion());

            return Response.ok(versionIssueCountsBeanFactory.createVersionBean(
                    result.getVersion(), fixIssueCount, affectsIssueCount)).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", id)));
        }
    }

    /**
     * Returns the number of unresolved issues for the given version
     *
     * @param id a String containing the version id
     * @return an unresolved issue count bean
     *
     * @response.representation.200.qname
     *      issuesUnresolvedCount
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains
     *      counts of issues unresolved in this version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionUnresolvedIssueCountsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path ("{id}/unresolvedIssueCount")
    public Response getVersionUnresolvedIssues(@PathParam ("id") final String id)
    {
        try
        {
            final User loggedInUser = authContext.getLoggedInUser();
            final VersionService.VersionResult result = versionService.getVersionById(loggedInUser, Long.parseLong(id));
            if (!result.isValid())
            {
                throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
            }

            long unresolvedIssueCount = versionService.getUnresolvedIssuesCount(loggedInUser, result.getVersion());

            return Response.ok(versionUnresolvedIssueCountsBeanFactory.createVersionBean(
                    result.getVersion(), unresolvedIssueCount)).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", id)));
        }
    }

    /**
     * Modify a version's sequence within a project.
     *
     * The move version bean has 2 alternative field value pairs:
     * <dl>
     *     <dt>position</dt><dd>An absolute position, which may have a value of 'First', 'Last', 'Earlier' or 'Later'</dd>
     *     <dt>after</dt><dd>A version to place this version after.  The value should be the self link of another version</dd>
     * </dl>
     *
     * @param id a String containing the version id
     * @param bean a MoveVersionBean that describes the move to be performed.
     * @return a project version
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionMoveBean#DOC_EXAMPLE}
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionMoveBean#DOC_EXAMPLE2}
     *
     * @response.representation.200.qname
     *      project version
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains a
     *      full representation of the version moved.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version, or target of the version to move after does not exist or the currently authenticated
     *      user does not have permission to view it.
     */
    @POST
    @Path ("/{id}/move")
    public Response moveVersion(@PathParam ("id") final String id, VersionMoveBean bean)
    {
        // Backbone makes it really hard to plug into the move method, so for ease all move methods get the operations
        final String expand = "operations";

        long versionId = -1;
        try
        {
            versionId = Long.parseLong(id);
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", versionId)));
        }

        User user = authContext.getLoggedInUser();
        // The version can be moved to the top or bottom or after another version
        if (bean.position != null)
        {
            switch (bean.position)
            {
                case Earlier:
                {
                    VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateIncreaseVersionSequence(user, Long.parseLong(id));
                    checkMoveResult(moveValidationResult);
                    versionService.increaseVersionSequence(moveValidationResult);
                    break;
                }
                case Later:
                {
                    VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateDecreaseVersionSequence(user, Long.parseLong(id));
                    checkMoveResult(moveValidationResult);
                    versionService.decreaseVersionSequence(moveValidationResult);
                    break;
                }
                case First:
                {
                    VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveToStartVersionSequence(user, Long.parseLong(id));
                    checkMoveResult(moveValidationResult);
                    versionService.moveToStartVersionSequence(moveValidationResult);
                    break;
                }
                case Last:
                {
                    VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveToEndVersionSequence(user, Long.parseLong(id));
                    checkMoveResult(moveValidationResult);
                    versionService.moveToEndVersionSequence(moveValidationResult);
                    break;
                }
                default :
                {
                    throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("admin.errors.version.move.target.invalid"));
                }
            }
        }
        else if (bean.after != null)
        {
            // Get the id from the bean.after URI
            long afterVersionId = getVersionIdFromSelfLink(bean.after.getPath());

            VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveVersionAfter(user, versionId, afterVersionId);
            checkMoveResult(moveValidationResult);
            versionService.moveVersionAfter(moveValidationResult);
        }

        return getVersion(id, expand);
    }

    private long getVersionIdFromSelfLink(String path)
    {
        String versionIdString = path.substring(path.lastIndexOf('/') + 1);
        long afterVersionId = -1;
        try
        {
            afterVersionId = Long.parseLong(versionIdString);
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", afterVersionId)));
        }
        return afterVersionId;
    }

    private void checkMoveResult(VersionService.MoveVersionValidationResult moveValidationResult)
    {
        if (moveValidationResult.getErrorCollection().hasAnyErrors())
        {
            if (moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
            if (moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.NOT_FOUND) ||
                moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.SCHEDULE_AFTER_VERSION_NOT_FOUND))
            {
                throw new NotFoundWebException(ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
            else
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
        }
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }
}
