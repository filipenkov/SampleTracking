/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinitionsFactory;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Arrays;
import java.util.List;

public class FogBugzValueMappingDefinitionsFactory implements ValueMappingDefinitionsFactory {
	private final FogBugzConfigBean configBean;
	private final JiraAuthenticationContext authenticationContext;
	private final ConstantsManager constantsManager;
	private final FieldManager fieldManager;

	public FogBugzValueMappingDefinitionsFactory(FogBugzConfigBean configBean,
			JiraAuthenticationContext authenticationContext,
			ConstantsManager constantsManager, FieldManager fieldManager) {
		this.configBean = configBean;
		this.authenticationContext = authenticationContext;
		this.constantsManager = constantsManager;
		this.fieldManager = fieldManager;
	}

	public List<ValueMappingDefinition> createMappingDefinitions(final ValueMappingHelper valueMappingHelper) {
		final JdbcConnection jdbcConnection = configBean.getJdbcConnection();
		return Arrays.<ValueMappingDefinition>asList(new PriorityValueMapper(jdbcConnection, authenticationContext, fieldManager),
				new FullNameValueMapper(jdbcConnection, authenticationContext),
				new ComputerValueMapper(jdbcConnection, authenticationContext),
				new IssueCategoryValueMapper(jdbcConnection, authenticationContext, constantsManager),
				new StatusValueMapper(jdbcConnection, authenticationContext, valueMappingHelper),
				new ResolutionValueMapper(jdbcConnection, authenticationContext, constantsManager));
	}
}
