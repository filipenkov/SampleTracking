/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.StatusValueMapper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class StatusValueMappingDefinition implements ValueMappingDefinition {
	private final ValueMappingHelper valueMappingHelper;
	private FogBugzHostedConfigBean configBean;

	public StatusValueMappingDefinition(FogBugzHostedConfigBean configBean, ValueMappingHelper valueMappingHelper) {
		this.configBean = configBean;
		this.valueMappingHelper = valueMappingHelper;
	}

	@Override
	public String getExternalFieldId() {
		return "sStatus";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Set<String> getDistinctValues() {
		try {
			Set<String> statuses = Sets.newHashSet(
					Collections2.transform(configBean.getClient().getStatuses(), new Function<String, String>() {
						@Override
						public String apply(@Nullable String input) {
							return input != null ? StatusValueMapper.getCleanedStatus(input) : null;
						}
					}));

			statuses.add(IssueFieldConstants.CLOSED_STATUS);

			return statuses;
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getJiraFieldId() {
		return IssueFieldConstants.STATUS;
	}

	@Override
	public Collection<ValueMappingEntry> getTargetValues() {
		final JiraWorkflow workflow = valueMappingHelper.getSelectedWorkflow();
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

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
		return new ImmutableList.Builder<ValueMappingEntry>().add(
				new ValueMappingEntry("Active", IssueFieldConstants.OPEN_STATUS_ID),
				new ValueMappingEntry("Pending", IssueFieldConstants.OPEN_STATUS_ID),
				new ValueMappingEntry("Abandoned - No Consensus", IssueFieldConstants.CLOSED_STATUS_ID),
				new ValueMappingEntry("Resolved", IssueFieldConstants.RESOLVED_STATUS_ID),
				new ValueMappingEntry("Approved", IssueFieldConstants.RESOLVED_STATUS_ID),
				new ValueMappingEntry(IssueFieldConstants.CLOSED_STATUS, IssueFieldConstants.CLOSED_STATUS_ID)).build();
	}

	@Override
	public boolean canBeBlank() {
		return false;
	}

	@Override
	public boolean canBeImportedAsIs() {
		return false;
	}

	@Override
	public boolean canBeCustom() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return true;
	}
}
