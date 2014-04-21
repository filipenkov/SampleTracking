/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectTransformer extends AbstractProjectTransformer {
	public static final String PROJECT_QUERY_SQL =
			"SELECT ixProject, sProject, sFullName FROM Person, Project WHERE ixPersonOwner = ixPerson";

	public ProjectTransformer(final FogBugzConfigBean configBean) {
		super(configBean);
	}

	public String getSqlQuery() {
		return PROJECT_QUERY_SQL;
	}

	protected ExternalProject create(final ResultSet rs) throws SQLException {
		final ExternalProject externalProject = new ExternalProject();
		externalProject.setId(rs.getString("ixProject"));
		externalProject.setName(rs.getString("sProject"));
		return externalProject;
	}

}
