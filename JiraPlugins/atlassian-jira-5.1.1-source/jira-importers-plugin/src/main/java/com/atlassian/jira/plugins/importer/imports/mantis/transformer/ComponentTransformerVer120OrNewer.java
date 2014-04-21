/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformerVer120OrNewer extends AbstractResultSetTransformer<ExternalComponent> {
	private final MantisConfigBean configBean;
	private final ExternalProject externalProject;

	public ComponentTransformerVer120OrNewer(final MantisConfigBean configBean,
			final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT name,(SELECT username FROM mantis_user_table WHERE id=c.user_id) AS username"
			+ " FROM mantis_category_table AS c WHERE project_id=" + externalProject.getId()
			+ " OR (EXISTS(SELECT 1 FROM mantis_project_table WHERE id=" + externalProject.getId()
			+ " AND inherit_global=1) AND c.project_id=0)";
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		final String componentName = rs.getString("name");

		if (StringUtils.isBlank(componentName)) {
			return null;
		}

		return new ExternalComponent(componentName, null,
				configBean.getUsernameForLoginName(rs.getString("username")), null);
	}
}
