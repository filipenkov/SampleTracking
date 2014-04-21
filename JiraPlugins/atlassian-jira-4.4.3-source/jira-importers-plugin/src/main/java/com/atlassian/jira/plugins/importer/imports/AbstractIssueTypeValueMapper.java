/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractIssueTypeValueMapper extends AbstractValueMappingDefinition {

	private final ConstantsManager constantsManager;

	protected AbstractIssueTypeValueMapper(final JdbcConnection jdbcConnection,
			final JiraAuthenticationContext authenticationContext, final ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext);
		this.constantsManager = constantsManager;
	}

	public String getJiraFieldId() {
		return IssueFieldConstants.ISSUE_TYPE;
	}

	@Override
	public Collection<ValueMappingEntry> getTargetValues() {
		return new ArrayList<ValueMappingEntry>(Collections2.transform(constantsManager.getRegularIssueTypeObjects(),
				new Function<IssueType, ValueMappingEntry>() {
					public ValueMappingEntry apply(@Nonnull IssueType from) {
						return new ValueMappingEntry(from.getName(), from.getId());
					}
				}));
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
