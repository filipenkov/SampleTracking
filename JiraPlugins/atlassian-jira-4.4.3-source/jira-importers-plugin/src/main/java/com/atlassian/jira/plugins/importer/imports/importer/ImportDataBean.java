/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.project.Project;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Interface encapsulating the data to be imported.
 */
public interface ImportDataBean {

	/**
	 * Gets a Collection of all users that are required by import to succeed. All accounts that actually used
	 * in the tracker (like reporters, commenters, and so on).
	 *
	 * @return a Collection {@link com.atlassian.jira.plugins.importer.external.beans.ExternalUser} objects
	 */
	Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger);

	/**
	 * Gets a Collection of all users that are registered in the external bugtracker.
	 *
	 * @return a Collection {@link com.atlassian.jira.plugins.importer.external.beans.ExternalUser} objects
	 */
	Set<ExternalUser> getAllUsers(ImportLogger log);

	/**
	 * @return A Collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalProject} objects.
	 */
	Set<ExternalProject> getAllProjects(ImportLogger log);

	/**
	 * Get projects that were selected by the user. Selection taken from {@link com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean}
	 */
	Set<ExternalProject> getSelectedProjects(ImportLogger log);

	/**
	 * Gets all versions associated with the project. Even ones that are not actively used.
	 * 
	 * @param externalProject that contains versions
	 * @param importLogger
	 * @return a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalVersion} objects
	 */
	Collection<ExternalVersion> getVersions(ExternalProject externalProject, ImportLogger importLogger);

	/**
	 * Gets all global custom field values so we can import them to JIRA.
	 */
	Collection<ExternalCustomFieldValue> getGlobalCustomFields();

	/**
	 * Gets all components associated with the project. Even ones that are not actively used.
	 *
	 * @param externalProject containing components
	 * @param importLogger
	 * @return a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalComponent}
	 */
	Collection<ExternalComponent> getComponents(ExternalProject externalProject, ImportLogger importLogger);

	/**
	 * Gets a collection of externalissues being imported for a project
	 *
	 * @param externalProject of the issue
	 * @param importLogger
	 * @return iterator over a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalIssue} objects
	 */
	Iterator<ExternalIssue> getIssuesIterator(ExternalProject externalProject, ImportLogger importLogger);

	void cleanUp();

	Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log);

	@Nullable
	String getIssueKeyRegex();

	/**
	 * get related and linked issues
	 *
	 * @return as a collection of {@link com.atlassian.jira.plugins.importer.external.beans.ExternalLink} objects
	 */
	Collection<ExternalLink> getLinks(ImportLogger log);

	/**
	 * @return the total number of issues to be imported
	 */
	long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log);

	/**
	 *
	 * @return group name that unused users will be added to
	 */
	String getUnusedUsersGroup();

	/**
	 * Perform any additional set up required after the Project has been created
	 * @param externalProject external project being imported
	 * @param project JIRA project that the data will be imported to
	 * @param importLogger import logger
	 */
	void afterProjectCreated(ExternalProject externalProject, Project project, ImportLogger importLogger);

	/**
	 * Return external system URL, if issues have external issue URL set it must starts exactly with external system URL.
	 */
	@Nullable
	String getExternalSystemUrl();
}
