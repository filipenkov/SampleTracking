/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * An importer provides a basic interface for importing data into JIRA via an
 * {@link com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean} object
 */
public interface JiraDataImporter {

	void setRunning();

	boolean isRunning();

	boolean isAborted();

	void abort(String username);

	String getAbortedBy();

	@NotNull
	Set<ExternalUser> getNonExistentAssociatedUsers();

	void doImport();

	@Nullable
	ImportStats getStats();

	@Nullable
	ImportLogger getLog();

    /**
     * Sets a new data bean. Also cleans the previous import state.
     */
	void setDataBean(ImportDataBean dataBean);

	ImportDataBean getDataBean();

	/**
	 * Clear all state associated with the previous import. Initialize for a new one
	 * (especially create a log).
	 */
	void initializeLog();

	Set<ExternalProject> getSelectedProjects();
}
