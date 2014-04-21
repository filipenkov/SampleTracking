/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.config;

import com.atlassian.jira.plugins.importer.imports.AbstractStatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

public class StatusValueMapper extends AbstractStatusValueMapper {
	public static final String FIELD = "status";

	public StatusValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext,
			final ValueMappingHelper mappingHelper) {
		super(jdbcConnection, authenticationContext, mappingHelper);
	}

	@Override
	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.bugzilla.mappings.value.status");
	}

	@Override
	public String getSqlQuery() {
		return "SELECT DISTINCT " + FIELD + " FROM ticket";
	}

}