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

public class ComponentTransformerVer118OrOlder extends AbstractResultSetTransformer<ExternalComponent> {
	private final MantisConfigBean configBean;
	private final ExternalProject externalProject;

	public ComponentTransformerVer118OrOlder(final MantisConfigBean configBean,
			final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT category,(SELECT username FROM mantis_user_table WHERE id=c.user_id) AS username"
			+ " FROM mantis_project_category_table AS c WHERE project_id=" + externalProject.getId()
			+ " UNION SELECT category,NULL FROM mantis_bug_table AS b"
			+ " WHERE NOT EXISTS(SELECT 1 FROM mantis_project_category_table AS c WHERE c.category=b.category) AND b.project_id=" + externalProject.getId();
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		final String componentName = rs.getString("category");

		if (StringUtils.isBlank(componentName)) {
			return null;
		}

		return new ExternalComponent(componentName, null,
				configBean.getUsernameForLoginName(rs.getString("username")), null);
	}
}
