package com.atlassian.jira.bc.project.version;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionKeys;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Service for {@link com.atlassian.jira.bc.project.version.VersionService}
 *
 * @since v3.13
 */
public interface VersionService
{

    /**
     * Indicates that the {@link com.atlassian.jira.project.version.Version} should be removed from affected issues.
     */
    public VersionAction REMOVE = new RemoveVersionAction();

    /**
     * Validate the name and description of a version, if you have edit permission.
     * @param user the user who is performing the edit operation
     * @param version the version that they want to edit
     * @param name the new name for the version (must not be null or already in use)
     * @param description the new description for the version
     * @return an ErrorCollection that contains the success or failure of the update
     * @throws IllegalArgumentException if the name is null or duplicates an existing name
     */
    ErrorCollection validateVersionDetails(final User user, final Version version, final String name, final String description);

    /**
     * Set the name and description of a version, if you have edit permission.
     * @param user the user who is performing the edit operation
     * @param version the version that they want to edit
     * @param name the new name for the version (must not be null or already in use)
     * @param description the new description for the version
     * @return a ServiceOutcome that contains the success or failure of the update
     * @throws IllegalArgumentException if the name is null or duplicates an existing name
     */
    ServiceOutcome<Version> setVersionDetails(final User user, final Version version, final String name, final String description);

    /**
     * Modify the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful
     */
    ServiceOutcome<Version> setReleaseDate(final User user, final Version version, final Date releaseDate);

    /**
     * Validate the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful
     */
    ServiceOutcome<Version> validateReleaseDate(final User user, final Version version, final String releaseDate);

    /**
     * Modify the release date of a version without performing a release/unrelease.
     * @param user the user who is changing the release date
     * @param version the version they want to modify
     * @param releaseDate the new release date to use
     * @return a ServiceOutcome describing the success/failure of the edit, along with the new Version if successful
     */
    ServiceOutcome<Version> setReleaseDate(final User user, final Version version, final String releaseDate);

    /**
     * Validates an attempt to delete a version from a project. When deleting a version, we need to decide what to do
     * with issues that reference the version in their Affects of Fix Version fields. The action taken is specified as a
     * flag for each field.
     *
     * @param context The context for this service call.
     * @param versionId The id of the version to be deleted.
     * @param affectsAction Used to decide whether to move all the issues to a different 'affects' version or just
     * remove them. See {@link com.atlassian.jira.project.version.VersionKeys#REMOVE_ACTION}, {@link
     * com.atlassian.jira.project.version.VersionKeys#SWAP_ACTION}
     * @param fixAction Used to decide wether to move all the issues to a different 'fix' version or just remove them.
     * See {@link com.atlassian.jira.project.version.VersionKeys#REMOVE_ACTION}, {@link
     * com.atlassian.jira.project.version.VersionKeys#SWAP_ACTION}
     * @return a {@link ValidationResult} object which contains the version to delete, and the versions to swap to for
     *         Affects and Fix versions, or null if the action to be taken is {@link VersionKeys#REMOVE_ACTION}
     */
    ValidationResult validateDelete(JiraServiceContext context, Long versionId, VersionAction affectsAction, VersionAction fixAction);

    /**
     * Deletes a version from a project. When deleting a version, we need to decide what to do with issues that
     * reference the version in their Affects of Fix Version fields. The action taken is specified as a flag for each
     * field.
     *
     * @param context The context for this service call.
     * @param result The result of validation, which contains the version to be deleted, and the swap versions for
     * Affects and Fix fields
     */
    void delete(JiraServiceContext context, ValidationResult result);

    /**
     * Validates an attempt to merge a version into another. Merging is essentially the same as Deleting with the
     * actions set to {@link VersionKeys#SWAP_ACTION}.
     *
     * @param context The context for this service call.
     * @param versionId The original version to be merged and removed.
     * @param swapVersionId The target version of the merge operation. Must be from the same project.
     * @return a {@link ValidationResult} object which contains the version to delete, and the versions to swap to for
     *         Affects and Fix versions, or null if the action to be taken is {@link VersionKeys#REMOVE_ACTION}
     */
    ValidationResult validateMerge(JiraServiceContext context, Long versionId, Long swapVersionId);

