/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */
package com.atlassian.jira.project.version;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DefaultVersionManager implements VersionManager
{
    private static final Logger log = Logger.getLogger(DefaultVersionManager.class);
    protected final IssueManager issueManager;
    protected final CollectionReorderer collectionReorderer;
    protected final AssociationManager associationManager;
    protected final IssueIndexManager issueIndexManager;
    protected final ProjectManager projectManager;
    protected final VersionStore versionStore;

    public DefaultVersionManager(final IssueManager issueManager, final CollectionReorderer collectionReorderer, final AssociationManager associationManager, final IssueIndexManager issueIndexManager, final ProjectManager projectManager, final VersionStore versionStore)
    {
        this.issueManager = issueManager;
        this.collectionReorderer = collectionReorderer;
        this.associationManager = associationManager;
        this.issueIndexManager = issueIndexManager;
        this.projectManager = projectManager;
        this.versionStore = versionStore;
    }

    public Version createVersion(final String name, final Date releaseDate, final String description, final GenericValue project, final Long scheduleAfterVersion) throws CreateException
    {
        if (project == null)
        {
            log.error("You cannot create a version without a project.");
            throw new CreateException("You cannot create a version without a project.");
        }
        return createVersion(name, releaseDate, description, project.getLong("id"), scheduleAfterVersion);
    }

    public Version createVersion(final String name, final Date releaseDate, final String description, final Long projectId, final Long scheduleAfterVersion) throws CreateException
    {
        final Map<String, Object> versionParams = new HashMap<String, Object>();
        if (!TextUtils.stringSet(name))
        {
            log.error("You cannot create a version without a name.");
            throw new CreateException("You cannot create a version without a name.");
        }
        if (projectId == null)
        {
            log.error("You cannot create a version without a project.");
            throw new CreateException("You cannot create a version without a project.");
        }

        versionParams.put("name", name);
        versionParams.put("project", projectId);

        // This determines where in the scheduling order the new version should be placed.
        if (scheduleAfterVersion != null)
        {
            if (scheduleAfterVersion == -1L)
            {
                // Decrease all version sequences in order to place new version in first position
                moveAllVersionSequences(projectId);
                // New version sequence will be first position
                versionParams.put("sequence", 1L);
            }
            else
            {
                // Decrease version sequences which follow this version in order to slot in new version
                moveVersionSequences(scheduleAfterVersion);
                // New version sequence will follow this version
                final Long newSequence = getVersion(scheduleAfterVersion).getSequence() + 1L;
                versionParams.put("sequence", newSequence);
            }
        }
        else
        {
            versionParams.put("sequence", getMaxVersionSequence(projectId));
        }

        if (releaseDate != null)
        {
            versionParams.put("releasedate", new Timestamp(releaseDate.getTime()));
        }

        versionParams.put("description", description);

        return new VersionImpl(projectManager, versionStore.createVersion(versionParams));
    }

    // ---- Scheduling Methods ----
    public void moveToStartVersionSequence(final Version version)
    {
        final List<Version> versions = new ArrayList<Version>(getAllVersions(version));
        collectionReorderer.moveToStart(versions, version);
        storeReorderedVersionList(versions);
    }

    public void increaseVersionSequence(final Version version)
    {
        final List<Version> versions = new ArrayList<Version>(getAllVersions(version));
        collectionReorderer.increasePosition(versions, version);
        storeReorderedVersionList(versions);
    }

    public void decreaseVersionSequence(final Version version)
    {
        final List<Version> versions = new ArrayList<Version>(getAllVersions(version));

        collectionReorderer.decreasePosition(versions, version);

        storeReorderedVersionList(versions);
    }

    public void moveToEndVersionSequence(final Version version)
    {
        final List<Version> versions = new ArrayList<Version>(getAllVersions(version));
        collectionReorderer.moveToEnd(versions, version);
        storeReorderedVersionList(versions);
    }

    public void moveVersionAfter(final Version version, final Long scheduleAfterVersionId)
    {
        if (version == null)
        {
            log.error("You cannot move a null version");
            throw new IllegalArgumentException("You cannot move a null version");
        }

        //Don't re-schedule if the scheduleAfterVersion id is of itself (Note: scheduleAfterVersion can be null)
        if ((version.getId() != null) && !version.getId().equals(scheduleAfterVersionId))
        {
            Version targetVersion;
            if (scheduleAfterVersionId == null)
            {
                targetVersion = getLastVersion(version.getProjectObject().getId());//move to last version
            }
            else if (scheduleAfterVersionId == -1L)
            {
                targetVersion = null;//move to start
            }
            else
            //move to the target version
            {
                targetVersion = getVersion(scheduleAfterVersionId);
            }

            final List<Version> versions = new ArrayList<Version>(getAllVersions(version));
            collectionReorderer.moveToPositionAfter(versions, version, targetVersion);
            storeReorderedVersionList(versions);
        }
    }

    // Increases sequence numbers for versions (make them later) to make space for new version
    private void moveVersionSequences(final Long scheduleAfterVersion)
    {
        final Version startVersion = getVersion(scheduleAfterVersion);
        final Collection<Version> versions = getVersions(startVersion.getProject());
        final List<Version> versionsChanged = new ArrayList<Version>();

        for (final Version version : versions)
        {
            if (version.getSequence() > startVersion.getSequence())
            {
                final Long newSequence = version.getSequence() + 1L;
                version.setSequence(newSequence);
                versionsChanged.add(version);
            }
        }
        versionStore.storeVersions(versionsChanged);
    }

    // Increases sequence numbers for all versions (make them later) to make space for new version
    private void moveAllVersionSequences(final Long project)
    {
        final Collection<Version> versions = getVersions(project);
        final List<Version> versionsChanged = new ArrayList<Version>();

        for (final Version version : versions)
        {
            final Long newSequence = version.getSequence() + 1L;
            version.setSequence(newSequence);
            versionsChanged.add(version);
        }

        versionStore.storeVersions(versionsChanged);
    }

    /**
     * This method is used by the merge and delete actions.
     * The action type needs to be specified as the action params are validated differently.
     * @deprecated since v3.13. Use {@link com.atlassian.jira.bc.project.version.VersionService#delete(com.atlassian.jira.bc.JiraServiceContext , com.atlassian.jira.bc.project.version.VersionService.ValidationResult)} instead.
     */
    public void deleteVersion(final String actionType, final Version version, final String affectsAction, final Long affectsSwapVersionId, final String fixAction, final Long fixSwapVersionId)
    {
        Collection affectedIssues;

        if (VersionKeys.MERGE_ACTION.equals(actionType))
        {
            validateMergeParams(version, affectsSwapVersionId);
        }
        else if (VersionKeys.DELETE_ACTION.equals(actionType))
        {
            validateDeleteParams(version, affectsAction, affectsSwapVersionId, fixAction, fixSwapVersionId);
        }
        else
        {
            throw new IllegalArgumentException("Unknown action specified for delete operation");
        }

        try
        {
            affectedIssues = getAllAffectedIssues(EasyList.build(version));

            swapVersions(affectsAction, affectsSwapVersionId, fixAction, fixSwapVersionId, version);

            // remove all associations to this version
            associationManager.removeAssociationsFromSink(version.getGenericValue());

            // reindex all the affected issues
            issueIndexManager.reIndexIssues(affectedIssues);

            versionStore.deleteVersion(version.getGenericValue());

            //JRA-13766: We need to get all the versions, and re-store the versions where sequence numbers are not
            //correct.
            storeReorderedVersionList(getAllVersions(version));
        }
        catch (final GenericEntityException e)
        {
            log.error(e, e);
        }
        catch (final Exception e)
        {
            log.error(e, e);
        }
    }

    public void deleteVersion(final Version version)
    {
        versionStore.deleteVersion(version.getGenericValue());

        //JRA-13766: We need to get all the versions, and re-store the versions where sequence numbers are not
        //correct.
        reorderVersionsInProject(version);
    }

    // ---- Edit Version Name methods ----
    public void editVersionDetails(final Version version, final String versionName, final String description, final GenericValue project)
    {
        //There must be a name for the entity
        if (!TextUtils.stringSet(versionName))
        {
            log.error("You must specify a valid version name.");
            throw new IllegalArgumentException("You must specify a valid version name.");
        }
        else
        {
            //if the name already exists then add an Error message
            if (isDuplicateName(version, versionName, project))
            {
                log.error("A version with this name already exists in this project.");
                throw new IllegalArgumentException("A version with this name already exists in this project.");
            }
        }

        version.setName(versionName);
        version.setDescription(description);

        versionStore.storeVersion(version);
    }

    // ---- Release Version methods ----
    public void releaseVersion(Version version, boolean release)
    {
        releaseVersions(Collections.singleton(version), release);
    }

    public void releaseVersions(final Collection<Version> versions, final boolean release)
    {
        final List<Version> versionsChanged = new ArrayList<Version>();

        for (final Version version : versions)
        {
            validateReleaseParams(version, release);
            version.setReleased(release);

            versionsChanged.add(version);
        }

        if (!versionsChanged.isEmpty())
        {
            versionStore.storeVersions(versionsChanged);
        }
    }

    public void moveIssuesToNewVersion(final List issues, final Version currentVersion, final Version swapToVersion) throws IndexException
    {
        if (!issues.isEmpty())
        {
            swapVersionAssociations(currentVersion, swapToVersion, IssueRelationConstants.FIX_VERSION, issues);
            issueIndexManager.reIndexIssues(issues);
        }
    }

    // ---- Archive Version methods ----
    public void archiveVersions(final String[] idsToArchive, final String[] idsToUnarchive)
    {
        final List<Version> versionsChanged = new ArrayList<Version>();

        for (String anIdsToArchive : idsToArchive)
        {
            final Long archiveId = new Long(anIdsToArchive);
            final Version version = getVersion(archiveId);
            if ((version != null) && !version.isArchived())
            {
                version.setArchived(true);
                versionsChanged.add(version);
            }
        }

        for (String anIdsToUnarchive : idsToUnarchive)
        {
            final Long unArchiveId = new Long(anIdsToUnarchive);
            final Version version = getVersion(unArchiveId);
            if ((version != null) && version.isArchived())
            {
                version.setArchived(false);
                versionsChanged.add(version);
            }
        }

        if (!versionsChanged.isEmpty())
        {
            versionStore.storeVersions(versionsChanged);
        }
    }

    public void archiveVersion(final Version version, final boolean archive)
    {
        version.setArchived(archive);
        versionStore.storeVersion(version);
    }

    // ---- Version Due Date Mthods ----
    public void editVersionReleaseDate(final Version version, final Date duedate)
    {
        //Theversion must be specified
        if (version == null)
        {
            log.error("You must specify a valid version.");
            throw new IllegalArgumentException("You must specify a valid version.");
        }

        version.setReleaseDate(duedate);

        versionStore.storeVersion(version);
    }

    public boolean isVersionOverDue(final Version version)
    {
        if (version.getReleaseDate() == null || version.isArchived() || version.isReleased())
        {
            return false;
        }
        final Calendar releaseDate = Calendar.getInstance();
        releaseDate.setTime(version.getReleaseDate());

        final Calendar lastMidnight = Calendar.getInstance();
        lastMidnight.set(Calendar.HOUR_OF_DAY, 0);
        lastMidnight.set(Calendar.MINUTE, 0);
        lastMidnight.set(Calendar.SECOND, 0);
        lastMidnight.set(Calendar.MILLISECOND, 0);

        return releaseDate.before(lastMidnight);
    }

    // ---- Get Version(s) methods ----
    public Collection<Version> getVersionsUnarchived(final Long projectId)
    {
        return queryDatabase(EasyMap.build("project", projectId, "archived", null));
    }

    public Collection<Version> getVersionsUnarchived(final GenericValue projectGV)
    {
        return getVersionsUnarchived(projectGV.getLong("id"));
    }

    public Collection<Version> getVersionsArchived(final GenericValue projectGV)
    {
        return queryDatabase(EasyMap.build("project", projectGV.getLong("id"), "archived", "true"));
    }

    public List<Version> getVersions(final GenericValue project)
    {
        return getVersions(project.getLong("id"));
    }

    public List<Version> getVersions(final Long projectId)
    {
        return queryDatabase(EasyMap.build("project", projectId));
    }

    public Collection<Version> getVersionsByName(final String versionName)
    {
        Assertions.notNull("versionName", versionName);
        final List<GenericValue> versionGvs = versionStore.getAllVersions();
        final Collection<Version> filteredVersions = new ArrayList<Version>();

        Consumer<GenericValue> consumer = new Consumer<GenericValue>()
        {
            public void consume(@NotNull final GenericValue element)
            {
                if (versionName.equalsIgnoreCase(element.getString("name")))
                {
                    filteredVersions.add(new VersionImpl(projectManager, element));
                }
            }
        };

        CollectionUtil.foreach(versionGvs, consumer);
        return filteredVersions;
    }

    /**
     *  @return A collection of {@link Version}s for an issue.
     */
    public Collection<Version> getAffectedVersionsByIssue(final GenericValue issue)
    {
        return getVersionsByIssue(issue, IssueRelationConstants.VERSION);
    }

    /**
     *  @return A collection of {@link Version}s for an issue.
     */
    public Collection<Version> getFixVersionsByIssue(final GenericValue issue)
    {
        return getVersionsByIssue(issue, IssueRelationConstants.FIX_VERSION);
    }

    public Collection<Version> getAllVersions()
    {
        return queryDatabase(null);
    }

    public Collection<Version> getAllVersionsReleased(final boolean includeArchived)
    {
        final Map params = EasyMap.build("released", "true");
        if (!includeArchived)
        {
            params.put("archived", null);
        }
        return queryDatabase(params);
    }

    public Collection<Version> getAllVersionsUnreleased(final boolean includeArchived)
    {
        final Map params = EasyMap.build("released", null);
        if (!includeArchived)
        {
            params.put("archived", null);
        }
        return queryDatabase(params);
    }

    /**
     * @param issue the issue
     * @param relationName {@link IssueRelationConstants#VERSION} or {@link IssueRelationConstants#FIX_VERSION}.
     * @return A collection of {@link Version}s for this issue.
     */
    protected Collection<Version> getVersionsByIssue(final GenericValue issue, final String relationName)
    {
        try
        {
            final List<GenericValue> versionGVs = associationManager.getSinkFromSource(issue, "Version", relationName, false);
            final List<Version> versions = new ArrayList<Version>();

            for (final GenericValue versionGV : versionGVs)
            {
                versions.add(new VersionImpl(projectManager, versionGV));
            }
            return versions;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving versions for issue with id '" + issue.getLong("id") + "'.", e);
        }
    }

    public Collection<Version> getVersions(final List<Long> ids)
    {
        final List<Version> versions = new ArrayList<Version>();

        for (final Long id : ids)
        {
            versions.add(getVersion(id));
        }

        return versions;
    }

    public Collection<Version> getVersionsUnreleased(final GenericValue project, final boolean includeArchived)
    {
        return getVersionsUnreleased(project.getLong("id"), includeArchived);
    }

    public Collection<Version> getVersionsUnreleased(final Long projectId, final boolean includeArchived)
    {
        final Map<String, Object> params = EasyMap.build("project", projectId, "released", null);

        if (!includeArchived)
        {
            params.put("archived", null);
        }

        return queryDatabase(params);
    }

    public Collection<Version> getVersionsReleased(final GenericValue project, final boolean includeArchived)
    {
        return getVersionsReleased(project.getLong("id"), includeArchived);
    }

    public Collection<Version> getVersionsReleased(final Long projectId, final boolean includeArchived)
    {
        final Map params = EasyMap.build("project", projectId, "released", "true");

        if (!includeArchived)
        {
            params.put("archived", null);
        }

        return queryDatabase(params);
    }

    public Collection<Version> getVersionsReleasedDesc(final GenericValue project, final boolean includeArchived)
    {
        return getVersionsReleasedDesc(project.getLong("id"), includeArchived);
    }

    public Collection<Version> getVersionsReleasedDesc(final Long projectId, final boolean includeArchived)
    {
        final List<Version> released = new ArrayList<Version>(getVersionsReleased(projectId, includeArchived));
        Collections.reverse(released);
        return released;
    }

    public Version getVersion(final Long id)
    {
        if (versionStore.getVersion(id) != null)
        {
            return new VersionImpl(projectManager, versionStore.getVersion(id));
        }

        return null;
    }

    /**
     * Retrieve a specific Version in a project, or <code>null</code> if no such version exists in that project.
     */
    public Version getVersion(final GenericValue project, final String versionName)
    {
        return getVersion(project.getLong("id"), versionName);
    }

    /**
     * Retrieve a specific Version in a project given the project id, or <code>null</code> if no such version exists in that project.
     */
    public Version getVersion(final Long projectId, final String versionName)
    {
        final List<Version> versions = queryDatabase(EasyMap.build("project", projectId, "name", versionName));
        if ((versions != null) && (versions.size() == 1))
        {
            return versions.get(0);
        }
        else
        {
            return null;
        }
    }

    // Return list of issues associated with versions
    public Collection<GenericValue> getAllAffectedIssues(final Collection<Version> versions)
    {
        try
        {
            final Collection<GenericValue> affectedIssues = new HashSet<GenericValue>();
            for (final Version version : versions)
            {
                affectedIssues.addAll(issueManager.getIssuesByEntity(IssueRelationConstants.VERSION, version.getGenericValue()));
                affectedIssues.addAll(issueManager.getIssuesByEntity(IssueRelationConstants.FIX_VERSION, version.getGenericValue()));
            }

            return affectedIssues;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error getting issue for versions " + ToStringBuilder.reflectionToString(versions), e);
        }
    }

    /**
     * Return all other versions in the project except this one
     */
    public Collection<Version> getOtherVersions(final Version version)
    {
        final Collection<Version> otherVersions = new ArrayList<Version>(getAllVersions(version));
        otherVersions.remove(version);
        return otherVersions;
    }

    /**
     * Return all unarchived versions except this one
     */
    public Collection<Version> getOtherUnarchivedVersions(final Version version)
    {
        final Collection<Version> otherUnarchivedVersions = new ArrayList<Version>(getVersionsUnarchived(version.getProject()));
        otherUnarchivedVersions.remove(version);
        return otherUnarchivedVersions;
    }

    public Collection<GenericValue> getFixIssues(final Version version)
    {
        try
        {
            return issueManager.getIssuesByEntity(IssueRelationConstants.FIX_VERSION, version.getGenericValue());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unabled to get fix issues for version " + version, e);
        }
    }

    public Collection<GenericValue> getAffectsIssues(final Version version)
    {
        try
        {
            return issueManager.getIssuesByEntity(IssueRelationConstants.VERSION, version.getGenericValue());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unabled to get affected issues for version " + version, e);
        }
    }

    /**
     * Returns a sorted list of all versions in the project that this version is in.
     *
     * @param version The version used to determine the project.
     * @return a sorted list of all versions in the project that this version is in.
     */
    private List<Version> getAllVersions(final Version version)
    {
        return queryDatabase(EasyMap.build("project", version.getLong("project")));
    }

    private void swapVersions(final String affectsAction, final Long affectsSwapVersionId, final String fixAction, final Long fixSwapVersionId, final Version version)
    {
        if (affectsAction.equalsIgnoreCase(VersionKeys.SWAP_ACTION))
        {
            Version swapAffectsVersion = getVersion(affectsSwapVersionId);
            swapVersionAssociations(version, swapAffectsVersion, IssueRelationConstants.VERSION, Collections.EMPTY_LIST);
        }

        if (fixAction.equalsIgnoreCase(VersionKeys.SWAP_ACTION))
        {
            Version swapFixVersion = getVersion(fixSwapVersionId);
            swapVersionAssociations(version, swapFixVersion, IssueRelationConstants.FIX_VERSION, Collections.EMPTY_LIST);
        }
    }

    /**
     * Swap versions due to a version deletion or release.
     * Deletion - all issues with the deleted version are changed
     * Release - only selected issues with the released version are changed
     */
    private void swapVersionAssociations(final Version swapFromVersion, final Version swapToVersion, final String issueRelation, final List issues)
    {
        try
        {
            if ((swapFromVersion != null) && (swapToVersion != null) && (issueRelation != null) && issues.isEmpty())
            {
                associationManager.swapAssociation("Issue", issueRelation, swapFromVersion.getGenericValue(), swapToVersion.getGenericValue());
            }
            else if ((swapFromVersion != null) && (swapToVersion != null) && (issueRelation != null) && !issues.isEmpty())
            {
                associationManager.swapAssociation(issues, issueRelation, swapFromVersion.getGenericValue(), swapToVersion.getGenericValue());
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Unabled to swapVersionAssociations for versions " + swapFromVersion + " and " + swapToVersion, e);
        }
    }

    /**
     * Checks if a version in the current project already exists with the same name
     */
    public boolean isDuplicateName(final Version currentVersion, final String name, final GenericValue project)
    {
        //Chek to see if there is already a version with that name for the project
        final Collection<Version> versions = getVersions(project);

        for (final Version version : versions)
        {
            if (!currentVersion.getId().equals(version.getLong("id")) && name.trim().equalsIgnoreCase(version.getName()))
            {
                return true;
            }
        }
        return false;
    }

    private void validateDeleteParams(final Version version, final String affectsAction, final Long affectsSwapVersionId, final String fixAction, final Long fixSwapVersionId)
    {

        if (version == null)
        {
            log.error("You must specify a valid version to be deleted.");
            throw new IllegalArgumentException("You must specify a valid version.");
        }

        if (!affectsAction.equalsIgnoreCase(VersionKeys.REMOVE_ACTION) && !affectsAction.equalsIgnoreCase(VersionKeys.SWAP_ACTION))
        {
            log.error("Illegal action specified for issues associated with this affects version.");
            throw new IllegalArgumentException("Illegal action specified for issues associated with this affects version.");
        }

        if (affectsAction.equals(VersionKeys.SWAP_ACTION))
        {
            if (version.getLong("id").equals(affectsSwapVersionId))
            {
                log.error("You cannot move the issues to the version being deleted.");
                throw new IllegalArgumentException("You cannot move the issues to the version being deleted.");
            }
        }

        if (!fixAction.equalsIgnoreCase(VersionKeys.REMOVE_ACTION) && !fixAction.equalsIgnoreCase(VersionKeys.SWAP_ACTION))
        {
            log.error("Illegal action specified for issues associated with this fix version.");
            throw new IllegalArgumentException("Illegal action specified for issues associated with this fix version.");
        }

        if (fixAction.equals(VersionKeys.SWAP_ACTION))
        {
            if (version.getLong("id").equals(fixSwapVersionId))
            {
                log.error("You cannot change the fix version to the version being deleted.");
                throw new IllegalArgumentException("You cannot change the fix version to the version being deleted.");
            }
        }
    }

    private void validateMergeParams(final Version version, final Long mergeToId)
    {
        final GenericValue mergeFromProject = version.getProject();
        final GenericValue mergeToProject = getVersion(mergeToId).getProject();

        if (!getVersions(mergeFromProject).contains(version))
        {
            log.error("The version to merge from is invalid.");
            throw new IllegalArgumentException("The version to merge from is invalid.");
        }

        if ((mergeToId == null) || !getVersions(mergeToProject).contains(getVersion(mergeToId)))
        {
            log.error("You must select a version to merge to.");
            throw new IllegalArgumentException("You must select a version to merge to.");
        }
    }

    private void validateReleaseParams(final Version version, final boolean release)
    {
        if ((version == null) && release)
        {
            log.error("Please select a version to release.");
            throw new IllegalArgumentException("Please select a version to release");
        }
        else if ((version == null) && !release)
        {
            log.error("Please select a version to unrelease.");
            throw new IllegalArgumentException("Please select a version to unrelease.");
        }
    }

    private Version getLastVersion(final Long projectId)
    {
        long maxSequence = 0L;
        Version lastVersion = null;

        for (final Version version : getVersions(projectId))
        {
            if ((version.getSequence() != null) && (version.getSequence() >= maxSequence))
            {
                maxSequence = version.getSequence();
                lastVersion = version;
            }
        }
        return lastVersion;
    }

    private long getMaxVersionSequence(final Long projectId)
    {
        long maxSequence = 1L;

        for (final Version version : getVersions(projectId))
        {
            if ((version.getSequence() != null) && (version.getSequence() >= maxSequence))
            {
                maxSequence = version.getSequence() + 1L;
            }
        }
        return maxSequence;
    }

    /**
     * Given a re-ordered list of versions, commit the changes to the backend datastore.
     */
    public void storeReorderedVersionList(final List<Version> versions)
    {
        final List<Version> versionsChanged = new ArrayList<Version>();

        for (int i = 0; i < versions.size(); i++)
        {
            final Version version = versions.get(i);
            final long expectedSequenceNumber = i + 1L;
            if (expectedSequenceNumber != version.getSequence())
            {
                version.setSequence(expectedSequenceNumber);
                versionsChanged.add(version);
            }
        }

        versionStore.storeVersions(versionsChanged);
    }

    /**
     * For testing purposes.
     * @param version The version used to determine the project.
     */
    void reorderVersionsInProject(final Version version)
    {
        storeReorderedVersionList(getAllVersions(version));
    }

    /**
     * Return a list of {@link Version}s extracted from the database/cache using the specified params.
     *
     * @param params map of parameters
     * @return a list of {@link Version}s extracted from the database/cache using the specified params, never null
     */
    private List<Version> queryDatabase(final Map params)
    {
        final List<GenericValue> versionGvs = EntityUtil.filterByAnd(versionStore.getAllVersions(), params);
        final List<Version> versions = new ArrayList<Version>();

        for (GenericValue versionGV : versionGvs)
        {
            versions.add(new VersionImpl(projectManager, versionGV));
        }
        return versions;
    }
}
