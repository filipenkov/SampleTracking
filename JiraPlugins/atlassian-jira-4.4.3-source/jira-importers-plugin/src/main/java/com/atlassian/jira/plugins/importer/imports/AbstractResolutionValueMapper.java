/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractResolutionValueMapper extends AbstractValueMappingDefinition {
	private final ConstantsManager constantsManager;

	public AbstractResolutionValueMapper(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext,
			ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext);
		this.constantsManager = constantsManager;
	}

	@Override
	public final String getJiraFieldId() {
		return IssueFieldConstants.RESOLUTION;
	}

	public String transformResolution(String resolution) {
		return resolution;
	}

	public final Set<String> getDistinctValues() {
		final List<String> distinctResolutions = jdbcConnection
				.queryDbAppendCollection(new ResultSetTransformer<Collection<String>>() {
					public String getSqlQuery() {
						return AbstractResolutionValueMapper.this.getSqlQuery();
					}

					public Collection<String> transform(final ResultSet rs) throws SQLException {
						final String s = transformResolution(rs.getString(1));
						return MultiSelectCFType.extractTransferObjectFromString(s);
					}
				});
		return new LinkedHashSet<String>(distinctResolutions);
	}

	protected abstract String getSqlQuery();

	@Override
	public final Collection<ValueMappingEntry> getTargetValues() {
		return getAllResolutions(constantsManager);
	}

	public static Collection<ValueMappingEntry> getAllResolutions(final ConstantsManager constantsManager) {
		return new ArrayList<ValueMappingEntry>(Collections2.transform(constantsManager.getResolutionObjects(),
				new Function<Resolution, ValueMappingEntry>() {
					public ValueMappingEntry apply(Resolution from) {
						return new ValueMappingEntry(from.getName(), from.getId());
					}
				}));
	}

	@Override
	public final boolean canBeBlank() {
		return false;
	}


}