    /**
     * Merges a version into another, then removes the original version.
     *
     * @param context The context for this service call.
     * @param result The result of validation, which contains the version to be deleted, and the swap versions for
     * Affects and Fix fields
     */
    void merge(JiraServiceContext context, ValidationResult result);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionId The id of requested version
     * @return VersionResult object
     */
    VersionResult getVersionById(final com.opensymphony.user.User user, final Project project, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionId The id of requested version
     * @return VersionResult object
     */
    VersionResult getVersionById(final User user, final Project project, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     * @since 4.2
     *
     * @param user The user trying to get a version
     * @param versionId The id of requested version
     * @return VersionResult object
     */
    VersionResult getVersionById(final com.opensymphony.user.User user, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version id.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the id specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     * @since 4.2
     *
     * @param user The user trying to get a version
     * @param versionId The id of requested version
     * @return VersionResult object
     */
    VersionResult getVersionById(final User user, final Long versionId);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version name within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the versionName specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionName The name of requested version
     * @return VerionResult object
     */
    VersionResult getVersionByProjectAndName(final com.opensymphony.user.User user, final Project project, final String versionName);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} object by version name within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionResult}.
     * The version will be null if no version for the versionName specified can be found, or if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In both of these cases, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @param versionName The name of requested version
     * @return VerionResult object
     */
    VersionResult getVersionByProjectAndName(final User user, final Project project, final String versionName);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} collection within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionsResult}.
     * The versions collection will be empty if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In this case, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @return VerionsResult object
     */
    VersionsResult getVersionsByProject(final com.opensymphony.user.User user, final Project project);

    /**
     * Used to retrieve a {@link com.atlassian.jira.project.version.Version} collection within project
     * specified by {@link com.atlassian.jira.project.Project} object.
     * This method returns a {@link com.atlassian.jira.bc.project.version.VersionService.VersionsResult}.
     * The versions collection will be empty if the user making the request
     * does not have the ADMIN, PROJECT_ADMIN or BROWSE project permission for the project.
     * In this case, the errorCollection in the result object will contain an appropriate error message.
     *
     * @param user The user trying to get a version
     * @param project The project object containing requested version
     * @return VerionsResult object
     */
    VersionsResult getVersionsByProject(final User user, final Project project);

    /**
     * This method needs to be called before creating a version to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project object and versionName.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version with the name provided already exists and throw an appropriate error.
     * <p/>
     * Optional validation will be done for the release date, if provided. An error will be returned,
     * if date format is valid.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to create a version
     * @param project The project object containing requested version
     * @param versionName The name of created version
     * @param releaseDate The release date for a version (optional)
     * @param description The description for a version (optional)
     * @param scheduleAfterVersion The version after which created version should be scheduled (optional)
     * @return CreateVersionValidationResult object
     */
    CreateVersionValidationResult validateCreateVersion(final com.opensymphony.user.User user, final Project project, final String versionName,
            final String releaseDate, final String description, final Long scheduleAfterVersion);

    /**
     * This method needs to be called before creating a version to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project object and versionName.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version with the name provided already exists and throw an appropriate error.
     * <p/>
     * Optional validation will be done for the release date, if provided. An error will be returned,
     * if date format is valid.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to create a version
     * @param project The project object containing requested version
     * @param versionName The name of created version
     * @param releaseDate The release date for a version (optional)
     * @param description The description for a version (optional)
     * @param scheduleAfterVersion The version after which created version should be scheduled (optional)
     * @return CreateVersionValidationResult object
     */
    CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final String releaseDate, final String description, final Long scheduleAfterVersion);

    /**
     * This method needs to be called before creating a version to ensure all parameters are correct.  There are a
     * number of required parameters, such as a project object and versionName.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version with the name provided already exists and throw an appropriate error.
     * <p/>
     * Optional validation will be done for the release date, if provided. An error will be returned,
     * if date format is valid.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to create a version
     * @param project The project object containing requested version
     * @param versionName The name of created version
     * @param releaseDate The release date for a version (optional)
     * @param description The description for a version (optional)
     * @param scheduleAfterVersion The version after which created version should be scheduled (optional)
     * @return CreateVersionValidationResult object
     */
    CreateVersionValidationResult validateCreateVersion(final User user, final Project project, final String versionName,
            final Date releaseDate, final String description, final Long scheduleAfterVersion);

    /**
     * Using the validation result from {@link #validateCreateVersion(User, com.atlassian.jira.project.Project,
     * String, String, String, Long)} a new version will be created.  This method will throw an RuntimeException if
     * the version could not be created.
     *
     * @param user The user trying to get a version
     * @param request The {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult} object
     *  containg all required data
     * @return created Version object
     */
    Version createVersion(com.opensymphony.user.User user, CreateVersionValidationResult request);

    /**
     * Using the validation result from {@link #validateCreateVersion(User, com.atlassian.jira.project.Project,
     * String, String, String, Long)} a new version will be created.  This method will throw an RuntimeException if
     * the version could not be created.
     *
     * @param user The user trying to get a version
     * @param request The {@link com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult} object
     *  containg all required data
     * @return created Version object
     */
    Version createVersion(User user, CreateVersionValidationResult request);

    /**
     * This method needs to be called before releasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is not released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to release a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateReleaseVersion(final com.opensymphony.user.User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before releasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is not released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to release a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before releasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is not released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to release a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateReleaseVersion(final User user, final Version version, final String releaseDate);

    /**
     * This method needs to be called before unreleasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to unrelease a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateUnreleaseVersion(final com.opensymphony.user.User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before unreleasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to unrelease a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final Date releaseDate);

    /**
     * This method needs to be called before unreleasing a version to ensure all parameters are correct.  There is
     * required parameter, version object.
     * An error will be returned if the user making the request does not have the ADMIN or PROJECT_ADMIN
     * permission for the project.
     * The validation will also check if a version provided has a valid name and if is released already.
     * <p/>
     * The method will return a {@link com.atlassian.jira.bc.project.version.VersionService.ReleaseVersionValidationResult}
     * which contains an ErrorCollection with any potential errors and all the version's details.
     *
     * @param user The user trying to unrelease a version
     * @param version The version to release
     * @param releaseDate The version release date (optional)
     * @return ReleaseVersionValidationResult object
     */
    ReleaseVersionValidationResult validateUnreleaseVersion(final User user, final Version version, final String releaseDate);

    /**
     * Using the validation result from {@link #validateReleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be released.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be released.
     *
     * @param result a ReleaseVersionValidationResult containg required data
     * @return a released version object
     */
    Version releaseVersion(final ReleaseVersionValidationResult result);

    /**
     * Using the validation result from {@link #validateReleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be released.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be released.
     *
     * @param user The user trying to release a version
     * @param currentVersion The current version being released.
     * @param newVersion The version to move issues to.
     */
    void moveUnreleasedToNewVersion(final User user, final Version currentVersion, final Version newVersion);

    /**
     * Using the validation result from {@link #validateUnreleaseVersion(User,
     * com.atlassian.jira.project.version.Version, Date)} a version will be unreleased.
     * This method will throw an IllegalArgumentException if the provied data are invalid and version could not be unreleased.
     *
     * @param result a ReleaseVersionValidationResult containg required data
     * @return a unreleased version object
     */
    Version unreleaseVersion(final ReleaseVersionValidationResult result);

    /**
     * This method should be called before archiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed in hasn't already been archived. If there's any
     * errors, the validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateArchiveVersion(final com.opensymphony.user.User user, final Version version);

    /**
     * This method should be called before archiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed in hasn't already been archived. If there's any
     * errors, the validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateArchiveVersion(final User user, final Version version);

    /**
     * This method should be called before unarchiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed is currently archived. If there's any errors, the
     * validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateUnarchiveVersion(final com.opensymphony.user.User user, final Version version);

    /**
     * This method should be called before unarchiving a version. It performs some basic validation of the version that
     * was passed in. This includes a null check, checking that the version name isn't empty, and checking that the
     * version is linked against a valid project.
     * <p/>
     * The method also validates that the user passed in is either a global admin, or has project admin rights for the
     * project that the version is linked to.
     * <p/>
     * Finally, this method checks that the version that was passed is currently archived. If there's any errors, the
     * validationResult will contain appropriate errors and wont be valid.
     *
     * @param user The user performing this operation
     * @param version The version to be archived
     * @return a validation result, containing any errors or the version details on success
     */
    ArchiveVersionValidationResult validateUnarchiveVersion(final User user, final Version version);

    /**
     * Takes a validation result and performs the archive operation.
     *
     * @param result The result from the validation
     * @return The version that was archived.  Ideally this version should have been retrieved from the store for
     *         consistency
     * @throws IllegalStateException if the result passed in is not valid.
     */
    Version archiveVersion(final ArchiveVersionValidationResult result);

    /**
     * Takes a validation result and performs the unarchive operation.
     *
     * @param result The result from the validation
     * @return The version that was unarchived.  Ideally this version should have been retrieved from the store for
     *         consistency
     * @throws IllegalStateException if the result passed in is not valid.
     */
    Version unarchiveVersion(final ArchiveVersionValidationResult result);

    /**
     * Is the passed version overdue? This method does no permission checks on the passed version.
     *
     * @param version the version to check.
     *
     * @return true if the passed version is overdue.
     */
    boolean isOverdue(Version version);

    /**
     * Validate Move a version to the start of the version list.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateMoveToStartVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to have a lower sequence number - ie make it earlier.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateIncreaseVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to have a higher sequence number - ie make it later.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateDecreaseVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version to the end of the version sequence.
     *
     * @param user  The user trying to move a version
     * @param versionId
     * @return a validation result, containing any errors or the version details on success
     */
    MoveVersionValidationResult validateMoveToEndVersionSequence(final User user, long versionId);

    /**
     * Validate Move a version after another version.
     * @param user  The user trying to move a version
     * @param versionId version to reschedule
     * @param scheduleAfterVersion id of the version to schedule after the given version object
     * @return a validation result, containing any errors or the version details and schedule after target on success
     */
    MoveVersionValidationResult validateMoveVersionAfter(final User user, long versionId, Long scheduleAfterVersion);

    /**
     * Move a version to the start of the version list.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveToStartVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to have a lower sequence number - ie make it earlier.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void increaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to have a higher sequence number - ie make it later.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void decreaseVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version to the end of the version sequence.
     *
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveToEndVersionSequence(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Move a version after another version.
     * @param moveVersionValidationResult Move Version Validation Result
     */
    void moveVersionAfter(MoveVersionValidationResult moveVersionValidationResult);

    /**
     * Return the count of Issues Fixed in this version.
     *
     * @param version
     * @return A count of issues
     */
    public long getFixIssuesCount(Version version);

    /**
     * Return the count of Issues that affect this version.
     *
     * @param version
     * @return A count of issues
     */
    public long getAffectsIssuesCount(Version version);

    /**
     * Return the count of Issues that are unresolved in this version. Used when
     * releasing a version to get user confirmation about what to do with
     * the unresolved issues.
     *
     * @param user the user trying to release the version
     * @param version which version to check for unresolved issues
     * @return A count of issues
     */
    public long getUnresolvedIssuesCount(User user, Version version);

    /**
     * Represents the results of performing a validation call for a single merge or delete operation.
     */
    interface ValidationResult
    {
        boolean isValid();

        Version getVersionToDelete();

        Version getFixSwapVersion();

        Version getAffectsSwapVersion();

        Set<Reason> getReasons();

        ErrorCollection getErrorCollection();

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Version not found
             */
            NOT_FOUND,
            /**
             * The version specified to swap to is invalid
             */
            SWAP_TO_VERSION_INVALID
        }

    }

    public static class CreateVersionValidationResult extends ServiceResultImpl
    {
        private final Project project;
        private final String versionName;
        private final Date releaseDate;
        private final String description;
        private final Long scheduleAfterVersion;
        private final Set<Reason> reasons;

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Project was not specified.
             */
            BAD_PROJECT,
            /**
             * Version name is not valid.
             */
            BAD_NAME,
            /**
             * Version name already exists for that project.
             */
            DUPLICATE_NAME,
            /**
             * The release date specified was invalid.
             */
            BAD_RELEASE_DATE,
            /**
             * The value was beyond specified length
             */
            VERSION_NAME_TOO_LONG
        }

        public CreateVersionValidationResult(ErrorCollection errorCollection, Set<Reason> reasons)
        {
            super(errorCollection);
            this.reasons = Collections.unmodifiableSet(reasons);
            this.project = null;
            this.versionName = null;
            this.releaseDate = null;
            this.description = null;
            this.scheduleAfterVersion = null;
        }

        public CreateVersionValidationResult(ErrorCollection errorCollection, Project project, String versionName,
                Date releaseDate, String description, Long scheduleAfterVersion)
        {
            super(errorCollection);
            this.reasons = Collections.emptySet();
            this.project = project;
            this.versionName = versionName;
            this.releaseDate = releaseDate;
            this.description = description;
            this.scheduleAfterVersion = scheduleAfterVersion;
        }

        public Project getProject()
        {
            return project;
        }

        public String getVersionName()
        {
            return versionName;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }

        public String getDescription()
        {
            return description;
        }

        public Long getScheduleAfterVersion()
        {
            return scheduleAfterVersion;
        }

        public Set<Reason> getReasons()
        {
            return reasons;
        }
    }

    public abstract static class AbstractVersionResult extends ServiceResultImpl
    {
        private final Version version;

        public AbstractVersionResult(ErrorCollection errorCollection)
        {
            this(errorCollection, null);
        }

        public AbstractVersionResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection);
            this.version = version;
        }

        public Version getVersion()
        {
            return version;
        }
    }

    public static class VersionResult extends AbstractVersionResult
    {

        public VersionResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public VersionResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
        }
    }

    public static class VersionsResult extends ServiceResultImpl
    {
        private final Collection<Version> versions;

        public VersionsResult(ErrorCollection errorCollection)
        {
            this(errorCollection, Collections.<Version>emptyList());
        }

        public VersionsResult(ErrorCollection errorCollection, Collection<Version> versions)
        {
            super(errorCollection);
            this.versions = versions;
        }

        public Collection<Version> getVersions()
        {
            return versions;
        }
    }

    public static class ReleaseVersionValidationResult extends AbstractVersionResult
    {
        private final Date releaseDate;

        public ReleaseVersionValidationResult(ErrorCollection errorCollection)
        {
            this(errorCollection, null, null);
        }

        public ReleaseVersionValidationResult(ErrorCollection errorCollection, Version version, Date releaseDate)
        {
            super(errorCollection, version);
            this.releaseDate = releaseDate;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }
    }

    public static class ArchiveVersionValidationResult extends AbstractVersionResult
    {
        public ArchiveVersionValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
        }

        public ArchiveVersionValidationResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
        }
    }

    public static class MoveVersionValidationResult extends AbstractVersionResult
    {
        private Long scheduleAfterVersion;
        private final Set<Reason> reasons;

        public static enum Reason
        {
            /**
             * Not allowed to create a version.
             */
            FORBIDDEN,
            /**
             * Version not found
             */
            NOT_FOUND,
            /**
             * schedule after version not found
             */
            SCHEDULE_AFTER_VERSION_NOT_FOUND
        }


        public MoveVersionValidationResult(ErrorCollection errorCollection,  Set<Reason> reasons)
        {
            super(errorCollection);
            this.reasons = Collections.unmodifiableSet(reasons);
        }

        public MoveVersionValidationResult(ErrorCollection errorCollection, Version version)
        {
            super(errorCollection, version);
            this.reasons = Collections.emptySet();
        }

        public MoveVersionValidationResult(ErrorCollection errorCollection, Version version, Long scheduleAfterVersion)
        {
            super(errorCollection, version);
            this.reasons = Collections.emptySet();
            this.scheduleAfterVersion = scheduleAfterVersion;
        }

        public Set<Reason> getReasons()
        {
            return reasons;
        }

        public Long getScheduleAfterVersion()
        {
            return scheduleAfterVersion;
        }
    }

    interface VersionAction
    {
        boolean isSwap();

        Long getSwapVersionId();
    }
}
