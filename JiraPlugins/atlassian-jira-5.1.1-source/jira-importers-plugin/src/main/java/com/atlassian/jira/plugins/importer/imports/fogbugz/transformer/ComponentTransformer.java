/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformer extends AbstractResultSetTransformer<ExternalComponent> {
	private final ExternalProject externalProject;
	private final FogBugzConfigBean configBean;

	public ComponentTransformer(FogBugzConfigBean configBean, final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT a.ixArea, a.sArea, p.sFullName FROM "
				+ "Area a LEFT JOIN Person p ON (a.ixPersonOwner = p.ixPerson) "
				+ "WHERE ixProject = "
				+ externalProject.getId() + " AND sArea IS NOT NULL AND sArea != ''";
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		final String sFullName = rs.getString("sFullName");
		final String lead = sFullName == null ? null : configBean.getUsernameForFullName(sFullName);

		return new ExternalComponent(
				rs.getString("sArea"),
				rs.getString("ixArea"),
				lead,
				null);
	}
}
