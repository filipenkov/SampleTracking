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
import com.atlassian.jira.plugins.importer.imports.mantis.MantisFieldConstants;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

public class SeverityToPriorityValueMapper extends AbstractPriorityValueMapper {
	private final ExternalCustomField customField;

    public SeverityToPriorityValueMapper(JdbcConnection jdbcConnection, JiraAuthenticationContext authenticationContext,
                                         FieldManager fieldManager, ExternalCustomField customField) {
		super(jdbcConnection, authenticationContext, fieldManager);
        this.customField = customField;
    }

	public String getExternalFieldId() {
		return customField.getId();
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mantis.mappings.value.severity");
	}

	public Set<String> getDistinctValues() {
		return Sets.newTreeSet(jdbcConnection.queryDb(new SeverityTransformer()));
	}

	@Override
	public Collection<ValueMappingEntry> getDefaultValues() {
        return new ImmutableList.Builder<ValueMappingEntry>().add(
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_BLOCK, IssueFieldConstants.BLOCKER_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_CRASH, IssueFieldConstants.CRITICAL_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_FEATURE, IssueFieldConstants.MAJOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_MAJOR, IssueFieldConstants.MAJOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_MINOR, IssueFieldConstants.MINOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_TWEAK, IssueFieldConstants.MINOR_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_TEXT, IssueFieldConstants.TRIVIAL_PRIORITY_ID),
			new ValueMappingEntry(MantisFieldConstants.SEVERITY_TRIVIAL, IssueFieldConstants.TRIVIAL_PRIORITY_ID)).build();
	}

    static class SeverityTransformer implements ResultSetTransformer<String> {
        public String getSqlQuery() {
            return "SELECT DISTINCT severity FROM mantis_bug_table ORDER BY severity";
        }

        public String transform(ResultSet rs) throws SQLException {
            return MantisFieldConstants.getSeverityName(rs.getString(1));
        }
    }
}
