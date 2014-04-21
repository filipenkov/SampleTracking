/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.imports.AbstractResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.ResolutionValueMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

class ResolutionValueMappingDefinition implements ValueMappingDefinition {
	private FogBugzHostedConfigBean configBean;
	private ConstantsManager constantsManager;

	public ResolutionValueMappingDefinition(FogBugzHostedConfigBean configBean, ConstantsManager constantsManager) {
		this.configBean = configBean;
		this.constantsManager = constantsManager;
	}

	@Override
	public String getExternalFieldId() {
		return "sStatus (Resolution)";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Set<String> getDistinctValues() {
		try {
			Set<String> statuses = Sets.newHashSet(
					Collections2.filter(
							Collections2
									.transform(configBean.getClient().getStatuses(), new Function<String, String>() {
										@Override
										public String apply(@Nullable String input) {
											return input != null ? ResolutionValueMapper.getCleanedResolution(input)
													: null;
										}
									}), new Predicate<String>() {
						@Override
						public boolean apply(@Nullable String input) {
							return StringUtils.isNotBlank(input);
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
		return IssueFieldConstants.RESOLUTION;
	}

	@Override
	public Collection<ValueMappingEntry> getTargetValues() {
		return AbstractResolutionValueMapper.getAllResolutions(constantsManager);
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
		return new ImmutableList.Builder<ValueMappingEntry>().add(
				new ValueMappingEntry("Fixed", IssueFieldConstants.FIXED_RESOLUTION_ID),
				new ValueMappingEntry("By Desing", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Duplicate", IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
				new ValueMappingEntry("Completed", IssueFieldConstants.FIXED_RESOLUTION_ID),
				new ValueMappingEntry("Already Exists", IssueFieldConstants.DUPLICATE_RESOLUTION_ID),
				new ValueMappingEntry("Won't Fix", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Won't Implement", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Won't Respond", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Closed", IssueFieldConstants.FIXED_RESOLUTION_ID),
				new ValueMappingEntry("Not Reproducible", IssueFieldConstants.CANNOTREPRODUCE_RESOLUTION_ID),
				new ValueMappingEntry("Postponed", IssueFieldConstants.WONTFIX_RESOLUTION_ID),
				new ValueMappingEntry("Waiting For Info", IssueFieldConstants.INCOMPLETE_RESOLUTION_ID),
				new ValueMappingEntry("Implemented", IssueFieldConstants.FIXED_RESOLUTION_ID)).build();
	}

	@Override
	public boolean canBeBlank() {
		return false;
	}

	@Override
	public boolean canBeImportedAsIs() {
		return true;
	}

	@Override
	public boolean canBeCustom() {
		return true;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}
}
