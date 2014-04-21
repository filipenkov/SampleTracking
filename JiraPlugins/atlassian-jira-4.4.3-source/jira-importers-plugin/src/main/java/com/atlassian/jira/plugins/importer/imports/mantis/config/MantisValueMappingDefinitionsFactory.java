/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Lists;

import java.util.List;

import static com.atlassian.jira.plugins.importer.external.CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE;
import static com.atlassian.jira.plugins.importer.external.CustomFieldConstants.MULTISELECT_FIELD_TYPE;
import static com.atlassian.jira.plugins.importer.external.CustomFieldConstants.SELECT_FIELD_TYPE;

public class MantisValueMappingDefinitionsFactory implements ValueMappingDefinitionsFactory {
	private final JiraAuthenticationContext authenticationContext;
	private final ConstantsManager constantsManager;
	private final MantisConfigBean configBean;
	private final FieldManager fieldManager;

	public MantisValueMappingDefinitionsFactory(JiraAuthenticationContext authenticationContext,
			ConstantsManager constantsManager, MantisConfigBean configBean, FieldManager fieldManager) {
		this.authenticationContext = authenticationContext;
		this.constantsManager = constantsManager;
		this.configBean = configBean;
		this.fieldManager = fieldManager;
	}

	public List<ValueMappingDefinition> createMappingDefinitions(ValueMappingHelper valueMappingHelper) {
		final JdbcConnection jdbcConnection = configBean.getJdbcConnection();
		List<ValueMappingDefinition> mappings = Lists.<ValueMappingDefinition>newArrayList(
				new LoginNameValueMapper(jdbcConnection, authenticationContext),
				new StatusValueMapper(jdbcConnection, authenticationContext, valueMappingHelper),
				new ResolutionValueMapper(jdbcConnection, authenticationContext, constantsManager));

		for (ExternalCustomField customField : configBean.getCustomFields()) {
			String type = customField.getTypeKey();

            if (MantisConfigBean.SEVERITY_FIELD.equals(customField.getId())) {
                if(configBean.isFieldMappedToIssueField(MantisConfigBean.SEVERITY_FIELD)) {
                    mappings.add(new SeverityToPriorityValueMapper(jdbcConnection, authenticationContext,
                            fieldManager, customField));
                } else {
                    mappings.add(new SeverityValueMapper(configBean, authenticationContext, customField));
                }
                continue;
            }

            if (MantisConfigBean.PRIORITY_FIELD.equals(customField.getId())) {
                if (configBean.isFieldMappedToIssueField(MantisConfigBean.PRIORITY_FIELD)) {
                    mappings.add(new PriorityToPriorityValueMapper(configBean, authenticationContext,
                            fieldManager, customField));
                } else {
                    mappings.add(new PriorityValueMapper(configBean, authenticationContext, customField));
                }
                continue;
            }

			if (MULTISELECT_FIELD_TYPE.equals(type)
					|| MULTICHECKBOXES_FIELD_TYPE.equals(type)) {
				mappings.add(new MultipleSelectionValueMapper(configBean, authenticationContext, customField));
			}

			if (SELECT_FIELD_TYPE.equals(type)) {
				mappings.add(new CustomFieldValueMapper(jdbcConnection, authenticationContext, customField));
			}
		}
		
		return mappings;
	}
}
