/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserTransformer extends AbstractResultSetTransformer<ExternalUser> {
	private final FogBugzConfigBean configBean;

	public UserTransformer(final FogBugzConfigBean configBean, ImportLogger importLogger) {
		super(importLogger);
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT sFullName, sEmail FROM Person";
	}

	public ExternalUser transform(final ResultSet rs) throws SQLException {
		final String fullName = rs.getString("sFullName");
		final String username = configBean.getUsernameForFullName(fullName);

		if (StringUtils.isBlank(username)) {
			log.warn("Username is blank and is not imported");
			return null;
		}
		return new ExternalUser(username, fullName, rs.getString("sEmail"));
	}
}
