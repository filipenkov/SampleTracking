/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisFieldConstants;
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
		return "SELECT DISTINCT " + FIELD + " FROM mantis_bug_table WHERE "
				+ FIELD + "!=" + MantisFieldConstants.RESOLUTION_OPEN_ID
				+ " AND " + FIELD + "!=" + MantisFieldConstants.RESOLUTION_REOPENED_ID;
	}

	@Override
	public String transformResolution(String resolution) {
		return MantisFieldConstants.getResolutionName(resolution);
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
                //new ValueMappingEntry(MantisFieldConstants.RESOLUTION_OPEN, ""),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_FIXED, IssueFieldConstants.FIXED_RESOLUTION_ID),
                //new ValueMappingEntry(MantisFieldConstants.RESOLUTION_REOPENED, ""),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_UNABLE_TO_REPRODUCE, IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_NOT_FIXABLE, IssueFieldConstants.WONTFIX_RESOLUTION_ID),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_DUPLICATE, IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_NO_CHANGE_REQUIRED, ""),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_SUSPENDED, ""),
                new ValueMappingEntry(MantisFieldConstants.RESOLUTION_WONT_FIX, IssueFieldConstants.WONTFIX_RESOLUTION_ID)).build(); // won't fix
	}

}
