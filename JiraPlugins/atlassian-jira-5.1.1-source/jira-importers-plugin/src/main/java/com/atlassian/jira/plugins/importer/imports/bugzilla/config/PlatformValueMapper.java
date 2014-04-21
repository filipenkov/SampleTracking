/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlatformValueMapper extends AbstractValueMappingDefinition {
	private static final String COMPUTER_FIELD = "rep_platform";

	public PlatformValueMapper(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext) {
		super(jdbcConnection, authenticationContext);
	}

	public String getExternalFieldId() {
		return COMPUTER_FIELD;
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.bugzilla.mappings.value.platform");
	}

	public Set<String> getDistinctValues() {
		final List<String> distinctComputers = jdbcConnection
				.queryDbAppendCollection(new ResultSetTransformer<Collection<String>>() {
					public String getSqlQuery() {
						return "SELECT DISTINCT " + COMPUTER_FIELD + " FROM bugs";
					}

					public Collection<String> transform(final ResultSet rs) throws SQLException {
						final String s = rs.getString(1);
						return MultiSelectCFType.extractTransferObjectFromString(s);
					}
				});
		return new LinkedHashSet<String>(distinctComputers);
	}
}
