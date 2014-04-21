/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionTransformer extends AbstractResultSetTransformer<ExternalVersion> {
	private final ExternalProject externalProject;

	public VersionTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT version FROM mantis_project_version_table WHERE project_id = "
				+ externalProject.getId() + " AND version IS NOT NULL AND version != ''";
	}

	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		return new ExternalVersion(rs.getString("version"));
	}
}
