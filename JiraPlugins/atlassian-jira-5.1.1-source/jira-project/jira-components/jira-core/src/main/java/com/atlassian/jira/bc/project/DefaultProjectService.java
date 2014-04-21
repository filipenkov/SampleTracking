package com.atlassian.jira.bc.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.List;

import static com.atlassian.jira.util.ErrorCollection.Reason;

public class DefaultProjectService implements ProjectService
{
    /**
     * Maximum length for the project key and URL fields.
     */
    private static final int MAX_FIELD_LENGTH = 255;

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectManager projectManager;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SchemeFactory schemeFactory;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final CustomFieldManager customFieldManager;
    private final NodeAssociationStore nodeAssociationStore;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final SharePermissionDeleteUtils sharePermissionDeleteUtils;
    private final AvatarManager avatarManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final WorkflowManager workflowManager;


    public DefaultProjectService(JiraAuthenticationContext jiraAuthenticationContext, ProjectManager projectManager,
            ApplicationProperties applicationProperties, PermissionManager permissionManager,
            PermissionSchemeManager permissionSchemeManager,
            NotificationSchemeManager notificationSchemeManager, IssueSecuritySchemeManager issueSecuritySchemeManager,
            SchemeFactory schemeFactory, WorkflowSchemeManager workflowSchemeManager,
            IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, CustomFieldManager customFieldManager,
            NodeAssociationStore nodeAssociationStore, VersionManager versionManager, ProjectComponentManager projectComponentManager,
            SharePermissionDeleteUtils sharePermissionDeleteUtils, AvatarManager avatarManager, I18nHelper.BeanFactory i18nFactory, WorkflowManager workflowManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.schemeFactory = schemeFactory;
        this.workflowSchemeManager = workflowSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.customFieldManager = customFieldManager;
        this.nodeAssociationStore = nodeAssociationStore;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.sharePermissionDeleteUtils = sharePermissionDeleteUtils;
        this.avatarManager = avatarManager;
        this.i18nFactory = i18nFactory;
        this.workflowManager = workflowManager;
    }

    @Override
    public CreateProjectValidationResult validateCreateProject(final User user, final String name, final String key,
            final String description, final String lead, final String url, final Long assigneeType)
    {
        return validateCreateProject(user, name, key, description, lead, url, assigneeType, null);
    }

    @Override
    public CreateProjectValidationResult validateCreateProject(final User user, final String name, final String key, final String description, final String lead, final String url, final Long assigneeType, final Long avatarId)
    {
        final JiraServiceContext serviceContext = getServiceContext(user, new SimpleErrorCollection());
        final I18nHelper i18nBean = getI18nBean(user);

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            serviceContext.getErrorCollection()
                    .addErrorMessage(i18nBean.getText("admin.projects.service.error.no.admin.permission"));
            return new CreateProjectValidationResult(serviceContext.getErrorCollection());
        }

        isValidAllProjectData(serviceContext, name, key, lead, url, assigneeType, avatarId);

        if (serviceContext.getErrorCollection().hasAnyErrors())
        {
            return new CreateProjectValidationResult(serviceContext.getErrorCollection());
        }

