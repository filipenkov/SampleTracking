/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;

public class ResolutionValueMapper extends AbstractResolutionValueMapper {
	public static final String FIELD = "sStatus (Resolution)";

	public ResolutionValueMapper(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext,
			ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext, constantsManager);
	}

	@Override
	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	@Nullable
	public String getDescription() {
		return null;
	}

	@Override
	public String getSqlQuery() {
		return "SELECT DISTINCT sStatus FROM Bug, Status WHERE Bug.ixStatus=Status.ixStatus";
	}

	@Override
	@Nullable
	public String transformResolution(String status) {
		return getCleanedResolution(super.transformResolution(status));
	}

	@Nullable
	public static String getCleanedResolution(final String fogBugzStatus) {
		int bs = fogBugzStatus.indexOf("("), be = fogBugzStatus.indexOf(")", bs);

		return (bs != -1 && be != -1 && bs < be) ? fogBugzStatus.substring(bs + 1, be) : null;
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
                new ValueMappingEntry("Fixed", IssueFieldConstants.FIXED_RESOLUTION_ID),
                new ValueMappingEntry("By Desing", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
                new ValueMappingEntry("Duplicate", IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
                new ValueMappingEntry("Won't Fix", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
                new ValueMappingEntry("Not Reproducible", IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID),
                new ValueMappingEntry("Postponed", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Implemented", IssueFieldConstants.FIXED_RESOLUTION_ID)).build();
	}

}
