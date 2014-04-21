/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.PrioritySystemField;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractPriorityValueMapper extends AbstractValueMappingDefinition {

	private final FieldManager fieldManager;

	protected AbstractPriorityValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext, final FieldManager fieldManager) {
		super(jdbcConnection, authenticationContext);
		this.fieldManager = fieldManager;
	}

	public String getJiraFieldId() {
		return IssueFieldConstants.PRIORITY;
	}

	public Collection<ValueMappingEntry> getTargetValues() {
		final OrderableField field = fieldManager.getOrderableField(getJiraFieldId());
		if (field instanceof PrioritySystemField) {
			PrioritySystemField prioritySystemField = (PrioritySystemField) field;
			@SuppressWarnings("unchecked")
			final Collection<Priority> priorities = prioritySystemField.getIssueConstants();
			ArrayList<ValueMappingEntry> res = new ArrayList<ValueMappingEntry>(priorities.size());
			for (Priority priority : priorities) {
				res.add(new ValueMappingEntry(priority.getName(), priority.getId()));
			}
			return res;
		}
		return null;
	}

	@Override
	public boolean canBeBlank() {
		return false;
	}

	@Override
	public boolean canBeCustom() {
		return true;
	}

	@Override
	public boolean canBeImportedAsIs() {
		return true;
	}
}