        return new CreateProjectValidationResult(serviceContext.getErrorCollection(), name, key, description, lead,
                url, assigneeType, avatarId);
    }

    protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
    {
        return new JiraServiceContextImpl(user, errorCollection);
    }

    public Project createProject(CreateProjectValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not create a project with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not create a project with an invalid validation result.");
        }

        //create the project
        final Project newProject = projectManager.createProject(result.getName(), result.getKey().toUpperCase(),
                result.getDescription(), result.getLead(), result.getUrl(), result.getAssigneeType(), result.getAvatarId());

        // Associate the project with the default issue type screen scheme
        issueTypeScreenSchemeManager.associateWithDefaultScheme(newProject);

        // Refresh the workflow cache
        workflowSchemeManager.clearWorkflowCache();

        return newProject;
    }

    @Override
    public UpdateProjectValidationResult validateUpdateProject(final User user, final String name, final String key, final String description, final String lead, final String url, final Long assigneeType)
    {
        return validateUpdateProject(user, name, key, description, lead, url, assigneeType, null);
    }

    @Override
    public ServiceResult validateUpdateProject(final User user, final String key)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        //check if the project exists.  If not return with an error.
        final GetProjectResult oldProjectResult = getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG);
        if (!oldProjectResult.isValid() || oldProjectResult.getProject() == null)
        {
            errorCollection.addErrorCollection(oldProjectResult.getErrorCollection());
            return new ServiceResultImpl(errorCollection);
        }

        return new ServiceResultImpl(errorCollection);
    }

    @Override
    public UpdateProjectValidationResult validateUpdateProject(final User user, final String name, final String key,
            final String description, final String lead, final String url, final Long assigneeType, Long avatarId)
    {
        ServiceResult validateUserAndKey = validateUpdateProject(user, key);
        if (!validateUserAndKey.isValid())
        {
            return new UpdateProjectValidationResult(validateUserAndKey.getErrorCollection());
        }

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final GetProjectResult oldProjectResult = getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG);
        final ErrorCollection validationErrors = validateUpdateProjectData(user, name, oldProjectResult.getProject(),
                lead, url, assigneeType, avatarId);

        if (validationErrors.hasAnyErrors())
        {
            return new UpdateProjectValidationResult(validationErrors);
        }

        return new UpdateProjectValidationResult(errorCollection, name, key, description, lead, url, assigneeType,
                avatarId, oldProjectResult.getProject());
    }

    @Override
    public Project updateProject(final UpdateProjectValidationResult result)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not update a project with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not update a project with an invalid validation result.");
        }

        return projectManager.updateProject(result.getOriginalProject(), result.getName(), result.getDescription(),
                result.getLead(), result.getUrl(), result.getAssigneeType(), result.getAvatarId());
    }

    @Override
    public UpdateProjectSchemesValidationResult validateUpdateProjectSchemes(final User user, final Long permissionSchemeId,
            final Long notificationSchemeId, final Long issueSecuritySchemeId)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.projects.service.error.no.admin.permission"));
            return new UpdateProjectSchemesValidationResult(errorCollection);
        }

        if (permissionSchemeId != null && !new Long(-1).equals(permissionSchemeId))
        {
            try
            {
                GenericValue scheme = permissionSchemeManager.getScheme(permissionSchemeId);
                if (scheme == null)
                {
                    errorCollection.addErrorMessage(
                            i18nBean.getText("admin.errors.project.validation.permission.scheme.not.retrieved"));
                }
            }
            catch (GenericEntityException e)
            {
                errorCollection.addErrorMessage(i18nBean.getText(
                        "admin.errors.project.validation.permission.scheme.not.retrieved.error", e.getMessage()));
            }
        }

        if (notificationSchemeId != null && !new Long(-1).equals(notificationSchemeId))
        {
            try
            {
                GenericValue scheme = notificationSchemeManager.getScheme(notificationSchemeId);
                if (scheme == null)
                {
                    errorCollection.addErrorMessage(
                            i18nBean.getText("admin.errors.project.validation.notification.scheme.not.retrieved"));
                }
            }
            catch (GenericEntityException e)
            {
                errorCollection.addErrorMessage(i18nBean.getText(
                        "admin.errors.project.validation.notification.scheme.not.retrieved.error", e.getMessage()));
            }
        }

        if (issueSecuritySchemeId != null && !new Long(-1).equals(issueSecuritySchemeId))
        {
            try
            {
                GenericValue scheme = issueSecuritySchemeManager.getScheme(issueSecuritySchemeId);
                if (scheme == null)
                {
                    errorCollection.addErrorMessage(
                            i18nBean.getText("admin.errors.project.validation.issuesecurity.scheme.not.retrieved"));
                }
            }
            catch (GenericEntityException e)
            {
                errorCollection.addErrorMessage(i18nBean.getText(
                        "admin.errors.project.validation.issuesecurity.scheme.not.retrieved.error", e.getMessage()));
            }
        }

        if (errorCollection.hasAnyErrors())
        {
            return new UpdateProjectSchemesValidationResult(errorCollection);
        }
        return new UpdateProjectSchemesValidationResult(errorCollection, permissionSchemeId, notificationSchemeId,
                issueSecuritySchemeId);
    }

    @Override
    public void updateProjectSchemes(final UpdateProjectSchemesValidationResult result, Project project)
    {
        if (result == null)
        {
            throw new IllegalArgumentException("You can not update project schemes with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not update project schemes with an invalid validation result.");
        }

        if (project == null)
        {
            throw new IllegalArgumentException("You can not update project schemes for a null project.");
        }

        //now add all the schemes to the project.
        final Long permissionSchemeId = result.getPermissionSchemeId();
        final Long notificationSchemeId = result.getNotificationSchemeId();
        final Long issueSecuritySchemeId = result.getIssueSecuritySchemeId();
        try
        {
            notificationSchemeManager.removeSchemesFromProject(project);
            if (notificationSchemeId != null && !new Long(-1).equals(notificationSchemeId))
            {
                GenericValue scheme = notificationSchemeManager.getScheme(notificationSchemeId);
                notificationSchemeManager.addSchemeToProject(project, schemeFactory.getScheme(scheme));
            }

            if (permissionSchemeId != null && !new Long(-1).equals(permissionSchemeId))
            {
                //A project should always be linked to a permissionscheme.
                permissionSchemeManager.removeSchemesFromProject(project);
                GenericValue scheme = permissionSchemeManager.getScheme(permissionSchemeId);
                permissionSchemeManager.addSchemeToProject(project, schemeFactory.getScheme(scheme));
            }

            issueSecuritySchemeManager.removeSchemesFromProject(project);
            if (issueSecuritySchemeId != null && !new Long(-1).equals(issueSecuritySchemeId))
            {
                GenericValue scheme = issueSecuritySchemeManager.getScheme(issueSecuritySchemeId);
                issueSecuritySchemeManager.addSchemeToProject(project, schemeFactory.getScheme(scheme));
            }

        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean isValidAllProjectData(final JiraServiceContext serviceContext, final String name, final String key, final String lead, final String url, final Long assigneeType)
    {
        return isValidAllProjectData(serviceContext, name, key, lead, url, assigneeType, null);
    }

    @Override
    public boolean isValidAllProjectData(JiraServiceContext serviceContext, String name, String key, String lead, String url,
            Long assigneeType, Long avatarId)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nHelper = serviceContext.getI18nBean();

        isValidRequiredProjectData(getServiceContext(serviceContext.getLoggedInUser(), errorCollection), name, key, lead);
        validateProjectUrl(url, errorCollection, i18nHelper);
        validateProjectAssigneeType(assigneeType, errorCollection, i18nHelper);
        validateAvatarId(avatarId, projectManager.getProjectObjByKey(key), errorCollection, i18nHelper);

        if (errorCollection.hasAnyErrors())
        {
            serviceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = serviceContext.getI18nBean();

        validateProjectName(name, errorCollection, i18nBean);
        validateProjectKey(key, errorCollection, i18nBean);
        validateProjectLead(lead, errorCollection, i18nBean);

        if (errorCollection.hasAnyErrors())
        {
            serviceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidProjectKey(JiraServiceContext serviceContext, String key)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = serviceContext.getI18nBean();

        validateProjectKey(key, errorCollection, i18nBean);

        if (errorCollection.hasAnyErrors())
        {
            serviceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return true;
    }

    /*
     * Does the same as isValidAllProjectData except that the project key doesn't need to be validated.
     */
    private ErrorCollection validateUpdateProjectData(User user, String name, Project oldProject, String lead, String url, Long assigneeType, Long avatarId)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nHelper = getI18nBean(user);

        validateProjectNameForUpdate(name, oldProject.getKey(), errorCollection, i18nHelper);
        validateProjectLead(lead, errorCollection, i18nHelper);
        validateProjectUrl(url, errorCollection, i18nHelper);
        validateProjectAssigneeType(assigneeType, errorCollection, i18nHelper);
        validateAvatarId(avatarId, oldProject, errorCollection, i18nHelper);

        return errorCollection;
    }


    @Override
    public String getProjectKeyDescription()
    {
        String projectKeyDescription = applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_DESCRIPTION);
        final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
        if (TextUtils.stringSet(projectKeyDescription))
        {
            return i18nBean.getText(projectKeyDescription);
        }
        else
        {
            return i18nBean.getText("admin.projects.key.description");
        }
    }

    @Override
    public GetProjectResult getProjectByIdForAction(User user, Long id, ProjectAction action)
    {
        Project project = projectManager.getProjectObj(id);
        if (project == null || !checkActionPermission(user, project, ProjectAction.VIEW_PROJECT))
        {
            return new ProjectNotFoundResult(user, "admin.errors.project.not.found.for.id", String.valueOf(id));
        }

        if (!checkActionPermission(user, project, action))
        {
            return new PermissionErrorResult(user, action);
        }

        return new GetProjectResult(project);
    }

    @Override
    public GetProjectResult getProjectById(User user, Long id)
    {
        return  getProjectByIdForAction(user, id, ProjectAction.VIEW_ISSUES);
    }

    @Override
    public GetProjectResult getProjectByKey(User user, String key)
    {
        return getProjectByKeyForAction(user, key, ProjectAction.VIEW_ISSUES);
    }

    @Override
    public GetProjectResult getProjectByKeyForAction(User user, String key, ProjectAction action)
    {
        Assertions.notNull("action", action);

        Project project = projectManager.getProjectObjByKey(key);
        if (project == null || !checkActionPermission(user, project, ProjectAction.VIEW_PROJECT))
        {
            return new ProjectNotFoundResult(user, "admin.errors.project.not.found.for.key", key);
        }

        if (!checkActionPermission(user, project, action))
        {
            return new PermissionErrorResult(user, action);
        }

        return new GetProjectResult(project);
    }

    @Override
    public ServiceOutcome<List<Project>> getAllProjects(final User user)
    {
        return getAllProjectsForAction(user, ProjectAction.VIEW_ISSUES);
    }

    @Override
    public ServiceOutcome<List<Project>> getAllProjectsForAction(final User user, final ProjectAction action)
    {
        final Iterable<Project> projects = Iterables.filter(projectManager.getProjectObjects(), new Predicate<Project>()
        {
            @Override
            public boolean apply(@Nullable Project input)
            {
                return (input != null) && checkActionPermission(user, input, action);
            }
        });
        return new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Lists.newArrayList(projects));

    }

    @Override
    public DeleteProjectValidationResult validateDeleteProject(final User user, final String key)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = getI18nBean(user);
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.projects.service.error.no.admin.permission"));
            return new DeleteProjectValidationResult(errorCollection);
        }

        //check if the project exists.  If not return with an error.
        final GetProjectResult oldProjectResult = getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG);
        if (!oldProjectResult.isValid() || oldProjectResult.getProject() == null)
        {
            errorCollection.addErrorCollection(oldProjectResult.getErrorCollection());
            return new DeleteProjectValidationResult(errorCollection);
        }

        return new DeleteProjectValidationResult(errorCollection, oldProjectResult.getProject());
    }

    @Override
    public DeleteProjectResult deleteProject(final User user, final DeleteProjectValidationResult result)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext serviceContext = getServiceContext(user, errorCollection);
        final I18nHelper i18nBean = getI18nBean(user);

        if (result == null)
        {
            throw new IllegalArgumentException("You can not delete a project with a null validation result.");
        }

        if (!result.isValid())
        {
            throw new IllegalStateException("You can not delete a project with an invalid validation result.");
        }

        final Project project = result.getProject();
        try
        {
            projectManager.removeProjectIssues(project);
        }
        catch (RemoveException e)
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.errors.project.exception.removing", e.getMessage()));
            return new DeleteProjectResult(errorCollection);
        }

        // Remove all context associations with the project
        customFieldManager.removeProjectAssociations(project);

        // Remove issue type field screen scheme association. This needs tobe done separately to the code below, as extra steps need ot be taken
        // when an issue type field screen scheme is deassociated with a project. For example, remove the actual issue type field screen scheme in the
        // if the scheme is not associated with other projects.
        final IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project.getGenericValue());
        issueTypeScreenSchemeManager.removeSchemeAssociation(project.getGenericValue(), issueTypeScreenScheme);

        final Iterable<JiraWorkflow> workflowsAssociatedToProject =
                workflowManager.getWorkflowsFromScheme(workflowSchemeManager.getSchemeFor(project));

        // removing all associations with this project
        try
        {
            nodeAssociationStore.removeAssociationsFromSource(project.getGenericValue());

            // remove versions
            final Iterable<Version> allVersions = versionManager.getVersions(project.getId());
            for (final Version version : allVersions)
            {
                versionManager.deleteVersion(version);
            }

            // Remove components through ProjectComponentManager
            final Iterable<ProjectComponent> components = projectComponentManager.findAllForProject(project.getId());
            for (final ProjectComponent component : components)
            {
                projectComponentManager.delete(component.getId());
            }
        }
        catch (DataAccessException e)
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.errors.project.exception.removing", e.getMessage()));
            return new DeleteProjectResult(errorCollection);
        }
        catch (EntityNotFoundException e)
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.errors.project.exception.removing", e.getMessage()));
            return new DeleteProjectResult(errorCollection);
        }

        sharePermissionDeleteUtils.deleteProjectSharePermissions(project.getId());

        projectManager.removeProject(project);
        projectManager.refresh();

        // JRA-8032 - clear the active workflow name cache
        workflowSchemeManager.clearWorkflowCache();
        workflowManager.copyAndDeleteDraftsForInactiveWorkflowsIn(user, workflowsAssociatedToProject);

        return new DeleteProjectResult(errorCollection);
    }

    /*
     * Default Assignee field is not on all forms, only validate if present
     */
    private void validateProjectAssigneeType(final Long assigneeType, final ErrorCollection errorCollection,
            final I18nHelper i18nHelper)
    {
        if (assigneeType != null && !ProjectAssigneeTypes.isValidType(assigneeType))
        {
            errorCollection.addErrorMessage(i18nHelper.getText("admin.errors.invalid.default.assignee"));
        }
    }

    /**
     * Checks that the avatar id is for an existing suitable avatar.
     *
     * @param avatarId null or the id of an existing avatar
     * @param oldProject the project before the update, null if this is a create.
     * @param errorCollection the errorCollection
     * @param i18nHelper the I18nHelper
     */
    private void validateAvatarId(final Long avatarId, final Project oldProject, final ErrorCollection errorCollection,
            final I18nHelper i18nHelper)
    {
        if (avatarId == null)
        {
            return;
        }
        final Avatar avatar = avatarManager.getById(avatarId);
        if (avatar != null)
        {
            if (!avatar.isSystemAvatar() && !oldProject.getId().toString().equals(avatar.getOwner()))
            {
                errorCollection.addErrorMessage(i18nHelper.getText("admin.errors.invalid.avatar"));
            }
        }
    }

    /*
     * URL is optional, only validate if present
     */
    private void validateProjectUrl(final String url, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        if (TextUtils.stringSet(url))
        {
            if(url.length() > MAX_FIELD_LENGTH)
            {
                errorCollection.addError(PROJECT_URL, i18nBean.getText("admin.errors.project.url.too.long"));
            }
            else if(!TextUtils.verifyUrl(url))
            {
                errorCollection.addError(PROJECT_URL, i18nBean.getText("admin.errors.url.specified.is.not.valid"));
            }
        }
    }

    /*
     * check key is set, is valid, is unique and not an OS reserved word
     */
    private void validateProjectKey(final String key, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        if (!isProjectKeyValid(key))
        {
            String projectKeyWarning = applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING);
            if (TextUtils.stringSet(projectKeyWarning))
            {
                errorCollection.addError(PROJECT_KEY, i18nBean.getText(projectKeyWarning));
            }
            else
            {
                errorCollection.addError(PROJECT_KEY, i18nBean.getText("admin.errors.must.specify.unique.project.key"));
            }
        }
        else if (isReservedKeyword(key))
        {
            errorCollection.addError(PROJECT_KEY, i18nBean.getText("admin.errors.project.keyword.invalid"));
        }
        else
        {
            if(key.length() > MAX_KEY_LENGTH)
            {
                errorCollection.addError(PROJECT_KEY, i18nBean.getText("admin.errors.project.key.too.long", MAX_KEY_LENGTH));
            }
            else
            {
                Project project = projectManager.getProjectObjByKey(key);

                if (project != null)
                {
                    errorCollection.addError(PROJECT_KEY, i18nBean.getText("admin.errors.project.with.that.key.already.exists"));
                }
            }
        }
    }

    /*
     * check project lead is set and exists in the system
     */
    private void validateProjectLead(final String lead, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        if (!TextUtils.stringSet(lead))
        {
            errorCollection.addError(PROJECT_LEAD, i18nBean.getText("admin.errors.must.specify.project.lead"));
        }
        else
        {
            if (!checkUserExists(lead))
            {
                errorCollection.addError(PROJECT_LEAD, i18nBean.getText("admin.errors.not.a.valid.user"));
            }
        }
    }

    /*
     * check project name is set and is unique
     */
    private void validateProjectName(final String name, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        if (!TextUtils.stringSet(name))
        {
            errorCollection.addError(PROJECT_NAME, i18nBean.getText("admin.errors.must.specify.a.valid.project.name"));
        }
        else
        {
            if(name.length() > MAX_NAME_LENGTH)
            {
                errorCollection.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.name.too.long", MAX_NAME_LENGTH));
            }
            else
            {
                Project project = projectManager.getProjectObjByName(name);

                if (project != null)
                {
                    errorCollection.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.with.that.name.already.exists"));
                }
            }
        }
    }

    /*
     * check project name is set and is unique or belongs to the same project
     */
    private void validateProjectNameForUpdate(final String name, final String key, final ErrorCollection errorCollection,
            final I18nHelper i18nBean)
    {
        if (!TextUtils.stringSet(name))
        {
            errorCollection.addError(PROJECT_NAME, i18nBean.getText("admin.errors.must.specify.a.valid.project.name"));
        }
        else
        {
            if(name.length() > MAX_NAME_LENGTH)
            {
                errorCollection.addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.name.too.long", MAX_NAME_LENGTH));
            }
            else
            {
                Project project = projectManager.getProjectObjByName(name);

                if (project != null)
                {
                    if (!key.equals(project.getKey()))
                    {
                        errorCollection
                                .addError(PROJECT_NAME, i18nBean.getText("admin.errors.project.with.that.name.already.exists"));
                    }
                }
            }
        }
    }

    final protected I18nHelper getI18nBean(User user)
    {
        return i18nFactory.getInstance(user);
    }

    boolean checkUserExists(final String lead)
    {
        return UserUtils.userExists(lead);
    }

    boolean isReservedKeyword(final String key)
    {
        return JiraKeyUtils.isReservedKeyword(key);
    }

    boolean isProjectKeyValid(final String key)
    {
        return JiraKeyUtils.validProjectKey(key);
    }

    boolean checkActionPermission(User user, Project project, ProjectAction action)
    {
        return action.hasPermission(permissionManager, user, project);
    }

    /**
     * A GetProjectResult that indicates a project was not found.
     */
    protected class ProjectNotFoundResult extends GetProjectResult
    {
        protected ProjectNotFoundResult(User user, String i18nKey, String... params)
        {
            super(makeErrorCollection(user, i18nKey, Reason.NOT_FOUND, params));
        }
    }

    /**
     * A GetProjectResult that indicates the calling user does not have permission to perform an action on a project.
     */
    protected class PermissionErrorResult extends GetProjectResult
    {
        protected PermissionErrorResult(User user, ProjectAction action)
        {
            super(makeErrorCollection(user, action.getErrorKey(), Reason.FORBIDDEN));
        }
    }

    /**
     * Creates a new ErrorCollection instance for a single error message. If the reason "forbidden" and the user is
     * null, this method adds an additional error message along the lines of "You are not logged in, please log in".
     *
     * @param user the User for whom the message will be i18n'ed
     * @param i18nKey a String containing an i18n key
     * @param reason a Reason code
     * @param params a String array containing the i18n params  @return a new ErrorCollection
     * @return a new ErrorCollection
     */
    protected final ErrorCollection makeErrorCollection(User user, String i18nKey, ErrorCollection.Reason reason, String... params)
    {
        SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage(getI18nBean(user).getText(i18nKey, params), reason);

        // lack of permissions may be due to the user not being logged in
        if (reason == Reason.FORBIDDEN && user == null)
        {
            errors.addErrorMessage(getI18nBean(user).getText("common.forms.ajax.unauthorised.alert"), Reason.NOT_LOGGED_IN);
        }

        return errors;
    }
}
