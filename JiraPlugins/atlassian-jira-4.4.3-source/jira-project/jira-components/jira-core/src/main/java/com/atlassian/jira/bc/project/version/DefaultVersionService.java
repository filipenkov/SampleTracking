package com.atlassian.jira.bc.project.version;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.event.project.VersionArchiveEvent;
import com.atlassian.jira.event.project.VersionCreateEvent;
import com.atlassian.jira.event.project.VersionDeleteEvent;
import com.atlassian.jira.event.project.VersionMergeEvent;
import com.atlassian.jira.event.project.VersionMoveEvent;
import com.atlassian.jira.event.project.VersionReleaseEvent;
import com.atlassian.jira.event.project.VersionUnarchiveEvent;
import com.atlassian.jira.event.project.VersionUnreleaseEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @since v3.13
 */
public class DefaultVersionService implements VersionService
{
    private static final Logger log = Logger.getLogger(DefaultVersionService.class);

    private final VersionManager versionManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final IssueIndexManager issueIndexManager;
    private final IssueFactory issueFactory;
    private final SearchProvider searchProvider;
    private final DateFieldFormat dateFieldFormat;
    private final EventPublisher eventPublisher;

    /**
     * The I18nBean.
     */
    private final I18nBean.BeanFactory i18n;

    public DefaultVersionService(final VersionManager versionManager, final PermissionManager permissionManager,
            final IssueManager issueManager, final IssueIndexManager issueIndexManager, SearchProvider searchProvider,
            final IssueFactory issueFactory, I18nHelper.BeanFactory i18n, DateFieldFormat dateFieldFormat,
            final EventPublisher eventPublisher)
    {
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.issueIndexManager = issueIndexManager;
        this.issueFactory = issueFactory;
        this.i18n = i18n;
        this.searchProvider = searchProvider;
        this.dateFieldFormat = dateFieldFormat;
        this.eventPublisher = eventPublisher;
    }

