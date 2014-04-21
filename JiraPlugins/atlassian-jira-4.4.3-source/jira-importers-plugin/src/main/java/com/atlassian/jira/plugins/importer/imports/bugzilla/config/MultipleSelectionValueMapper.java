/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Sets;

import java.util.Set;

public class MultipleSelectionValueMapper extends AbstractValueMappingDefinition {
	private final ExternalCustomField customField;

	protected MultipleSelectionValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext, ExternalCustomField customField) {
		super(jdbcConnection, authenticationContext);
		this.customField = customField;
	}

	public String getExternalFieldId() {
		return customField.getId();
	}

	public String getDescription() {
		return customField.getName();
	}

	public Set<String> getDistinctValues() {
		return Sets.newHashSet(jdbcConnection.queryDb(new SingleStringResultTransformer(
				"SELECT DISTINCT value FROM bug_" + customField.getId())));
	}
}
