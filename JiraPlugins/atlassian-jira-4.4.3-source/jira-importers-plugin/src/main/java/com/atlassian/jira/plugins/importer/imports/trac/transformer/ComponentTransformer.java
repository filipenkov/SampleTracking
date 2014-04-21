/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformer extends AbstractResultSetTransformer<ExternalComponent> {
	private final TracConfigBean configBean;

	public ComponentTransformer(final TracConfigBean configBean, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT name, owner, description FROM component WHERE name IS NOT NULL AND name != ''";
	}

	public ExternalComponent transform(final ResultSet rs) throws SQLException {
		ExternalComponent component = new ExternalComponent(rs.getString("name"));
		component.setDescription(rs.getString("description"));
		component.setLead(configBean.getUsernameForEmail(rs.getString("owner")));
		return component;
	}
}
