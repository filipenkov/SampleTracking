/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class FullNameValueMapper extends AbstractValueMappingDefinition {
	private static final String USER_FIELD = "sFullName";

	public FullNameValueMapper(JdbcConnection jdbcConnection, JiraAuthenticationContext authenticationContext) {
		super(jdbcConnection, authenticationContext);
	}

	public String getExternalFieldId() {
		return USER_FIELD;
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.external.fogbugz.mappings.value.fullname");
	}

	public Set<String> getDistinctValues() {
		return new LinkedHashSet<String>(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT sFullName FROM Person ORDER BY sFullName")));
	}
}
