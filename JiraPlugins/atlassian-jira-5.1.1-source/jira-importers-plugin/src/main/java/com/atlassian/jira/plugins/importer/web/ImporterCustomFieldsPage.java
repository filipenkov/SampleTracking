/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.Maps;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImporterCustomFieldsPage extends ImporterProcessSupport.Database {

	private List<ExternalCustomField> customFields = Collections.emptyList();
	private Map<ExternalCustomField, Map<String, Map<String, String>>> availableFieldMappings = Collections.emptyMap();

	public ImporterCustomFieldsPage(UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor,
			DateTimeFormatterFactory dateTimeFormatterFactory) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
	}

	@Override
    @RequiresXsrfCheck
	protected void doValidation() {
		final AbstractConfigBean2 configBean = getConfigBean();
		if (configBean != null) {
			configBean.populateFieldMappings(ActionContext.getParameters(), this);
		}
	}

	@Override
	protected void prepareModel() throws Exception {
		final AbstractConfigBean2 configBean = getConfigBean();

		//let it NPE - it would be an illegal state error, let default handler take care of it
		//noinspection ConstantConditions
		customFields = configBean.getCustomFields();
		availableFieldMappings = Maps.newHashMapWithExpectedSize(customFields.size());
		final Set<ExternalProject> projects = getController().createDataBean().getSelectedProjects(ConsoleImportLogger.INSTANCE);

		for (ExternalCustomField customField : customFields) {
			final Map<String, Map<String, String>> mappings = configBean.getAvailableFieldMappings(customField, projects);
			availableFieldMappings.put(customField, mappings);
		}
	}

	public Map<String, Map<String, String>> getAvailableFieldMappings(final ExternalCustomField customField) {
		return availableFieldMappings.get(customField);
	}

	public List<ExternalCustomField> getCustomFields() {
		return customFields;
	}

	public String getFieldMapping(String fieldName) {
		//explicitly fine to call config bean here
		final AbstractConfigBean2 configBean = getConfigBean();
		return configBean != null ? configBean.getFieldMapping(fieldName) : null;
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.set.up.custom.fields");
	}
}
