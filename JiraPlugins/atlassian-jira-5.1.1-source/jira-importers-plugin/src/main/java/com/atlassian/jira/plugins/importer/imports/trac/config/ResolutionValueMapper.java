/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class ResolutionValueMapper extends AbstractResolutionValueMapper {
	public static final String FIELD = "resolution";

	public ResolutionValueMapper(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext,
			ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext, constantsManager);
	}

	@Override
	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.bugzilla.mappings.value.resolution");
	}

	@Override
	public String getSqlQuery() {
		return "SELECT DISTINCT " + FIELD + " FROM ticket";
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
		return new ImmutableList.Builder<ValueMappingEntry>().add(
			new ValueMappingEntry("wontfix", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
			new ValueMappingEntry("duplicate", IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
			new ValueMappingEntry("invalid", IssueFieldConstants.INCOMPLETE_RESOLUTION_ID),
			new ValueMappingEntry("worksforme", IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID),
			new ValueMappingEntry("fixed", IssueFieldConstants.FIXED_RESOLUTION_ID)).build();
	}
}
