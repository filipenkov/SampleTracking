/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.AbstractSelectFieldValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.imports.trac.config.TracValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TracConfigBean extends AbstractDatabaseConfigBean {

    public static final String SEVERITY_FIELD = "severity";

    public static final String PRIORITY_FIELD = "priority";

	public static final String MILESTONE_FIELD = "milestone";

	private static final String UNUSED_USERS_GROUP = "trac-import-unused-users";

	private final ExternalUtils utils;
	private final Configuration environmentConfiguration;
	private final File environmentZip;

	public TracConfigBean(JdbcConnection jdbcConnection, ExternalUtils utils,
			File environmentZip) throws IOException, ConfigurationException {
		super(jdbcConnection, utils);
		this.environmentZip = environmentZip;

		final ZipFile zip = new ZipFile(environmentZip);
		try {
			this.environmentConfiguration = TracImporterController.getEnvironmentConfiguration(zip);
		} finally {
			zip.close();
		}
		this.utils = utils;
	}

    @Override
    public void initializeValueMappingHelper() {
        final ValueMappingDefinitionsFactory mappingDefinitionFactory = new TracValueMappingDefinitionsFactory(
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
		return Lists.newArrayList(environmentConfiguration.getString("descr"));
	}

    @Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField) {
        final Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();

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

    public List<ExternalCustomField> getCustomFields() {
		final List<ExternalCustomField> fields = Lists.newArrayList(
                ExternalCustomField.createSelect(SEVERITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.severity.name")),
                ExternalCustomField.createSelect(PRIORITY_FIELD,
                        getI18n().getText("jira-importer-plugin.customfield.priority.name")),
				ExternalCustomField.createSelect(MILESTONE_FIELD,
						getI18n().getText("jira-importer-plugin.customfield.milestone.name")));

		@SuppressWarnings({"unchecked"})
		final Set<String> configKeys = Sets.newHashSet(environmentConfiguration.getKeys());
		for(String key : configKeys) {
			if (!configKeys.contains(key + ".label")) {
				continue; // each custom field needs to have at least label
			}

			final String label = environmentConfiguration.getString(key + ".label");
			final String type = environmentConfiguration.getString(key);
			if ("textarea".equals(type)) {
				fields.add(ExternalCustomField.createFreeText(key, label));
			} else if ("checkbox".equals(type)) {
				fields.add(ExternalCustomField.createCheckboxes(key, label));
			} else if ("text".equals(type)) {
				fields.add(ExternalCustomField.createText(key, label));
			} else if ("select".equals(type)) {
				final ExternalCustomField customField = ExternalCustomField.createSelect(key, label);

				customField.setValueMappingDefinition(new AbstractSelectFieldValueMapper(this,
						utils.getAuthenticationContext(), customField) {
					@Override
					public Set<String> getDistinctValues() {
						return Sets.newHashSet(jdbcConnection.queryDb(new SingleStringResultTransformer(
							"SELECT DISTINCT value FROM ticket_custom WHERE name='" + customField.getId() + "'")));
					}
				});

				final String options = environmentConfiguration.getString(key + ".options");
				if (StringUtils.isNotBlank(options)) {
					customField.setValueSet(Arrays.asList(StringUtils.split(options, '|')));
				}

				fields.add(customField);
			} else if ("radio".equals(type)) {
				final ExternalCustomField customField = ExternalCustomField.createRadio(key, label);

				final String options = environmentConfiguration.getString(key + ".options");
				if (StringUtils.isNotBlank(options)) {
					customField.setValueSet(Arrays.asList(StringUtils.split(options, '|')));
				}

				fields.add(customField);
			}
		}

		return fields;
	}

	public List<String> getLinkNamesFromDb() {
        return Lists.newArrayList();
	}

	public String getUsernameForEmail(@Nullable String email) {
		final String translatedUserName = getValueMappingHelper().getValueMappingForImport("email",
				StringUtils.defaultIfEmpty(email, "anonymous"));
		if (translatedUserName.equals(email)) {
			return extractUsername(email);
		} else {
			return translatedUserName;
		}
	}

	@Nullable
	public static String extractFullName(String fullName) {
		if (StringUtils.contains(fullName, "<") && StringUtils.contains(fullName, ">")) {
			return StringUtils.trimToNull(StringUtils.substringBefore(fullName, "<"));
		}
		return null;
	}

	protected static String extractUsername(@Nullable String fullName) {
		final String username;
		if (StringUtils.contains(fullName, "<") && StringUtils.contains(fullName, ">")) {
			username = StringUtils.substringBetween(fullName, "<", ">");
		} else {
			username = fullName;
		}
		return StringUtils.defaultString(username, "").toLowerCase(Locale.ENGLISH);
	}

	public String getUnusedUsersGroup() {
		return UNUSED_USERS_GROUP;
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

	public File getEnvironmentZip() {
		return environmentZip;
	}

	public Configuration getEnvironmentConfiguration() {
		return environmentConfiguration;
	}

	@Nullable
	public static Date getTimestamp(ResultSet rs, String column) throws SQLException {
		final Long ts = rs.getLong(column);
		return (ts != null && ts != 0) ? new Date(ts / 1000) : null; // they store timestamps in microseconds since epoch, we need miliseconds
    }
}

