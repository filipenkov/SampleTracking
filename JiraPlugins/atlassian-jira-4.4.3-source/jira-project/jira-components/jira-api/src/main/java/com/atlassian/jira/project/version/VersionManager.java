/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.index.IndexException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Manager responsible for <a href="http://www.atlassian.com/software/jira/docs/latest/version_management.html">JIRA versions</a>.
 */
public interface VersionManager
{
    /**
     * Used to represent empty version fields
     */
    public static final String NO_VERSIONS = "-1";

    /**
     * Used to retrieve all unreleased versions
     */
    public static final String ALL_UNRELEASED_VERSIONS = "-2";

    /**
     * Used to retrieve all released versions
     */
    public static final String ALL_RELEASED_VERSIONS = "-3";


    /**
     * Creates a new {@link Version} object.
     *
     * @param name
     * @param releaseDate          date of release or null if not released.
     * @param description
     * @param project              the GenericValue of the Project of the version.
     * @param scheduleAfterVersion id of the version after which this should be sequenced or null.
     * @return the new Version.
     * @throws CreateException        If there was a problem creating the version.
     * @deprecated now use #createVersion with projectId instead of generic value
     */
    Version createVersion(String name, Date releaseDate, String description, GenericValue project, Long scheduleAfterVersion) throws CreateException;

    /**
     * Creates a new {@link Version} object. Takes a project ID instead of a project GenericValue.
     *
     * @param name
     * @param releaseDate          date of release or null if not released.
     * @param description
     * @param projectId            the id of the Project of the version.
     * @param scheduleAfterVersion id of the version after which this should be sequenced or null.
     * @return the new Version.
     * @throws CreateException        If there was a problem creating the version.
     */
    Version createVersion(String name, Date releaseDate, String description, Long projectId, Long scheduleAfterVersion) throws CreateException;

    // ---- Scheduling Methods ----

    /**
     * Move a version to the start of the version list
     *
     * @param version
     * @throws GenericEntityException
     */
    void moveToStartVersionSequence(Version version) throws GenericEntityException;

    /**
     * Move a version to have a lower sequence number - ie make it earlier
     *
     * @param version
     * @throws GenericEntityException
     */
    void increaseVersionSequence(Version version) throws GenericEntityException;

    /**
     * Move a version to have a higher sequence number - ie make it later
     *
     * @param version
     * @throws GenericEntityException
     */
    void decreaseVersionSequence(Version version) throws GenericEntityException;

    /**
     * Move a version to the end of the version sequence
     *
     * @param version
     * @throws GenericEntityException
     */
    void moveToEndVersionSequence(Version version) throws GenericEntityException;

    /**
     * Move a version after another version
     * @param version version to reschedule
     * @param scheduleAfterVersion id of the version to schedule after the given version object
     */
    void moveVersionAfter(Version version, Long scheduleAfterVersion);

    // ---- Delete Version Methods ----

    /**
     * This method is used by the merge and delete actions.
     * The action type needs to be specified as the action params are validated differently.
     *
     * @param actionType           See {@link VersionKeys#DELETE_ACTION},{@link VersionKeys#MERGE_ACTION}
     * @param version              The version to delete.
     * @param affectsAction        Used to decide whether to move all the issues to a different 'affects' version or just remove them. See {@link VersionKeys#REMOVE_ACTION}, {@link VersionKeys#SWAP_ACTION}
     * @param affectsSwapVersionId The new affects version to move issues to.
     * @param fixAction            Used to decide wether to move all the issues to a different 'fix' version or just remove them. See {@link VersionKeys#REMOVE_ACTION}, {@link VersionKeys#SWAP_ACTION}
     * @param fixSwapVersionId     The new fix version to move issues to.
     * @throws GenericEntityException
     * @deprecated since v3.13. Use {@link #deleteVersion(Version)} instead.
     */
    void deleteVersion(String actionType, Version version, String affectsAction, Long affectsSwapVersionId, String fixAction, Long fixSwapVersionId) throws GenericEntityException;

    /**
     * Removes a specific version from the system.
     *
     * @param version              The version to be removed.
     */
    void deleteVersion(final Version version);

    // ---- Edit Version Name Methods ----

