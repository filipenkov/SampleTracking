/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.transformer.AbstractProjectTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectTransformer extends AbstractProjectTransformer {
	public static final String PROJECT_QUERY_SQL = "SELECT id, name, description FROM products";

	public ProjectTransformer(final BugzillaConfigBean configBean) {
		super(configBean);
	}

	public String getSqlQuery() {
		return PROJECT_QUERY_SQL;
	}

	protected ExternalProject create(final ResultSet rs) throws SQLException {
		ExternalProject project = new ExternalProject();
		project.setName(rs.getString("name"));
		project.setId(rs.getString("id"));
		project.setDescription(rs.getString("description"));
		return project;
	}

}
