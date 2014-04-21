/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalLabelsMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractIssueTransformer<C extends AbstractDatabaseConfigBean>
        extends AbstractResultSetTransformer<ExternalIssue> {

    protected final JdbcConnection jdbcConnection;
    protected final C configBean;

    public AbstractIssueTransformer(C configBean, ImportLogger log) {
        super(log);
        this.configBean = configBean;
        this.jdbcConnection = configBean.getJdbcConnection();
    }

    protected void setCustomFieldValues(ExternalIssue externalIssue, ResultSet rs, String bugId) throws SQLException {

        // Add the custom fields if the field mapping is not null
        final List<ExternalCustomFieldValue> customFields = Lists.newArrayList();
        for (final ExternalCustomField customField : configBean.getCustomFields()) {
            Object value = getCustomFieldValue(rs, bugId, customField);

            if (configBean.isFieldMappedToIssueField(customField.getId())) {
                externalIssue.setField(configBean.getIssueFieldMapping(customField.getId()), value);
                continue;
            }

            final String mapping = configBean.getFieldMapping(customField.getId());
            // map to a specific custom field (if it exists)
            final String customFieldName = StringUtils.isNotEmpty(mapping) ? mapping  : customField.getName();

            customFields.add(new ExternalCustomField(customField.getId(), customFieldName,
					customField.getTypeKey(), customField.getSearcherKey()).createValue(value));
        }

        externalIssue.setExternalCustomFieldValues(customFields);
    }

    @Nullable
	protected abstract Object getCustomFieldValue(ResultSet rs, String bugId, ExternalCustomField customField)
            throws SQLException;

	public static Set<Label> createLabels(Collection<String> labels) {
		final SimpleExternalLabelsMapper.CreateLabelFromString labelFactory = new SimpleExternalLabelsMapper.CreateLabelFromString();
		final Set<Label> result = Sets.newHashSet();
		for(final String label : labels) {
			if (StringUtils.isBlank(label)) {
				continue;
			}

			final String cleanLabel = LabelParser.getCleanLabel(label);
            if (StringUtils.isNotBlank(cleanLabel)) {
                result.add(labelFactory.create(cleanLabel));
            }
		}
		return result;
	}

}
