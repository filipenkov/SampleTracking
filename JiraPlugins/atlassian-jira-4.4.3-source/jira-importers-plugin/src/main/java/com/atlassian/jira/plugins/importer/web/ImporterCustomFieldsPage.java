/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.Maps;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImporterCustomFieldsPage extends ImporterProcessSupport.Database {

	private List<ExternalCustomField> customFields = Collections.emptyList();
	private Map<ExternalCustomField, Map<String, Map<String, String>>> availableFieldMappings = Collections.emptyMap();

	public ImporterCustomFieldsPage(UsageTrackingService usageTrackingService,
			ImporterControllerFactory importerControllerFactory, WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, importerControllerFactory, webInterfaceManager);
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
	protected void prepareModel() {
		final AbstractConfigBean2 configBean = getConfigBean();

		//let it NPE - it would be an illegal state error, let default handler take care of it
		//noinspection ConstantConditions
		customFields = configBean.getCustomFields();
		availableFieldMappings = Maps.newHashMapWithExpectedSize(customFields.size());
		for (ExternalCustomField customField : customFields) {
			final Map<String, Map<String, String>> mappings = configBean.getAvailableFieldMappings(customField);
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
		return "Setup custom fields";
	}
}
