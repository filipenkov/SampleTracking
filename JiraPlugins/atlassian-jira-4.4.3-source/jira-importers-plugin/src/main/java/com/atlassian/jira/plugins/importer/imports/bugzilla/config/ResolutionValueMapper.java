/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

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
	protected String getSqlQuery() {
		return "SELECT DISTINCT " + FIELD + " FROM bugs";
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
                new ValueMappingEntry("FIXED", IssueFieldConstants.FIXED_RESOLUTION_ID),
                new ValueMappingEntry("WONTFIX", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
                new ValueMappingEntry("DUPLICATE", IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
                new ValueMappingEntry("INVALID", IssueFieldConstants.INCOMPLETE_RESOLUTION_ID),
                new ValueMappingEntry("WORKSFORME", IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID),
                new ValueMappingEntry("NEEDTESTCASE", IssueFieldConstants.INCOMPLETE_RESOLUTION_ID),
                new ValueMappingEntry("LATER", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
                new ValueMappingEntry("REMIND", IssueFieldConstants.WONTFIX_RESOLUTION_ID)).build();
	}

}
