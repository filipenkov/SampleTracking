/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractStatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public class StatusValueMapper extends AbstractStatusValueMapper {
	public static final String FIELD = "sStatus";

	public StatusValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext,
			final ValueMappingHelper mappingHelper) {
		super(jdbcConnection, authenticationContext, mappingHelper);
	}

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
		return "SELECT DISTINCT " + FIELD + " FROM Bug, Status WHERE Bug.ixStatus=Status.ixStatus";
	}

	@Override
	public Set<String> getDistinctValues() {
		Set<String> set = Sets.newHashSet(super.getDistinctValues());
		set.add(IssueFieldConstants.CLOSED_STATUS);
		return set;
	}

	@Override
	public String transformStatus(String status) {
		return getCleanedStatus(super.transformStatus(status));
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
        	new ValueMappingEntry("Active", IssueFieldConstants.OPEN_STATUS_ID),
			new ValueMappingEntry("Resolved", IssueFieldConstants.RESOLVED_STATUS_ID),
			new ValueMappingEntry(IssueFieldConstants.CLOSED_STATUS, IssueFieldConstants.CLOSED_STATUS_ID)).build();
	}

	public static String getCleanedStatus(final String fogBugzStatus) {
		int bracket = fogBugzStatus.indexOf("(");
		return StringUtils.trimToEmpty(bracket != -1 ? fogBugzStatus.substring(0, bracket) : fogBugzStatus);
	}

}
