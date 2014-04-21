/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class LoginNameValueMapper extends AbstractValueMappingDefinition {
	public static final String FIELD = "username";

	public LoginNameValueMapper(JdbcConnection jdbcConnection, JiraAuthenticationContext authenticationContext) {
		super(jdbcConnection, authenticationContext);
	}

	public String getExternalFieldId() {
		return FIELD;
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mappings.value.login.name");
	}

	public Set<String> getDistinctValues() {
		return new LinkedHashSet<String>(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT username FROM mantis_user_table ORDER BY username")));
	}
}
