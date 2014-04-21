/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.bugzilla.config.LoginNameValueMapper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FogBugzHostedConfigBean extends AbstractConfigBean2 {

	private final FogBugzClient client;

	public FogBugzHostedConfigBean(FogBugzClient client, ExternalUtils utils) {
		super(utils);
		this.client = client;
	}

	public FogBugzClient getClient() {
		return client;
	}

	@Override
    public void initializeValueMappingHelper() {
        final ValueMappingDefinitionsFactory mappingDefinitionFactory = new ValueMappingDefinitionsFactory() {
			public List<ValueMappingDefinition> createMappingDefinitions(final ValueMappingHelper valueMappingHelper) {
				List<ValueMappingDefinition> mappings = Lists.newArrayList();

				mappings.add(new StatusValueMappingDefinition(FogBugzHostedConfigBean.this, valueMappingHelper));
				mappings.add(new ResolutionValueMappingDefinition(FogBugzHostedConfigBean.this, utils.getConstantsManager()));
				return mappings;
			}
		};
		valueMappingHelper = new ValueMappingHelperImpl(utils.getWorkflowSchemeManager(),
				utils.getWorkflowManager(), mappingDefinitionFactory, utils.getConstantsManager());
    }

	public List<String> getProjectNamesFromDb() {
		try {
			return Lists.newArrayList(client.getAllProjectNames());
		} catch (FogBugzRemoteException e) {
			throw new RuntimeException(e); // @todo wseliga
		}
	}

    @Override
    public Map<String, Map<String, String>> getAvailableFieldMappings(ExternalCustomField customField) {
        return Maps.newLinkedHashMap();
    }

    public List<ExternalCustomField> getCustomFields() {
		return Collections.emptyList();
	}

	public List<String> getLinkNamesFromDb() {
		return ImmutableList.of(FogBugzConfigBean.SUBCASE_LINK_NAME);
	}

	public String getUsernameForLoginName(String loginName) {
		String username = getValueMappingHelper().getValueMappingForImport(LoginNameValueMapper.FIELD, loginName);
		return username.toLowerCase(Locale.ENGLISH);
	}

	public String getUnusedUsersGroup() {
		return FogBugzConfigBean.UNUSED_USERS_GROUP;
	}
}

