/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.mantis.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.imports.mantis.config.MantisValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.CustomFieldTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MantisConfigBean extends AbstractDatabaseConfigBean {
	public static final String RELATED_TO_LINK_NAME = "Related to";

	public static final String DUPLICATE_LINK_NAME = "Duplicate of / Has duplicate";

	public static final String PARENT_LINK_NAME = "Parent of / Child of";

	private static final String INACTIVE_USERS_GROUP = "mantis-import-disabled-users";

	private static final String UNUSED_USERS_GROUP = "mantis-import-unused-users";

	private final ExternalUtils utils;

    public static final String SEVERITY_FIELD = "severity";

    public static final String PRIORITY_FIELD = "priority";

	private final Map<String, Boolean> columnIsTimestamp = Maps.newHashMap();

    public MantisConfigBean(JdbcConnection jdbcConnection, ExternalUtils utils) {
		super(jdbcConnection, utils);
		this.utils = utils;
	}

    @Override
    public void initializeValueMappingHelper() {
        valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(),
				new MantisValueMappingDefinitionsFactory(utils.getAuthenticationContext(),
						utils.getConstantsManager(), this, utils.getFieldManager()), utils.getConstantsManager());
    }

	@Override
	public void validateJustBeforeImport(ErrorCollection errors) {
		validateWorkflowSchemes(errors);
	}

	public List<String> getProjectNamesFromDb() {
		return getJdbcConnection().queryDb(new ResultSetTransformer<String>() {
			public String getSqlQuery() {
				return ProjectTransformer.PROJECT_QUERY_SQL;
			}

			public String transform(ResultSet rs) throws SQLException {
				return rs.getString("name");
			}
		});
	}

	public List<ExternalCustomField> getCustomFields() {
		final List<ExternalCustomField> fields = Lists.newArrayList(
				new ExternalCustomField("bug_url", DefaultJiraDataImporter.EXTERNAL_ISSUE_URL,
						CustomFieldConstants.URL_FIELD_TYPE, CustomFieldConstants.EXACT_TEXT_SEARCHER),
				ExternalCustomField.createFreeText("steps_to_reproduce",
						utils.getAuthenticationContext().getI18nHelper()
								.getText("jira-importer-plugin.external.mantis.steps_to_reproduce")),
				ExternalCustomField.createFreeText("additional_information",
						utils.getAuthenticationContext().getI18nHelper()
								.getText("jira-importer-plugin.external.mantis.additional_information")),
                ExternalCustomField.createSelect(SEVERITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.severity.name")),
                ExternalCustomField.createSelect(PRIORITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.priority.name"))
		);

		fields.addAll(jdbcConnection.queryDb(new CustomFieldTransformer()));

		return fields;
	}

	public List<String> getLinkNamesFromDb() {
		return Lists.newArrayList(RELATED_TO_LINK_NAME, DUPLICATE_LINK_NAME, PARENT_LINK_NAME);
	}

	@Nullable
	public String getUsernameForLoginName(@Nullable String loginName) {
		if (loginName == null) {
			return null;
		}
		return getValueMappingHelper().getValueMappingForImport(LoginNameValueMapper.FIELD, loginName);
	}

	public String getInactiveUsersGroup() {
		return INACTIVE_USERS_GROUP;
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}

    @Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField) {
        Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();

        if (SEVERITY_FIELD.equals(customField.getId()) || PRIORITY_FIELD.equals(customField.getId())) {
            fieldMappings.put(getI18n().getText("admin.csv.import.mappings.issue.fields.header"),
                    MapBuilder.<String, String>newBuilder().add(
                            mapToIssueFieldValue(IssueFieldConstants.PRIORITY),
                            getI18n().getText("issue.field.priority")).toMap());
        }

        fieldMappings.put(getI18n().getText("admin.csv.import.mappings.custom.fields.header"),
                getAvailableCustomFieldMappings(customField));

        fieldMappings.putAll(super.getAvailableFieldMappings(customField));
        return fieldMappings;
    }

    @Override
    public void populateFieldMappings(Map actionParams, ErrorCollection errors) {
        super.populateFieldMappings(actionParams, errors);
        if (fieldMapping.get(SEVERITY_FIELD).equals(fieldMapping.get(PRIORITY_FIELD))) {
            errors.addError(SEVERITY_FIELD,
                    getI18n().getText("jira-importer-plugin.config.mappings.duplicate.custom.fields",
                            SEVERITY_FIELD, PRIORITY_FIELD));
            errors.addError(PRIORITY_FIELD,
                    getI18n().getText("jira-importer-plugin.config.mappings.duplicate.custom.fields",
                            SEVERITY_FIELD, PRIORITY_FIELD));
        }
    }

	@Nullable
    public Date getTimestamp(ResultSet rs, String column) throws SQLException {
		if (!columnIsTimestamp.containsKey(column)) {
			final List<String> columns = SqlUtils.getColumnNames(rs.getMetaData());
			final int idx = columns.indexOf(column) + 1;
			final int type = rs.getMetaData().getColumnType(idx);

		   columnIsTimestamp.put(column, type == Types.TIMESTAMP || type == Types.TIME || type == Types.DATE);
		}

        if (columnIsTimestamp.get(column)) {
            return rs.getTimestamp(column);
        } else {
            final Date result = new Date(rs.getLong(column) * 1000);
			return rs.wasNull() ? null : result;
        }
    }
}

