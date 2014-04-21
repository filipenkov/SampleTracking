/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformer extends AbstractResultSetTransformer<ExternalComponent> {
	private final ExternalProject externalProject;

	public ComponentTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT ixArea, sArea FROM Area WHERE ixProject = "
				+ externalProject.getId() + " AND sArea IS NOT NULL AND sArea != ''";
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		return new ExternalComponent(rs.getString("sArea"));
	}
}
