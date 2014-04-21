/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.Maps;
import webwork.action.ActionContext;

import java.util.Map;

public class ImporterFieldMappingsPage extends ImporterProcessSupport.Database {

	public ImporterFieldMappingsPage(UsageTrackingService usageTrackingService,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
	}

    @Override
    public String doDefault() throws Exception {
        String result = super.doDefault();
        if (INPUT.equals(result)) {
            // save current configuration before we reset ValueMappingHelper
            final Map<String, Object> configCopy = Maps.newHashMap();
            getConfigBean().getValueMappingHelper().copyToNewProperties(configCopy);
            getConfigBean().initializeValueMappingHelper();
			getConfigBean().getValueMappingHelper().initDistinctValuesCache();
            getConfigBean().getValueMappingHelper().copyFromProperties(configCopy);
        }
        return result;
    }

    @Override
    @RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

        if (getConfigBean() != null) {
		    getConfigBean().getValueMappingHelper().populateFieldForValueMappings(ActionContext.getParameters());
        }
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.set.up.field.mappings");
	}
}
