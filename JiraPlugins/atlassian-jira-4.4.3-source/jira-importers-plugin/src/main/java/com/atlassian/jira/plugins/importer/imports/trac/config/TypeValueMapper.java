/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTypeValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class TypeValueMapper extends AbstractIssueTypeValueMapper {
	public static final String FIELD = "type";

	public TypeValueMapper(final JdbcConnection jdbcConnection,
			JiraAuthenticationContext authenticationContext, ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext, constantsManager);
	}

	@Override
	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.trac.mappings.value.types");
	}

	@Override
	public Set<String> getDistinctValues() {
		return new LinkedHashSet<String>(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT DISTINCT type FROM ticket ORDER BY type")));
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
		return new ImmutableList.Builder<ValueMappingEntry>().add(
			new ValueMappingEntry("defect", IssueFieldConstants.BUG_TYPE_ID),
			new ValueMappingEntry("enhancement", IssueFieldConstants.NEWFEATURE_TYPE_ID),
			new ValueMappingEntry("task", IssueFieldConstants.TASK_TYPE_ID)).build();
	}
}