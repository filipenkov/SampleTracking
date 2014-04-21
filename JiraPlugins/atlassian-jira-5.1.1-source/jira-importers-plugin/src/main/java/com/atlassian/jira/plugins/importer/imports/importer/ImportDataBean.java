/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.sample.Callbacks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Interface encapsulating the data to be imported.
 */
public abstract class ImportDataBean {

	/**
	 * Gets a Collection of all users that are required by import to succeed. All accounts that actually used
	 * in the tracker (like reporters, commenters, and so on).
	 *
	 * @return a Collection {@link com.atlassian.jira.plugins.importer.external.beans.ExternalUser} objects
	 */
	public abstract Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger);

	/**
	 * Gets a Collection of all users that are registered in the external bugtracker.
	 *
	 * @return a Collection {@link com.atlassian.jira.plugins.importer.external.beans.ExternalUser} objects
	 */
	public abstract Set<ExternalUser> getAllUsers(ImportLogger log);

	/**
	 * @return A Collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalProject} objects.
	 */
	public abstract Set<ExternalProject> getAllProjects(ImportLogger log);

	/**
	 * Get projects that were selected by the user. Selection taken from {@link com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean}
	 */
	public abstract Set<ExternalProject> getSelectedProjects(ImportLogger log);

	/**
	 * Gets all versions associated with the project. Even ones that are not actively used.
	 * 
	 * @param externalProject that contains versions
	 * @param importLogger
	 * @return a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalVersion} objects
	 */
	public abstract Collection<ExternalVersion> getVersions(ExternalProject externalProject, ImportLogger importLogger);

	/**
	 * Gets all custom field definitions so we can import them to JIRA.
	 */
	public Collection<ExternalCustomField> getCustomFields() {
		return Collections.emptyList();
	}

	/**
	 * Gets all components associated with the project. Even ones that are not actively used.
	 *
	 * @param externalProject containing components
	 * @param importLogger
	 * @return a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalComponent}
	 */
	public abstract Collection<ExternalComponent> getComponents(ExternalProject externalProject, ImportLogger importLogger);

	/**
	 * Gets a collection of externalissues being imported for a project
	 *
	 * @param externalProject of the issue
	 * @param importLogger
	 * @return iterator over a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalIssue} objects
	 */
	public abstract Iterator<ExternalIssue> getIssuesIterator(ExternalProject externalProject, ImportLogger importLogger);

	public abstract void cleanUp();

	public Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log) {
        return Collections.emptyList();
    }

	@Nullable
	public abstract String getIssueKeyRegex();

	/**
	 * get related and linked issues
	 *
	 * @return as a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalLink} objects
	 */
	public abstract Collection<ExternalLink> getLinks(ImportLogger log);

	/**
	 * @return the total number of issues to be imported
	 */
	public abstract long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log);

	/**
	 *
	 * @return group name that unused users will be added to
	 */
	public abstract String getUnusedUsersGroup();

	/**
	 * Return external system URL, if issues have external issue URL set it must starts exactly with external system URL.
	 */
	@Nullable
	public String getExternalSystemUrl() {
		return null;
	}

	/**
	 * Get HTML with a return link
	 */
	public abstract String getReturnLinks();

    @Nonnull
    public Callbacks getCallbacks() {
        return new Callbacks();
    }
}