    public VersionResult getVersionById(final User user, final Project project, final Long versionId)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("versionId", versionId);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionResult(errors);
        }

        Version version = versionManager.getVersion(versionId);

        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", versionId));
            return new VersionResult(errors);
        }

        return new VersionResult(errors, version);
    }

    @Override
    public VersionResult getVersionById(com.opensymphony.user.User user, Long versionId)
    {
        return getVersionById((User) user, versionId);
    }

    public VersionResult getVersionById(final User user, final Long versionId)
    {
        Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            I18nHelper i18nBean = getI18nBean(user);
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", versionId));
            return new VersionResult(errors);
        }

        return getVersionById(user, version.getProjectObject(), versionId);
    }

    @Override
    public VersionResult getVersionByProjectAndName(com.opensymphony.user.User user, Project project, String versionName)
    {
        return getVersionByProjectAndName((User) user, project, versionName);
    }

    public VersionResult getVersionByProjectAndName(final User user, final Project project, final String versionName)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("project", project);
        Assertions.notBlank("versionName", versionName);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionResult(errors);
        }

        Version version = versionManager.getVersion(project.getId(), versionName);

        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist", versionName, project.getName()));
            return new VersionResult(errors);
        }

        return new VersionResult(errors, version);
    }

    @Override
    public VersionsResult getVersionsByProject(com.opensymphony.user.User user, Project project)
    {
        return getVersionsByProject((User) user, project);
    }

    public VersionsResult getVersionsByProject(final User user, final Project project)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Assertions.notNull("project", project);

        if (!hasReadPermission(user, project))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.read.permission"));
            return new VersionsResult(errors, Collections.<Version>emptyList());
        }

        return new VersionsResult(errors, versionManager.getVersions(project.getId()));
    }

    @Override
    public CreateVersionValidationResult validateCreateVersion(com.opensymphony.user.User user, Project project, String versionName, String releaseDate, String description, Long scheduleAfterVersion)
    {
        return validateCreateVersion((User) user, project, versionName, releaseDate, description, scheduleAfterVersion);
    }

    private boolean hasReadPermission(final User user, final Project project)
    {
        return hasEditPermission(user, project) || permissionManager.hasPermission(Permissions.BROWSE, project, user);
    }

    private boolean hasEditPermission(final User user, final Project project)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);
    }

    @Override
    public ServiceOutcome<Version> setVersionDetails(final User user, final Version version, final String name, final String description)
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        if (hasEditPermission(user, version.getProjectObject()))
        {
            try
            {
                versionManager.editVersionDetails(version, name, description, version.getProject());
                return ServiceOutcomeImpl.ok(version);
            }
            catch (final GenericEntityException e)
            {
                final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
                errors.addErrorMessage(message, ErrorCollection.Reason.SERVER_ERROR);
                return new ServiceOutcomeImpl(errors);
            }
        }
        else
        {
            final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
            errors.addErrorMessage(message, ErrorCollection.Reason.FORBIDDEN);
            return new ServiceOutcomeImpl(errors);
        }
    }


    @Override
    public ErrorCollection validateVersionDetails(final User user, final Version version, final String name, final String description)
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        if (hasEditPermission(user, version.getProjectObject()))
        {
                //There must be a name for the entity
                if (StringUtils.isBlank(name))
                {
                    final String message = i18n.getInstance(user).getText("admin.errors.projectversions.must.specify.version.name");
                    errors.addError("name", message, ErrorCollection.Reason.VALIDATION_FAILED);
                }
                else
                {
                    //if the name already exists then add an Error message
                    if (versionManager.isDuplicateName(version, name, version.getProject()))
                    {
                        final String message = i18n.getInstance(user).getText("admin.errors.projectversions.version.exists");
                        errors.addError("name", message, ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
        }
        else
        {
            final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
            errors.addErrorMessage(message, ErrorCollection.Reason.FORBIDDEN);
        }
        return errors;
    }

    @Override
    public ServiceOutcome<Version> setReleaseDate(User user, Version version, Date releaseDate)
    {
        if (hasEditPermission(user, version.getProjectObject()))
        {
            try
            {
                versionManager.editVersionReleaseDate(version, releaseDate);
                return ServiceOutcomeImpl.ok(version);
            }
            catch (GenericEntityException e)
            {
                final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
                return ServiceOutcomeImpl.error(message);
            }
        }
        else
        {
            final String message = i18n.getInstance(user).getText("admin.errors.projectversions.could.not.edit", version.getName());
            return ServiceOutcomeImpl.error(message);
        }
    }

    @Override
    public ServiceOutcome<Version> validateReleaseDate(User user, Version version, String releaseDate)
    {
        try
        {
            return setReleaseDate(user, version, parseDate(user, releaseDate));
        }
        catch (ReleaseDateParseException e)
        {
            return ServiceOutcomeImpl.from(e.parseErrors, version);
        }
    }

    @Override
    public ServiceOutcome<Version> setReleaseDate(User user, Version version, String releaseDate)
    {
        try
        {
            return setReleaseDate(user, version, parseDate(user, releaseDate));
        }
        catch (ReleaseDateParseException e)
        {
            return ServiceOutcomeImpl.from(e.parseErrors, version);
        }
    }

    public ValidationResult validateDelete(final JiraServiceContext context, final Long versionId, final VersionAction affectsAction, final VersionAction fixAction)
    {
        log.debug("Validating delete of version with id " + versionId);

        // Validate that we can find the version we are deleting
        final DeleteVersionValidator validator = new DeleteVersionValidator(context, versionManager, permissionManager);
        return validator.validate(versionId, affectsAction, fixAction);
    }

    public void delete(final JiraServiceContext context, final ValidationResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalArgumentException("Result from validation is invalid");
        }

        final Version version = result.getVersionToDelete();
        log.debug("Deleting version with id " + version.getId());

        // Now lets remove the version from all affected issues.
        final Collection<GenericValue> issues = getAllAssociatedIssues(version);

        if (!issues.isEmpty())
        {
            // swap the versions on the affected issues
            swapVersionsForIssues(context.getUser(), version, result.getAffectsSwapVersion(), result.getFixSwapVersion(), issues);

            // reindex all the affected issues
            try
            {
                issueIndexManager.reIndexIssues(issues);
            }
            catch (final IndexException e)
            {
                log.warn("Could not reindex issues after swapping versions", e);
            }
        }

        // delete the version from the system
        versionManager.deleteVersion(version);
        // Publish delete and merge events if necessary
        if (!issues.isEmpty() && result.getFixSwapVersion() != null)
        {
            eventPublisher.publish(new VersionMergeEvent(result.getFixSwapVersion().getId(), version.getId()));
        }
        eventPublisher.publish(new VersionDeleteEvent(version.getId()));
    }

    /**
     * Implementation is the same as deleting, with the actions set to SWAP and the swapVersionId being passed as both
     * Affects Version swap and Fix Version swap
     */
    public ValidationResult validateMerge(final JiraServiceContext context, final Long versionId, final Long swapVersionId)
    {
        final SwapVersionAction swapVersionAction = new SwapVersionAction(swapVersionId);
        return validateDelete(context, versionId, swapVersionAction, swapVersionAction);
    }

    public void merge(final JiraServiceContext context, final ValidationResult result)
    {
        delete(context, result);
    }

    @Override
    public VersionResult getVersionById(com.opensymphony.user.User user, Project project, Long versionId)
    {
        return getVersionById((User) user, project, versionId);
    }

    /**
     * Retrieves a Collection of {@link GenericValue} objects representing issues which are associated with the
     * specified {@link Version} object.
     *
     * @param version which {@link Version} to associate with.
     * @return a Collection of {@link GenericValue}s for issues.
     */
    Collection<GenericValue> getAllAssociatedIssues(final Version version)
    {
        try
        {
            final Collection<GenericValue> affectedIssues = new HashSet<GenericValue>();
            affectedIssues.addAll(getGvIssuesByAffectsVersion(version));
            affectedIssues.addAll(getGvIssuesByFixVersion(version));
            return affectedIssues;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error getting issues for version with id " + version.getId(), e);
        }
    }

    List<GenericValue> getGvIssuesByAffectsVersion(final Version version) throws GenericEntityException
    {
        return issueManager.getIssuesByEntity(IssueRelationConstants.VERSION, version.getGenericValue());
    }

    List<GenericValue> getGvIssuesByFixVersion(final Version version) throws GenericEntityException
    {
        return issueManager.getIssuesByEntity(IssueRelationConstants.FIX_VERSION, version.getGenericValue());
    }

    void swapVersionsForIssues(final User user, final Version version, final Version affectsSwapVersion, final Version fixSwapVersion, final Collection<GenericValue> issues)
    {
        for (final GenericValue gvIssue : issues)
        {
            final MutableIssue issue = issueFactory.getIssue(gvIssue);
            issue.setAffectedVersions(getNewVersions(version, issue.getAffectedVersions(), affectsSwapVersion));
            issue.setFixVersions(getNewVersions(version, issue.getFixVersions(), fixSwapVersion));

            // Use the backend issue update action to update the issue
            try
            {
                // issueGV is needed due to not-null validation done in AbstractIssueAction. TODO: remove this when we have a better IssueManager
                final Map<String, Object> actionParams = MapBuilder.newBuilder("issueObject", issue, "remoteUser",
                        user, "sendMail", Boolean.FALSE, "issue", issue.getGenericValue()).toMutableMap();
                executeIssueUpdate(actionParams);
            }
            catch (final Exception e)
            {
                throw new DataAccessException("Unable to swap versions for issue with key: " + issue.getKey(), e);
            }
        }
    }

    void executeIssueUpdate(final Map<String, Object> actionParams) throws Exception
    {
        final ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.ISSUE_UPDATE, actionParams);
        ActionUtils.checkForErrors(aResult);
    }

    /**
     * Gets a set of new versions.
     *
     * @param versionToRemove The version to remove.
     * @param versions The current {@link Version}s for an issue.
     * @param versionToSwap The version being swapped in.  May be null (in which case nothing gets swapped in).
     * @return A set of versions to save back to the issue.
     */
    private Collection<Version> getNewVersions(final Version versionToRemove, final Collection<Version> versions, final Version versionToSwap)
    {
        final Collection<Version> newVersions = new HashSet<Version>(versions);
        boolean oldVersionRemoved = false;
        boolean alreadyContainsNewVersion = false;
        for (final Iterator<Version> iterator = newVersions.iterator(); iterator.hasNext();)
        {
            final Version version = iterator.next();
            // Is this our old version?
            if (version.getId().equals(versionToRemove.getId()))
            {
                iterator.remove();
                oldVersionRemoved = true;
            }
            // Is this our new version?
            // JRA-17005 Don't rely on Version.equals(). If you merge multiple versions at once, the sequence can
            // change on a Version and the new Version object will not equal the old Version object.
            else if (versionToSwap != null && version.getId().equals(versionToSwap.getId()))
            {
                alreadyContainsNewVersion = true;
            }
        }

        // JRA-15887 only swap in the versionToSwap, if the versionToRemove was actually removed.
        if (oldVersionRemoved && versionToSwap != null && !alreadyContainsNewVersion)
        {
            newVersions.add(versionToSwap);
        }

        return newVersions;
    }

    public CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final String releaseDate, final String description, final Long scheduleAfterVersion)
    {
        ValidateResult result = validateCreateParameters(user, project, versionName, releaseDate);

        if (!result.isValid())
        {
            return new CreateVersionValidationResult(result.errors, result.reasons);
        }
        return new CreateVersionValidationResult(result.errors, project, versionName, result.parsedDate, description, scheduleAfterVersion);
    }

    public CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final Date releaseDate, final String description, final Long scheduleAfterVersion)
    {
        ValidateResult result = validateCreateParameters(user, project, versionName, null);
        if (!result.isValid())
        {
            return new CreateVersionValidationResult(result.errors, result.reasons);
        }
        return new CreateVersionValidationResult(result.errors, project, versionName,
                makeMidnight(releaseDate, getI18nBean(user).getLocale()), description, scheduleAfterVersion);
    }

    @Override
    public Version createVersion(com.opensymphony.user.User user, CreateVersionValidationResult request)
    {
        return createVersion((User) user, request);
    }

    public Version createVersion(User user, CreateVersionValidationResult request)
    {
        try
        {
            Version version = versionManager.createVersion(request.getVersionName(), request.getReleaseDate(), request.getDescription(), request.getProject().getId(), request.getScheduleAfterVersion());
            eventPublisher.publish(new VersionCreateEvent(version.getId()));
            return version;
        }
        catch (CreateException ex)
        {
            //the validateCreateVersion method should guarantee that this can never happen.
            throw new RuntimeException("createVersion failed", ex);
        }
    }

    @Override
    public ReleaseVersionValidationResult validateReleaseVersion(com.opensymphony.user.User user, Version version, Date releaseDate)
    {
        return validateReleaseVersion((User) user, version, releaseDate);
    }

    public ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final Date releaseDate)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }

        if (version.isReleased())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.release.already.released"));
        }

        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }
        return new ReleaseVersionValidationResult(errors, version, releaseDate);
    }

    public ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final String releaseDate)
    {
        try
        {
            return validateReleaseVersion(user, version, parseDate(user, releaseDate));
        }
        catch (ReleaseDateParseException e)
        {
            return new ReleaseVersionValidationResult(e.parseErrors);
        }
    }

    @Override
    public ReleaseVersionValidationResult validateUnreleaseVersion(com.opensymphony.user.User user, Version version, Date releaseDate)
    {
        return validateUnreleaseVersion((User) user, version, releaseDate);
    }

    public ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final Date releaseDate)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }

        if (!version.isReleased())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.release.not.released"));
        }

        if (errors.hasAnyErrors())
        {
            return new ReleaseVersionValidationResult(errors);
        }
        return new ReleaseVersionValidationResult(errors, version, releaseDate);
    }

    public ReleaseVersionValidationResult validateUnreleaseVersion(User user, Version version, String releaseDate)
    {
        try
        {
            return validateUnreleaseVersion(user, version, parseDate(user, releaseDate));
        }
        catch (ReleaseDateParseException e)
        {
            return new ReleaseVersionValidationResult(e.parseErrors);
        }
    }

    public ArchiveVersionValidationResult validateArchiveVersion(final User user, final Version version)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);
        checkVersionValid(errors, i18nBean, user, version);

        if (version.isArchived())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.archive.already.archived"));
        }

        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }
        return new ArchiveVersionValidationResult(errors, version);
    }

    @Override
    public ArchiveVersionValidationResult validateUnarchiveVersion(com.opensymphony.user.User user, Version version)
    {
        return validateUnarchiveVersion((User) user, version);
    }

    public ArchiveVersionValidationResult validateUnarchiveVersion(final User user, final Version version)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);
        checkVersionValid(errors, i18nBean, user, version);
        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }

        if (!version.isArchived())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.archive.not.archived"));
        }

        if (errors.hasAnyErrors())
        {
            return new ArchiveVersionValidationResult(errors);
        }
        return new ArchiveVersionValidationResult(errors, version);
    }

    private void checkVersionValid(ErrorCollection errors, I18nHelper i18nHelper, User user, Version version)
    {
        Assertions.notNull("version", version);

        final Project project = version.getProjectObject();
        if (project == null)
        {
            errors.addErrorMessage(i18nHelper.getText("admin.errors.must.specify.valid.project"));
            return;
        }

        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nHelper.getText("admin.errors.version.no.permission"));
            return;
        }

        if (StringUtils.isEmpty((version.getName())))
        {
            errors.addError("name", i18nHelper.getText("admin.errors.enter.valid.version.name"));
        }
    }

    public Version releaseVersion(final ReleaseVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not release a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not release a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setReleaseDate(result.getReleaseDate());
        version.setReleased(true);

        versionManager.releaseVersion(version, true);

        version = versionManager.getVersion(version.getId());
        eventPublisher.publish(new VersionReleaseEvent(version.getId()));
        return version;
    }

    public void moveUnreleasedToNewVersion(User user, Version currentVersion, Version newVersion)
    {
        final List<Issue> issues = getUnresolvedIssues(user, currentVersion);
        if (!issues.isEmpty())
        {
            for (final Issue issue : issues)
            {
                // Need to look this up from the DB since we have DocumentIssues from the search.
                final MutableIssue mutableIssue = issueManager.getIssueObject(issue.getId());
                final Collection<Version> versions = mutableIssue.getFixVersions();
                versions.remove(currentVersion);
                versions.add(newVersion);
                mutableIssue.setFixVersions(versions);
                try
                {
                    issueManager.updateIssue(user, mutableIssue, EventDispatchOption.ISSUE_UPDATED, true);
                }
                catch (UpdateException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    private List<Issue> getUnresolvedIssues(User user, Version toRelease)
    {
        try
        {
            Long pid = toRelease.getProjectObject().getId();
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(pid).and().unresolved();
            if (toRelease != null)
            {
                builder.and().fixVersion(toRelease.getId());
            }

            final SearchResults results = searchProvider.search(builder.buildQuery(), user, PagerFilter.getUnlimitedFilter());
            final List<Issue> issues = results.getIssues();
            return (issues == null) ? Collections.<Issue> emptyList() : issues;
        }
        catch (final Exception e)
        {
            log.error("Exception whilst getting unresolved issues " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }



    public Version unreleaseVersion(final ReleaseVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not unrelease a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not unrelease a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setReleaseDate(result.getReleaseDate());
        version.setReleased(false);

        versionManager.releaseVersion(version, false);

        version = versionManager.getVersion(version.getId());
        eventPublisher.publish(new VersionUnreleaseEvent(version.getId()));
        return version;
    }

    @Override
    public ArchiveVersionValidationResult validateArchiveVersion(com.opensymphony.user.User user, Version version)
    {
        return validateArchiveVersion((User) user, version);
    }


    public Version archiveVersion(ArchiveVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not archive a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not archive a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setArchived(true);
        versionManager.archiveVersion(version, true);

        version = versionManager.getVersion(version.getId());
        eventPublisher.publish(new VersionArchiveEvent(version.getId()));
        return version;
    }

    public Version unarchiveVersion(ArchiveVersionValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not unarchive a version with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not unarchive a version with an invalid validation result.");
        }

        Version version = result.getVersion();
        version.setArchived(false);
        versionManager.archiveVersion(version, false);

        version = versionManager.getVersion(version.getId());
        eventPublisher.publish(new VersionUnarchiveEvent(version.getId()));
        return version;
    }

    @Override
    public MoveVersionValidationResult validateMoveToStartVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateIncreaseVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateDecreaseVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateMoveToEndVersionSequence(final User user, long versionId)
    {
        return validateMove(user, versionId);
    }

    @Override
    public MoveVersionValidationResult validateMoveVersionAfter(final User user, long versionId, Long scheduleAfterVersionId)
    {
        MoveVersionValidationResult moveVersionValidationResult =  validateMove(user, versionId);

        if (!moveVersionValidationResult.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = new SimpleErrorCollection();
                    final I18nHelper i18nBean = getI18nBean(user);

            Version version = moveVersionValidationResult.getVersion();
            Version scheduleAfterVersion = versionManager.getVersion(scheduleAfterVersionId);
            if (scheduleAfterVersion == null || !scheduleAfterVersion.getProjectObject().equals(version.getProjectObject()))
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id.for.project", scheduleAfterVersionId.toString(), version.getProjectObject().getKey()));
                moveVersionValidationResult = new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.SCHEDULE_AFTER_VERSION_NOT_FOUND));
            }
            else
            {
                moveVersionValidationResult = new MoveVersionValidationResult(new SimpleErrorCollection(), version, scheduleAfterVersionId);
            }
        }
        return moveVersionValidationResult;
    }

    private MoveVersionValidationResult validateMove(User user, long versionId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.not.exist.with.id", String.valueOf(versionId)));
            return new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.NOT_FOUND));
        }

        Project project = version.getProjectObject();
        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new MoveVersionValidationResult(errors, EnumSet.of(MoveVersionValidationResult.Reason.FORBIDDEN));
        }

        return new MoveVersionValidationResult(errors, version);
    }

    @Override
    public void moveToStartVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        try
        {
            versionManager.moveToStartVersionSequence(moveVersionValidationResult.getVersion());
            eventPublisher.publish(new VersionMoveEvent(moveVersionValidationResult.getVersion().getId()));
        }
        catch (GenericEntityException ex)
        {
            //the validateMoveToStartVersionSequence method should guarantee that this can never happen.
            throw new RuntimeException("createVersion failed", ex);
        }
    }

    @Override
    public void increaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        try
        {
            versionManager.increaseVersionSequence(moveVersionValidationResult.getVersion());
            eventPublisher.publish(new VersionMoveEvent(moveVersionValidationResult.getVersion().getId()));
        }
        catch (GenericEntityException ex)
        {
            //the validateIncreaseVersionSequence method should guarantee that this can never happen.
            throw new RuntimeException("increaseVersionSequence failed", ex);
        }
    }

    @Override
    public void decreaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        try
        {
            versionManager.decreaseVersionSequence(moveVersionValidationResult.getVersion());
            eventPublisher.publish(new VersionMoveEvent(moveVersionValidationResult.getVersion().getId()));
        }
        catch (GenericEntityException ex)
        {
            //the validateDecreaseVersionSequence method should guarantee that this can never happen.
            throw new RuntimeException("decreaseVersionSequence failed", ex);
        }
    }

    @Override
    public void moveToEndVersionSequence(MoveVersionValidationResult moveVersionValidationResult)
    {
        try
        {
            versionManager.moveToEndVersionSequence(moveVersionValidationResult.getVersion());
            eventPublisher.publish(new VersionMoveEvent(moveVersionValidationResult.getVersion().getId()));
        }
        catch (GenericEntityException ex)
        {
            //the validateMoveToEndVersionSequence method should guarantee that this can never happen.
            throw new RuntimeException("moveToEndVersionSequence failed", ex);
        }
    }

    @Override
    public void moveVersionAfter(MoveVersionValidationResult moveVersionValidationResult)
    {
        versionManager.moveVersionAfter(moveVersionValidationResult.getVersion(), moveVersionValidationResult.getScheduleAfterVersion());
        eventPublisher.publish(new VersionMoveEvent(moveVersionValidationResult.getVersion().getId()));
    }

    @Override
    public boolean isOverdue(Version version)
    {
        return versionManager.isVersionOverDue(Assertions.notNull("version", version));
    }

    @Override
    public long getFixIssuesCount(Version version)
    {
        try
        {
            return versionManager.getFixIssues(version).size();
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getAffectsIssuesCount(Version version)
    {
        try
        {
            return versionManager.getAffectsIssues(version).size();
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getUnresolvedIssuesCount(final User user, final Version version)
    {
        return getUnresolvedIssues(user, version).size();
    }

    I18nHelper getI18nBean(User user)
    {
        return i18n.getInstance(user);
    }

    ValidateResult validateCreateParameters(User user, Project project, String versionName, String releaseDate)
    {
        I18nHelper i18nBean = getI18nBean(user);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        if (project == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.specify.valid.project"));
            return new ValidateResult(errors, EnumSet.of(CreateVersionValidationResult.Reason.BAD_PROJECT));
        }

        //check if the user is either a global admin or project admin for the selected project.
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user)
                && !permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.version.no.permission"));
            return new ValidateResult(errors, EnumSet.of(CreateVersionValidationResult.Reason.FORBIDDEN));
        }

        Set<CreateVersionValidationResult.Reason> reasons = EnumSet.noneOf(CreateVersionValidationResult.Reason.class);
        if (StringUtils.isEmpty((versionName)))
        {
            errors.addError("name", i18nBean.getText("admin.errors.enter.valid.version.name"));
            reasons.add(CreateVersionValidationResult.Reason.BAD_NAME);
        }
        else
        {
            Collection<Version> versions = versionManager.getVersions(project.getId());
            for (final Version version : versions)
            {
                if (versionName.equalsIgnoreCase(version.getName()))
                {
                    errors.addError("name", i18nBean.getText("admin.errors.version.already.exists"));
                    reasons.add(CreateVersionValidationResult.Reason.DUPLICATE_NAME);
                }
            }

            if (versionName.length() > 255)
            {
                errors.addError("name", i18nBean.getText("admin.errors.portalpages.description.too.long"));
                reasons.add(CreateVersionValidationResult.Reason.VERSION_NAME_TOO_LONG);
            }
        }

        Date date = null;
        try
        {
            date = parseDate(user, releaseDate);
        }
        catch (ReleaseDateParseException e)
        {
            errors.addErrorCollection(e.parseErrors);
            reasons.add(CreateVersionValidationResult.Reason.BAD_RELEASE_DATE);
        }

        return new ValidateResult(errors, reasons, date);
    }

    private static Date makeMidnight(Date date, Locale locale)
    {
        if (date == null)
        {
            return date;
        }
        Calendar calendar = Calendar.getInstance(locale);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Parses a string release date into a Date object, throwing an exception if there is a problem parsing the string.
     * If the release date is an empty string, this method returns null.
     *
     * @param user the user who has provided the date
     * @param releaseDate a string containing a release date
     * @return a Date object, or null
     * @throws com.atlassian.jira.bc.project.version.DefaultVersionService.ReleaseDateParseException if there is a
     * problem parsing the date string
     */
    @Nullable
    protected Date parseDate(User user, String releaseDate) throws ReleaseDateParseException
    {
        if (StringUtils.isEmpty(releaseDate))
        {
            return null;
        }

        try
        {
            return dateFieldFormat.parseDatePicker(releaseDate);
        }
        catch (IllegalArgumentException e)
        {
            I18nHelper i18n = getI18nBean(user);
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addError("releaseDate", i18n.getText("admin.errors.incorrect.date.format", dateFieldFormat.getFormatHint()));

            throw new ReleaseDateParseException(errors);
        }
    }

    static class ValidateResult
    {
        private final ErrorCollection errors;
        private final Set<CreateVersionValidationResult.Reason> reasons;
        private final Date parsedDate;

        ValidateResult(ErrorCollection errors, Set<CreateVersionValidationResult.Reason> reasons)
        {
            this(errors, reasons, null);
        }

        ValidateResult(ErrorCollection errors, Set<CreateVersionValidationResult.Reason> reasons, Date parsedDate)
        {
            if (!reasons.isEmpty() && !errors.hasAnyErrors())
            {
                throw new IllegalArgumentException("Cannot have reasons without error messages.");
            }
            this.errors = errors;
            this.reasons = reasons;
            this.parsedDate = parsedDate;
        }

        boolean isValid()
        {
            return !errors.hasAnyErrors();
        }

        ErrorCollection getErrors()
        {
            return errors;
        }

        Set<CreateVersionValidationResult.Reason> getReasons()
        {
            return reasons;
        }

        Date getParsedDate()
        {
            return parsedDate;
        }
    }

    /**
     * Thrown when a string cannot be parsed as a date.
     */
    static class ReleaseDateParseException extends Exception
    {
        /**
         * An internationalised error collection.
         */
        final ErrorCollection parseErrors;

        ReleaseDateParseException(ErrorCollection parseErrors)
        {
            this.parseErrors = parseErrors;
        }
    }
}
