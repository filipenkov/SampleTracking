/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.config;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.AbstractPriorityValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class PriorityToPriorityValueMapper extends AbstractPriorityValueMapper {
    private final AbstractDatabaseConfigBean configBean;
    private final ExternalCustomField customField;

    public PriorityToPriorityValueMapper(AbstractDatabaseConfigBean configBean,
                                         JiraAuthenticationContext authenticationContext,
                                         FieldManager fieldManager, ExternalCustomField customField) {
		super(configBean.getJdbcConnection(), authenticationContext, fieldManager);
        this.configBean = configBean;
        this.customField = customField;
    }

	public String getExternalFieldId() {
		return customField.getId();
	}

    @Nullable
    public String getDescription() {
        return getI18n().getText("jira-importer-plugin.config.mapped.to.issue.field", customField.getName(),
                configBean.getIssueFieldMapping(customField.getId()));
    }

    public Set<String> getDistinctValues() {
		return new LinkedHashSet<String>(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT DISTINCT priority FROM ticket WHERE priority!='' AND priority IS NOT NULL ORDER BY priority")));
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
                new ValueMappingEntry("blocker", IssueFieldConstants.BLOCKER_PRIORITY_ID),
                new ValueMappingEntry("critical", IssueFieldConstants.CRITICAL_PRIORITY_ID),
                new ValueMappingEntry("major", IssueFieldConstants.MAJOR_PRIORITY_ID),
                new ValueMappingEntry("minor", IssueFieldConstants.MINOR_PRIORITY_ID),
                new ValueMappingEntry("trivial", IssueFieldConstants.TRIVIAL_PRIORITY_ID)).build();
	}
}
