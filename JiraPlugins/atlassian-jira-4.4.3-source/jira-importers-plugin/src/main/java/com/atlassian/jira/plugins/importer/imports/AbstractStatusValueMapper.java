/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.JiraWorkflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractStatusValueMapper extends AbstractValueMappingDefinition {
	private final ValueMappingHelper mappingHelper;

	public AbstractStatusValueMapper(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext,
			ValueMappingHelper mappingHelper) {
		super(jdbcConnection, authenticationContext);
		this.mappingHelper = mappingHelper;
	}

	@Override
	public final String getJiraFieldId() {
		return IssueFieldConstants.STATUS;
	}

	public Collection<ValueMappingEntry> getTargetValues() {
		final JiraWorkflow workflow = mappingHelper.getSelectedWorkflow();
		if (workflow == null) {
			return Collections.emptyList();
		}
		final List<Status> linkedStatuses = workflow.getLinkedStatusObjects();
		ArrayList<ValueMappingEntry> res = new ArrayList<ValueMappingEntry>(linkedStatuses.size());
		for (Status status : linkedStatuses) {
			res.add(new ValueMappingEntry(status.getName(), status.getId()));
		}
		return res;
	}

	public abstract String getSqlQuery();

	public String transformStatus(String status) {
		return status;
	}

	public Set<String> getDistinctValues() {
		final List<String> distinctComputers = jdbcConnection
				.queryDbAppendCollection(new ResultSetTransformer<Collection<String>>() {
					public String getSqlQuery() {
						return AbstractStatusValueMapper.this.getSqlQuery();
					}

					public Collection<String> transform(final ResultSet rs) throws SQLException {
						final String s = transformStatus(rs.getString(1));
						return MultiSelectCFType.extractTransferObjectFromString(s);
					}
				});
		return new LinkedHashSet<String>(distinctComputers);
	}

	@Override
	public final boolean canBeBlank() {
		return false;
	}

	@Override
	public final boolean canBeImportedAsIs() {
		return false;
	}

	@Override
	public final boolean canBeCustom() {
		return false;
	}

	@Override
	public final boolean isMandatory() {
		return true;
	}
}
