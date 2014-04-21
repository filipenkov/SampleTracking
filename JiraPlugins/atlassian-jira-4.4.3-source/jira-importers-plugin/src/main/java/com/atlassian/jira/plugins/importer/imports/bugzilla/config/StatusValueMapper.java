/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

import com.atlassian.jira.plugins.importer.imports.AbstractStatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class StatusValueMapper extends AbstractStatusValueMapper {
	public static final String FIELD = "bug_status";

	public StatusValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext,
			final ValueMappingHelper mappingHelper) {
		super(jdbcConnection, authenticationContext, mappingHelper);
	}

	public String getExternalFieldId() {
		return FIELD;
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.bugzilla.mappings.value.status");
	}

	@Override
	public String getSqlQuery() {
		return "SELECT DISTINCT " + FIELD + " FROM bugs";
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
        	new ValueMappingEntry("ASSIGNED", "1"),
			new ValueMappingEntry("NEW", "1"),
			new ValueMappingEntry("REOPENED", "4"),
			new ValueMappingEntry("VERIFIED", "5"),
			new ValueMappingEntry("RESOLVED", "5"),
			new ValueMappingEntry("CLOSED", "6")).build();
	}

}
