/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.AbstractPriorityValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingEntry;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisFieldConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

public class PriorityToPriorityValueMapper extends AbstractPriorityValueMapper {
    private final AbstractDatabaseConfigBean configBean;
    private final ExternalCustomField customField;

    public PriorityToPriorityValueMapper(AbstractDatabaseConfigBean configBean, JiraAuthenticationContext authenticationContext,
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
		return Sets.newTreeSet(jdbcConnection.queryDb(new PriorityTransformer()));
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_IMMEDIATE, IssueFieldConstants.BLOCKER_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_URGENT, IssueFieldConstants.CRITICAL_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_HIGH, IssueFieldConstants.MAJOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_NORMAL, IssueFieldConstants.MINOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_LOW, IssueFieldConstants.MINOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.PRIORITY_NONE, IssueFieldConstants.TRIVIAL_PRIORITY_ID)).build();
	}

    static class PriorityTransformer implements ResultSetTransformer<String> {
        public String getSqlQuery() {
            return "SELECT DISTINCT priority FROM mantis_bug_table ORDER BY priority";
        }

        public String transform(ResultSet rs) throws SQLException {
            return MantisFieldConstants.getPriorityName(rs.getString(1));
        }
    }
}
