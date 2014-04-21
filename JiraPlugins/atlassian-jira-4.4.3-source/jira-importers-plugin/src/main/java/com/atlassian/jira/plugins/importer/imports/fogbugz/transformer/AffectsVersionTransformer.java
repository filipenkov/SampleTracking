/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AffectsVersionTransformer extends AbstractResultSetTransformer<ExternalVersion> {
	private final ExternalProject externalProject;

	public AffectsVersionTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT DISTINCT sVersion FROM Bug WHERE ixProject = " + externalProject.getId()
				+ " AND sVersion IS NOT NULL AND sVersion != '' ORDER BY sVersion";
	}

	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		return new ExternalVersion(rs.getString("sVersion"));
	}
}