    /**
     * Updates details for an existing version.
     *
     * @param version     The version to update
     * @param name
     * @param description
     * @param project     Used to check for duplicate version names in a project.
     * @throws GenericEntityException
     * @throws IllegalArgumentException If the name is not set, or already exists.
     */
    void editVersionDetails(Version version, String name, String description, GenericValue project) throws GenericEntityException;

    /**
     * Check that the version name we are changing to is not a duplicate.
     * @param @param version     The version to update
     * @param name The new name for the version
     * @param project Used to check for duplicate version names in a project.
     * @return
     */
    public boolean isDuplicateName(Version version, final String name, final GenericValue project);


    // ---- Release Version Methods ----
    /**
     * Used to release or unrelease a version, depending on the release flag.
     *
     * @param version Version to be released (or unreleased)
     * @param release  True to release a version. False to 'unrelease' a version
     */
    void releaseVersion(Version version, boolean release);

    /**
     * Used to release versions depending on the release flag.
     *
     * @param versions Collection of {@link Version}s
     * @param release  True to release a version. False to 'unrelease' a version
     */
    void releaseVersions(Collection<Version> versions, boolean release);

    /**
     * Swaps the list of issues supplied from one version to another.
     *
     * @param issues
     * @param currentVersion
     * @param swapToVersion
     * @throws GenericEntityException
     * @throws IndexException
     */
    void moveIssuesToNewVersion(List issues, Version currentVersion, Version swapToVersion) throws GenericEntityException, IndexException;

    // ---- Archive Version Methods ----

    /**
     * Method used to archive and un-archive a number of versions.
     *
     * @param idsToArchive
     * @param idsToUnarchive
     * @throws GenericEntityException
     */
    void archiveVersions(String[] idsToArchive, String[] idsToUnarchive) throws GenericEntityException;

    /**
     * Archive/Un-archive a single version depending on the archive flag.
     *
     * @param version
     * @param archive
     */
    void archiveVersion(Version version, boolean archive);

    /**
     * Return all un-archived versions for a particular project
     *
     * @param projectGV
     * @deprecated Use {@link #getVersionsUnarchived(Long)} instead.  Since v3.10
     * @return A collection of {@link Version}s
     * @throws GenericEntityException
     */
    Collection<Version> getVersionsUnarchived(GenericValue projectGV) throws GenericEntityException;

    /**
     * Return all un-archived versions for a particular project
     * @param projectId id of the project.
     * @return A collection of {@link Version}s
     * @throws GenericEntityException If there's a problem retrieving versions
     * @since v3.10
     */
    Collection<Version> getVersionsUnarchived(Long projectId);

    /**
     * Return all archived versions for a particular project.
     *
     * @param projectGV
     * @return A collections of {@link Version}s
     * @throws GenericEntityException
     */
    Collection<Version> getVersionsArchived(GenericValue projectGV) throws GenericEntityException;

    // ---- Version Release Date Mthods ----

    /**
     * Update the release date of a version.
     *
     * @param version
     * @param duedate
     * @throws GenericEntityException
     */
    void editVersionReleaseDate(Version version, Date duedate) throws GenericEntityException;

    /**
     * Checks to see if a version is overdue.  Note: This method checks if the due date
     * set for a version is previous to last midnight. (not now()).
     *
     * @param version
     * @return True if the version is overdue. (i.e. releaseDate is before last midnight)
     */
    public boolean isVersionOverDue(Version version);

    /**
     * Gets all the versions for a project.
     *
     * @param project
     * @return a List of Version objects.
     * @deprecated use #getVersions(Long)
     */
    List<Version> getVersions(GenericValue project);

    /**
     * Return a list of {@link Version}s for a project.
     *
     * @param projectId
     * @return a List of Version objects.
     */
    List<Version> getVersions(Long projectId);

    /**
     * Return a collection of {@link Version}s that have the specified name.
     *
     * @param versionName the name of the version (case-insensitive)
     * @return a Collection of Version objects. Never null.
     */
    Collection<Version> getVersionsByName(String versionName);

    /**
     * Return a collection of {@link Version}s matching the ids passed in.
     *
     * @param ids
     * @return
     */
    Collection<Version> getVersions(List<Long> ids);

    /**
     * Returns a single version.
     *
     * @param id
     * @return A {@link Version} object.
     */
    Version getVersion(Long id);

    /**
     * Search for a version by projectID and name.
     *
     * @param projectId
     * @param versionName
     * @return A {@link Version} object.
     */
    Version getVersion(Long projectId, String versionName);

