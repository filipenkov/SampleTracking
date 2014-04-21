/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectTransformer extends AbstractProjectTransformer {
	public static final String PROJECT_QUERY_SQL = "SELECT id,name,description FROM mantis_project_table WHERE enabled=true";

	public ProjectTransformer(final MantisConfigBean configBean, ImportLogger log) {
		super(configBean);
	}

	public String getSqlQuery() {
		return PROJECT_QUERY_SQL;
	}

	protected ExternalProject create(final ResultSet rs) throws SQLException {
		final ExternalProject externalProject = new ExternalProject();
		externalProject.setId(rs.getString("id"));
		externalProject.setName(rs.getString("name"));
		externalProject.setDescription(rs.getString("description"));
		return externalProject;
	}

}
