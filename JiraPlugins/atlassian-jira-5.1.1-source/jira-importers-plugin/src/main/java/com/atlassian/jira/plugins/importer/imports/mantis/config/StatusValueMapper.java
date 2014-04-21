/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractStatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisFieldConstants;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class StatusValueMapper extends AbstractStatusValueMapper {
	public static final String FIELD = "status";

	public StatusValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext,
			final ValueMappingHelper mappingHelper) {
		super(jdbcConnection, authenticationContext, mappingHelper);
	}

	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mantis.mappings.value.status");
	}

	@Override
	public String getSqlQuery() {
		return "SELECT DISTINCT " + FIELD + " FROM mantis_bug_table";
	}

	@Override
	public String transformStatus(String status) {
		return MantisFieldConstants.getStatusName(status);
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return ImmutableList.<ValueMappingEntry>builder().add(
                new ValueMappingEntry(MantisFieldConstants.STATUS_NEW, IssueFieldConstants.OPEN_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_FEEDBACK, IssueFieldConstants.OPEN_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_ACKNOWLEDGED, IssueFieldConstants.OPEN_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_CONFIRMED, IssueFieldConstants.OPEN_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_ASSIGNED, IssueFieldConstants.INPROGRESS_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_RESOLVED, IssueFieldConstants.RESOLVED_STATUS_ID),
                new ValueMappingEntry(MantisFieldConstants.STATUS_CLOSED, IssueFieldConstants.CLOSED_STATUS_ID)).build();
	}

}