    /**
     * Search for a version by projectID and name.
     *
     * @param project
     * @param name
     * @return A {@link Version} object.
     * @deprecated Use {@link #getVersion(Long,String)} instead
     */
    Version getVersion(GenericValue project, String name);

    /**
     * Gets a list of un-released versions for a particular project.
     *
     * @param project
     * @param includeArchived
     * @return A collection of {@link Version}s
     * @deprecated Use {@link #getVersionsUnreleased(Long,boolean)} instead, since v3.10.
     */
    Collection<Version> getVersionsUnreleased(GenericValue project, boolean includeArchived);

    /**
     * Gets a list of un-released versions for a particular project.
     *
     * @param projectId       The id of the project for which to return versions
     * @param includeArchived True if archived versions should be included
     * @return A Collection of {@link com.atlassian.jira.project.version.Version}s, never null
     * @since v3.10
     */
    Collection<Version> getVersionsUnreleased(Long projectId, boolean includeArchived);

    /**
     * Gets a list of released versions for a project. This list will include
     * archived versions if the 'includeArchived' flag is set to true.
     *
     * @param project         project generic value
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     * @deprecated use {@link #getVersionsReleased(Long,boolean)} instead
     */
    Collection<Version> getVersionsReleased(GenericValue project, boolean includeArchived);

    /**
     * Gets a list of released versions for a project. This list will include
     * archived versions if the 'includeArchived' flag is set to true.
     *
     * @param projectId       project id
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     */
    Collection<Version> getVersionsReleased(Long projectId, boolean includeArchived);

    /**
     * Gets a list of released versions for a project in reverse order.
     * This list will include archived versions if the 'includeArchived' flag
     * is set to true.
     *
     * @param project         project generic value
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     * @deprecated use {@link #getVersionsReleasedDesc(Long, boolean)} instead
     */
    Collection<Version> getVersionsReleasedDesc(GenericValue project, boolean includeArchived);

    /**
     * Gets a list of released versions for a project in reverse order.
     * This list will include archived versions if the 'includeArchived' flag
     * is set to true.
     *
     * @param projectId         project id
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     */
    Collection<Version> getVersionsReleasedDesc(Long projectId, boolean includeArchived);

    /**
     * Return all other versions in the project except this one
     *
     * @param version
     * @return
     * @throws GenericEntityException
     */
    public Collection<Version> getOtherVersions(Version version) throws GenericEntityException;

    /**
     * Return all unarchived versions except this one
     *
     * @param version
     * @return
     * @throws GenericEntityException
     */
    public Collection<Version> getOtherUnarchivedVersions(Version version) throws GenericEntityException;

    /**
     * Return all Issues that are associated with the specified versions
     *
     * @param versions a collection of {@link Version} objects
     * @return A collection of issue {@link GenericValue}s
     * @throws GenericEntityException
     */
    public Collection<GenericValue> getAllAffectedIssues(Collection<Version> versions) throws GenericEntityException;

    /**
     * Return Fix Issues
     *
     * @param version
     * @return A collection of issue {@link GenericValue}s
     * @throws GenericEntityException
     */
    public Collection<GenericValue> getFixIssues(Version version) throws GenericEntityException;

    /**
     * Return 'Affects' Issues
     *
     * @param version
     * @return A collection of issue {@link GenericValue}s
     * @throws GenericEntityException
     */
    public Collection<GenericValue> getAffectsIssues(Version version) throws GenericEntityException;

    /**
     * @param issue
     * @return A collection of 'affects' {@link Version}s for an issue.
     */
    Collection<Version> getAffectedVersionsByIssue(GenericValue issue);

    /**
     * @param issue
     * @return A collection of 'fix for' {@link Version}s for an issue.
     */
    Collection<Version> getFixVersionsByIssue(GenericValue issue);

    /**
     * @return all versions in JIRA. Never null.
     */
    Collection<Version> getAllVersions();

    /**
     * @param includeArchived whether or not to include archived versions
     * @return all released versions in JIRA. Never null.
     */
    Collection<Version> getAllVersionsReleased(boolean includeArchived);

    /**
     * @param includeArchived whether or not to include archived versions
     * @return all released versions in JIRA. Never null.
     */
    Collection<Version> getAllVersionsUnreleased(boolean includeArchived);
}
