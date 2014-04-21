/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Set;

public class CustomFieldValueMapper extends AbstractValueMappingDefinition {
	private final ExternalCustomField customField;

	protected CustomFieldValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext, ExternalCustomField customField) {
		super(jdbcConnection, authenticationContext);
		this.customField = customField;
	}

	public String getExternalFieldId() {
		return customField.getName();
	}

	@Nullable
	public String getDescription() {
		return null;
	}

	public Set<String> getDistinctValues() {
		return Sets.newHashSet(jdbcConnection.queryDb(new SingleStringResultTransformer(
				"SELECT DISTINCT value FROM mantis_custom_field_string_table WHERE field_id=" + customField.getId())));
	}
}
