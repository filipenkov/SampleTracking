/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PivotalConfigBean extends AbstractConfigBean2 {

	private final PivotalClient pivotalClient;
	private final ExternalUtils utils;
	private final String defaultResolutionId;
	private final SiteConfiguration credentials;

	public PivotalConfigBean(SiteConfiguration credentials, ExternalUtils utils) {
		super(utils);
		this.credentials = credentials;
		this.pivotalClient = new CachingPivotalClient();
		this.utils = utils;
		defaultResolutionId = utils.getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
	}

    @Override
    public void initializeValueMappingHelper() {
        final ValueMappingDefinitionsFactory mappingDefinitionFactory = new ValueMappingDefinitionsFactory() {
			public List<ValueMappingDefinition> createMappingDefinitions(ValueMappingHelper valueMappingHelper) {
				return Collections.emptyList();
			}
		};
		valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(), mappingDefinitionFactory, utils.getConstantsManager());
    }

	public PivotalClient getPivotalClient() throws PivotalRemoteException {
		if (!pivotalClient.isLoggedIn()) {
			pivotalClient.login(credentials.getUsername(), credentials.getPassword());
		}
		return pivotalClient;
	}

	public List<String> getProjectNamesFromDb() {
		try {
			return Lists.newArrayList(getPivotalClient().getAllProjectNames());
		} catch (PivotalRemoteException e) {
			throw new RuntimeException(e); // @todo wseliga
		}
	}


    @Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField) {
        final Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();
//
//        if (BUG_SEVERITY_FIELD.equals(customField.getId()) || PRIORITY_FIELD.equals(customField.getId())) {
//            fieldMappings.put(getI18n().getText("admin.csv.import.mappings.issue.fields.header"),
//                    MapBuilder.<String, String>newBuilder().add(
//                            mapToIssueFieldValue(IssueFieldConstants.PRIORITY),
//                            getI18n().getText("issue.field.priority")).toMap());
//        }
//
//        fieldMappings.put(getI18n().getText("admin.csv.import.mappings.custom.fields.header"),
//                getAvailableCustomFieldMappings(customField));
//
//        fieldMappings.putAll(super.getAvailableFieldMappings(customField));
        return fieldMappings;
    }

    public List<ExternalCustomField> getCustomFields() {
		return Collections.emptyList();
	}

	public List<String> getLinkNamesFromDb() {
		return Collections.emptyList();
	}

	public String getUsernameForLoginName(String loginName) {
		String username = getValueMappingHelper().getValueMappingForImport(LoginNameValueMapper.FIELD, loginName);
		return username.toLowerCase(Locale.ENGLISH);
	}

	public String getDefaultResolutionId() {
		return defaultResolutionId;
	}
}

