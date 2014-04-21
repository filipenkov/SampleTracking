/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

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
		return "SELECT value FROM versions WHERE product_id=" + externalProject.getId()
				+ " AND value IS NOT NULL AND value != '' ORDER BY value";
	}

	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		return new ExternalVersion(rs.getString("value"));
	}
}
