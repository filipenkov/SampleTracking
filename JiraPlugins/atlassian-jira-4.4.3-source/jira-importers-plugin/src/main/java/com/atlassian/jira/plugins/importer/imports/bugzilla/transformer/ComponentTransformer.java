/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformer extends AbstractResultSetTransformer<ExternalComponent> {
	private final BugzillaConfigBean configBean;
	private final ExternalProject externalProject;

	public ComponentTransformer(final BugzillaConfigBean configBean,
			final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT name, login_name, description FROM components JOIN profiles ON (components.initialowner = profiles.userid)"
			+" WHERE product_id= " + externalProject.getId();
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		final String componentName = rs.getString("name");

		if (StringUtils.isBlank(componentName)) {
			return null;
		}

		ExternalComponent component = new ExternalComponent(componentName);
		component.setDescription(rs.getString("description"));
		component.setLead(configBean.getUsernameForLoginName(rs.getString("login_name")));
		return component;
	}
}
