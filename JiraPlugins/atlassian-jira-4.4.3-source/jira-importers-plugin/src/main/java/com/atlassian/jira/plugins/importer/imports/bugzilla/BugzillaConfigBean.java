/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.BugzillaValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.imports.bugzilla.transformer.CustomFieldTransformer;
import com.atlassian.jira.plugins.importer.imports.bugzilla.transformer.ProjectTransformer;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BugzillaConfigBean extends AbstractDatabaseConfigBean {

    public static final String BUG_SEVERITY_FIELD = "bug_severity";

    public static final String PRIORITY_FIELD = "priority";

	public static final String DUPLICATES_LINK_NAME = "Duplicates";

	public static final String DEPENDS_LINK_NAME = "Depends on / Blocks";

	private static final String INACTIVE_USERS_GROUP = "bugzilla-import-disabled-users";

	private static final String UNUSED_USERS_GROUP = "bugzilla-import-unused-users";

    private final ExternalUtils utils;

	private final String fielddefsIdColumn;

	public BugzillaConfigBean(JdbcConnection jdbcConnection,
			ExternalUtils utils) {
		super(jdbcConnection, utils);
		this.utils = utils;

		try {
			fielddefsIdColumn = SqlUtils.getColumnNames(jdbcConnection.getConnection(), "fielddefs").contains("fieldid")
					? "fieldid" : "id";
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public void initializeValueMappingHelper() {
        ValueMappingDefinitionsFactory mappingDefinitionFactory = new BugzillaValueMappingDefinitionsFactory(
				utils.getAuthenticationContext(),
				utils.getConstantsManager(),
				this, utils.getFieldManager());

		valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(), mappingDefinitionFactory, utils.getConstantsManager());
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

    @Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField) {
        Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();

        if (BUG_SEVERITY_FIELD.equals(customField.getId()) || PRIORITY_FIELD.equals(customField.getId())) {
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

    public List<ExternalCustomField> getCustomFields() {
		final List<ExternalCustomField> fields = Lists.newArrayList(
				new ExternalCustomField("bug_url", DefaultJiraDataImporter.EXTERNAL_ISSUE_URL,
					CustomFieldConstants.URL_FIELD_TYPE, CustomFieldConstants.EXACT_TEXT_SEARCHER),
                ExternalCustomField.createSelect(BUG_SEVERITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.severity.name")),
                ExternalCustomField.createSelect(PRIORITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.priority.name")),
                ExternalCustomField.createText("status_whiteboard",
                        getI18n().getText("jira-importer-plugin.external.bugzilla.status_whiteboard")));

		fields.addAll(jdbcConnection.queryDb(new CustomFieldTransformer()));
		
		return fields;
	}

	public List<String> getLinkNamesFromDb() {
        return new ImmutableList.Builder<String>()
                .add(DUPLICATES_LINK_NAME, DEPENDS_LINK_NAME).build();
	}

	public String getUsernameForLoginName(@Nullable String loginName) {
		String username = getValueMappingHelper().getValueMappingForImport(LoginNameValueMapper.FIELD,
				StringUtils.defaultIfEmpty(loginName, "deleted"));
		return username.toLowerCase(Locale.ENGLISH);
	}

	public String getInactiveUsersGroup() {
		return INACTIVE_USERS_GROUP;
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
	}

    @Override
    public void populateFieldMappings(Map actionParams, ErrorCollection errors) {
        super.populateFieldMappings(actionParams, errors);

        if (fieldMapping.get(BUG_SEVERITY_FIELD) != null
				&& fieldMapping.get(BUG_SEVERITY_FIELD).equals(fieldMapping.get(PRIORITY_FIELD))) {
            errors.addError(BUG_SEVERITY_FIELD,
                    getI18n().getText("jira-importer-plugin.config.mappings.duplicate.custom.fields",
                            BUG_SEVERITY_FIELD, PRIORITY_FIELD));
            errors.addError(PRIORITY_FIELD,
                    getI18n().getText("jira-importer-plugin.config.mappings.duplicate.custom.fields",
                            BUG_SEVERITY_FIELD, PRIORITY_FIELD));
        }
    }

	public String getFielddefsIdColumn() {
		return fielddefsIdColumn;
	}


}

