package com.atlassian.jira.bc.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;

import java.util.List;

public interface ProjectService
{
    /**
     * The default name of HTML fields containing a Project's name. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_NAME = "projectName";

    /**
     * The default name of HTML fields containing a Project's key. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_KEY = "projectKey";

    /**
     * The default name of HTML fields containing a Project's lead. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_LEAD = "projectLead";

    /**
     * The default name of HTML fields containing a Project's URL. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_URL = "projectUrl";

    /**
     * The default name of HTML fields containing a Project's description. Validation methods on this service
     * (isValidAllProjectData) will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed
     * to this field name.
     */
    public static final String PROJECT_DESCRIPTION = "projectDescription";

    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error. The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * The default avatar will be used for the created project.
     * <p/>
     * Optional validation will be done for the url and assigneetype parameters.  The url needs to be a valid URL, and
     * the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD} or {@link
     * com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}.  UNASSIGNED will also only be valid, if unassigned issues
     * are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(com.opensymphony.user.User user, String name, String key, String description,
            String lead, String url, Long assigneeType);

    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error.  The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * The default avatar will be used for the created project.
     * <p/>
     * Optional validation will be done for the url and assigneetype parameters.  The url needs to be a valid URL, and
     * the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD} or {@link
     * com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}.  UNASSIGNED will also only be valid, if unassigned issues
     * are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(User user, String name, String key, String description,
            String lead, String url, Long assigneeType);

    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error. The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * Optional validation will be done for the url, assigneetype and avatarId parameters. The url needs to be a valid
     * URL and the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD} or
     * {@link com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}.  UNASSIGNED will also only be valid, if unassigned
     * issues are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an avatar.
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(com.opensymphony.user.User user, String name, String key, String description,
            String lead, String url, Long assigneeType, Long avatarId);

    /**
     * This method needs to be called before creating a project to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project name, key and lead. The validation will also check if a project
     * with the name or key provided already exists and throw an appropriate error. The project key will be validated
     * that it matches the allowed key pattern, and it is not a reserved word. A validation error will also be added if no
     * user exists for the lead username provided.
     * <p/>
     * Optional validation will be done for the url, assigneetype and avatarId parameters. The url needs to be a valid
     * URL and the assigneeType needs to be either {@link com.atlassian.jira.project.AssigneeTypes#PROJECT_LEAD} or
     * {@link com.atlassian.jira.project.AssigneeTypes#UNASSIGNED}.  UNASSIGNED will also only be valid, if unassigned
     * issues are enabled in the General Configuration.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult} which
     * contains an ErrorCollection with any potential errors and all the project's details.
     *
     * @param user The user trying to create a project
     * @param name The name of the new project
     * @param key The project key of the new project
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the new project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an avatar.
     * @return A validation result containing any errors and all project details
     */
    CreateProjectValidationResult validateCreateProject(User user, String name, String key, String description,
            String lead, String url, Long assigneeType, Long avatarId);

    /**
     * Using the validation result from {@link #validateCreateProject(User, String, String,
     * String, String, String, Long)} a new project will be created.  This method will throw an IllegalStateException if
     * the validation result contains any errors.
     * <p/>
     * Project creation involves creating the project itself and setting some defaults for workflow schemes and issue
     * type screen schemes.
     *
     * @param createProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return The new project
     * @throws IllegalStateException if the validation result contains any errors.
     */
    Project createProject(CreateProjectValidationResult createProjectValidationResult);

    /**
     * Validates that the given user is authorised to update a project. A project can be updated by any user with the
     * global admin permission or project admin permission for the project in question.
     *
     * @param user The user trying to update a project
     * @param key The project key of the project to update.
     * @return a ServiceResult, which will contain errors if the user is not authorised to update the project
     */
    ServiceResult validateUpdateProject(final User user, final String key);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(com.opensymphony.user.User user, String name, String key, String description,
            String lead, String url, Long assigneeType);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(User user, String name, String key, String description,
            String lead, String url, Long assigneeType);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(com.opensymphony.user.User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing avatar.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(com.opensymphony.user.User user, String name, String key, String description,
            String lead, String url, Long assigneeType, Long avatarId);

    /**
     * Validates updating a project's details.  The project is looked up by the key provided.  If no project with the
     * key provided can be found, an appropriate error will be added to the result.
     * <p/>
     * Validation performed will be the same as for the {@link #validateCreateProject(User,
     * String, String, String, String, String, Long)} method. The only difference is that the project key will obviously
     * not be validated.
     * <p/>
     * A project can be updated by any user with the global admin permission or project admin permission for the project
     * in question.
     *
     * @param user The user trying to update a project
     * @param name The name of the new project
     * @param key The project key of the project to update.
     * @param description An optional description for the project
     * @param lead The lead developer for the project
     * @param url An optional URL for the project
     * @param assigneeType The default assignee for issues created in this project.  May be either project lead, or
     * unassigned if unassigned issues are enabled.
     * @param avatarId the id of an existing avatar.
     * @return A validation result containing any errors and all project details
     */
    UpdateProjectValidationResult validateUpdateProject(User user, String name, String key, String description,
            String lead, String url, Long assigneeType, Long avatarId);

    /**
     * Using the validation result from {@link #validateUpdateProject(User, String, String,
     * String, String, String, Long)} this method performs the actual update on the project.
     *
     * @param updateProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return The updated project
     * @throws IllegalStateException if the validation result contains any errors.
     */
    Project updateProject(UpdateProjectValidationResult updateProjectValidationResult);

    /**
     * Validation to delete a project is quite straightforward.  The user must have global admin rights and the project
     * about to be deleted needs to exist.
     *
     * @param user The user trying to delete a project
     * @param key The key of the project to delete
     * @return A validation result containing any errors and all project details
     */
    DeleteProjectValidationResult validateDeleteProject(com.opensymphony.user.User user, String key);

    /**
     * Validation to delete a project is quite straightforward.  The user must have global admin rights and the project
     * about to be deleted needs to exist.
     *
     * @param user The user trying to delete a project
     * @param key The key of the project to delete
     * @return A validation result containing any errors and all project details
     */
    DeleteProjectValidationResult validateDeleteProject(User user, String key);

    /**
     * Deletes the project provided by the deleteProjectValidationResult.  There's a number of steps involved in
     * deleting a project, which are carried out in the following order:
     * <ul>
     * <li>Delete all the issues in the project</li>
     * <li>Remove any custom field associations for the project</li>
     * <li>Remove the IssueTypeScreenSchemeAssocation for the project</li>
     * <li>Remove any other associations of this project (to permission schemes, notification schemes...)</li>
     * <li>Remove any versions in this project</li>
     * <li>Remove any components in this project</li>
     * <li>Delete all portlets that rely on this project (either directly or via filters)</li>
     * <li>Delete all the filters for this project</li>
     * <li>Delete the project itself in the database</li>
     * <li>Flushing the issue, project and workflow scheme caches</li>
     * </ul>
     *
     * @param user The user trying to delete a project
     * @param deleteProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return A result containing any errors.  Users of this method should check the result.
     */
    DeleteProjectResult deleteProject(com.opensymphony.user.User user, DeleteProjectValidationResult deleteProjectValidationResult);

    /**
     * Deletes the project provided by the deleteProjectValidationResult.  There's a number of steps involved in
     * deleting a project, which are carried out in the following order:
     * <ul>
     * <li>Delete all the issues in the project</li>
     * <li>Remove any custom field associations for the project</li>
     * <li>Remove the IssueTypeScreenSchemeAssocation for the project</li>
     * <li>Remove any other associations of this project (to permission schemes, notification schemes...)</li>
     * <li>Remove any versions in this project</li>
     * <li>Remove any components in this project</li>
     * <li>Delete all portlets that rely on this project (either directly or via filters)</li>
     * <li>Delete all the filters for this project</li>
     * <li>Delete the project itself in the database</li>
     * <li>Flushing the issue, project and workflow scheme caches</li>
     * </ul>
     *
     * @param user The user trying to delete a project
     * @param deleteProjectValidationResult Result from the validation, which also contains all the project's details.
     * @return A result containing any errors.  Users of this method should check the result.
     */
    DeleteProjectResult deleteProject(User user, DeleteProjectValidationResult deleteProjectValidationResult);

    /**
     * If the scheme ids are not null or -1 (-1 is often used to reset schemes), then an attempt will be made to
     * retrieve the scheme.  If this attempt fails an error will be added.  IssueSecuritySchemes will only be validated
     * in enterprise edition.
     *
     * @param permissionSchemeId The permission scheme that the new project should use
     * @param notificationSchemeId The notification scheme that the new project should use. Optional.
     * @param issueSecuritySchemeId The issue security scheme that the new project should use. Optional.
     * @return A validation result containing any errors and all scheme ids
     */
    UpdateProjectSchemesValidationResult validateUpdateProjectSchemes(com.opensymphony.user.User user, final Long permissionSchemeId,
            final Long notificationSchemeId, final Long issueSecuritySchemeId);

    /**
     * If the scheme ids are not null or -1 (-1 is often used to reset schemes), then an attempt will be made to
     * retrieve the scheme.  If this attempt fails an error will be added.  IssueSecuritySchemes will only be validated
     * in enterprise edition.
     *
     * @param permissionSchemeId The permission scheme that the new project should use
     * @param notificationSchemeId The notification scheme that the new project should use. Optional.
     * @param issueSecuritySchemeId The issue security scheme that the new project should use. Optional.
     * @return A validation result containing any errors and all scheme ids
     */
    UpdateProjectSchemesValidationResult validateUpdateProjectSchemes(User user, final Long permissionSchemeId,
            final Long notificationSchemeId, final Long issueSecuritySchemeId);

    /**
     * Updates the project schemes for a particular project, given a validation result and project to update.
     *
     * @param result Result from the validation, which also contains all the schemes details.
     * @param project The project which will have its schemes updated.
     * @throws IllegalStateException if the validation result contains any errors.
     */
    void updateProjectSchemes(UpdateProjectSchemesValidationResult result, Project project);

        /**
     * Will validate all project fields setting the appropriate errors in the {@link
     * com.atlassian.jira.bc.JiraServiceContext} if any errors occur.
     *
     * @param serviceContext containing the errorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @NotNull
     * @param key the key of the project @NotNull
     * @param lead the project lead @NotNull
     * @param url the project URL (optional)
     * @param assigneeType the default assignee type (optional - only appears on some forms)
     * @return true if project data is valid, false otherwise
     */
    boolean isValidAllProjectData(JiraServiceContext serviceContext, String name, String key, String lead, String url, Long assigneeType);

    /**
     * Will validate all project fields setting the appropriate errors in the {@link
     * com.atlassian.jira.bc.JiraServiceContext} if any errors occur.
     *
     * @param serviceContext containing the errorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @NotNull
     * @param key the key of the project @NotNull
     * @param lead the project lead @NotNull
     * @param url the project URL (optional)
     * @param assigneeType the default assignee type (optional - only appears on some forms)
     * @param avatarId the id of the avatar (null indicates default avatar)
     * @return true if project data is valid, false otherwise
     */
    boolean isValidAllProjectData(JiraServiceContext serviceContext, String name, String key, String lead, String url, Long assigneeType, Long avatarId);

    /**
     * Will validate the fields required for creating a project and setting the appropriate validation errors in the
     * {@link com.atlassian.jira.bc.JiraServiceContext} if any errors occur.
     *
     * @param serviceContext containing the errorCollection that will be populated with any validation errors that are
     * encountered
     * @param name the name of the project @NotNull
     * @param key the key of the project @NotNull
     * @param lead the project lead @NotNull
     * @return true if project data is valid, false otherwise
     */
    boolean isValidRequiredProjectData(JiraServiceContext serviceContext, String name, String key, String lead);

    /**
     * Get the project key description from the properties file. If the user has specified a custom regex that project
     * keys must conform to and a description for that regex, this method should return the description.
     * <p/>
     * If the user has not specified a custom regex, this method will return the default project key description:
     * <p/>
     * "Usually the key is just 3 letters - i.e. if your project name is Foo Bar Raz, a key of FBR would make sense.<br>
     * The key must contain only uppercase alphabetic characters, and be at least 2 characters in length.<br> <i>It is
     * recommended to use only ASCII characters, as other characters may not work."
     *
     * @return a String description of the project key format
     */
    String getProjectKeyDescription();

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the id
     * specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectById(com.opensymphony.user.User user, Long id);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the id
     * specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectById(User user, Long id);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by id providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * id specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param id The id of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A ProjectResult object
     */
    GetProjectResult getProjectByIdForAction(User user, Long id, ProjectAction action);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKey(com.opensymphony.user.User user, String key);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key.  This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request does not have the BROWSE project permission for the
     * project. In both of these cases, the errorCollection in the result object will contain an appropriate error
     * message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKey(User user, String key);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.Project} object by key providing the user can perform the
     * passed action on the project. This method returns a {@link
     * com.atlassian.jira.bc.project.ProjectService.GetProjectResult}. The project will be null if no project for the
     * key specified can be found, or if the user making the request cannot perform the passed action on the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user retrieving the project.
     * @param key The key of the project.
     * @param action the action the user must be able to perform on the project.
     * @return A GetProjectResult object
     */
    GetProjectResult getProjectByKeyForAction(User user, String key, ProjectAction action);

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects. The list will be empty, if the user does not have
     * the BROWSE project permission for any project or no projects are visible when using anonymous access.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     *
     * @return A ServiceOutcome containing a list of projects
     * @since v4.3
     */
    ServiceOutcome<List<Project>> getAllProjects(User user);

    /**
     * Used to retrieve a list of {@link com.atlassian.jira.project.Project} objects. This method returns a
     * {@link com.atlassian.jira.bc.ServiceOutcome} containing a list of projects that the user can perform the passed
     * action on. The list will be empty if no projects match the passed action.
     *
     * @param user The user retrieving the list of projects or NULL when using anonymous access.
     * @param action the action the user must be able to perform on the returned projects.
     * @return A ServiceOutcome containing a list of projects the user can perform the passed action on.
     * @since v4.3
     */
    ServiceOutcome<List<Project>> getAllProjectsForAction(User user, ProjectAction action);

    public static class UpdateProjectSchemesValidationResult extends ServiceResultImpl
    {
        private Long permissionSchemeId;
        private Long notificationSchemeId;
        private Long issueSecuritySchemeId;

        public UpdateProjectSchemesValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public UpdateProjectSchemesValidationResult(ErrorCollection errorCollection, Long permissionSchemeId,
                Long notificationSchemeId, Long issueSecuritySchemeId)
        {
            super(errorCollection);
            this.permissionSchemeId = permissionSchemeId;
            this.notificationSchemeId = notificationSchemeId;
            this.issueSecuritySchemeId = issueSecuritySchemeId;
        }

        public Long getPermissionSchemeId()
        {
            return permissionSchemeId;
        }

        public Long getNotificationSchemeId()
        {
            return notificationSchemeId;
        }

        public Long getIssueSecuritySchemeId()
        {
            return issueSecuritySchemeId;
        }
    }

    public abstract static class AbstractProjectValidationResult extends ServiceResultImpl
    {
        private final String name;
        private final String key;
        private final String description;
        private final String lead;
        private final String url;
        private final Long assigneeType;
        private final Long avatarId;

        public AbstractProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            name = null;
            key = null;
            description = null;
            lead = null;
            url = null;
            assigneeType = null;
            avatarId = null;
        }

        public AbstractProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, final Long avatarId)
        {
            super(errorCollection);
            this.name = name;
            this.key = key;
            this.description = description;
            this.lead = lead;
            this.url = url;
            this.assigneeType = assigneeType;
            this.avatarId = avatarId;
        }

        public String getName()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }

        public String getDescription()
        {
            return description;
        }

        public String getLead()
        {
            return lead;
        }

        public String getUrl()
        {
            return url;
        }

        public Long getAssigneeType()
        {
            return assigneeType;
        }

        public Long getAvatarId()
        {
            return avatarId;
        }
    }

    public static class CreateProjectValidationResult extends AbstractProjectValidationResult
    {
        public CreateProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public CreateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId)
        {
            super(errorCollection, name, key, description, lead, url, assigneeType, avatarId);
        }
    }

    public static class UpdateProjectValidationResult extends AbstractProjectValidationResult
    {
        private final Project originalProject;

        public UpdateProjectValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.originalProject = null;
        }

        public UpdateProjectValidationResult(ErrorCollection errorCollection, String name, String key,
                String description, String lead, String url, Long assigneeType, Long avatarId, Project originalProject)
        {
            super(errorCollection, name, key, description, lead, url, assigneeType, avatarId);
            this.originalProject = originalProject;
        }

        public Project getOriginalProject()
        {
            return originalProject;
        }
    }

    public static abstract class AbstractProjectResult extends ServiceResultImpl
    {
        private Project project;

        public AbstractProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public AbstractProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection);
            this.project = project;
        }

        public Project getProject()
        {
            return project;
        }
    }

    public static class GetProjectResult extends AbstractProjectResult
    {
        public GetProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public GetProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection, project);
        }
    }

    public static class CreateProjectResult extends AbstractProjectResult
    {
        public CreateProjectResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public CreateProjectResult(ErrorCollection errorCollection, Project project)
        {
            super(errorCollection, project);
        }
    }


    public static class DeleteProjectValidationResult extends AbstractProjectResult
    {
        public DeleteProjectValidationResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public DeleteProjectValidationResult(final ErrorCollection errorCollection, final Project project)
        {
            super(errorCollection, project);
        }
    }

    public static class DeleteProjectResult extends ServiceResultImpl
    {
        public DeleteProjectResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
        }
    }
}
