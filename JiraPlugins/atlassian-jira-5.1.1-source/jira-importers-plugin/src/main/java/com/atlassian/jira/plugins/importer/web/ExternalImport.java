/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugins.importer.extensions.ExternalSystemImporterModuleDescriptor;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

@WebSudoRequired
public class ExternalImport extends ImporterProcessSupport {

	private final PluginAccessor pluginAccessor;

	public ExternalImport(UsageTrackingService usageTrackingService, PluginAccessor pluginAccessor,
						  WebInterfaceManager webInterfaceManager) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
		this.pluginAccessor = pluginAccessor;
	}

	@Override
	public String doDefault() throws Exception {
		return INPUT;
	}

	@Override
	public String doExecute() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}

		return "success";
	}

    @SuppressWarnings("unused")
	public String doInitialOptIn() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		getUsageTrackingService().activate();
		getUsageTrackingService().includeTrackingWhenActive();
		return doExecute();
	}

    @SuppressWarnings("unused")
	public String doInitialOptOut() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}
		getUsageTrackingService().deactivate();
		return doExecute();
	}

	public List<ExternalSystemImporterModuleDescriptor> getEnabledExternalImporters() {
		return pluginAccessor.getEnabledModuleDescriptorsByClass(ExternalSystemImporterModuleDescriptor.class);
	}
    
    public Iterable<ExternalSystemImporterModuleDescriptor> getVisibleExternalImporters() {
        final HashMap<String, Object> context = Maps.newHashMap();
        context.put(JiraWebInterfaceManager.CONTEXT_KEY_USER, getLoggedInUser());
        final List<ExternalSystemImporterModuleDescriptor> enabledExternalImporters = getEnabledExternalImporters();
        // velocity SortTool does not support Iterable :(
        return Lists.newArrayList(Iterables.filter(enabledExternalImporters, new Predicate<ExternalSystemImporterModuleDescriptor>() {
            @Override
            public boolean apply(ExternalSystemImporterModuleDescriptor descriptor) {
                try {
                    return descriptor.getCondition() == null || descriptor.getCondition().shouldDisplay(context);
                } catch (Exception e) {
                    log.error("Cannot evaluate condition for descriptor '" + descriptor.getKey() + "'", e);
                    return false;
                }
            }
        }));
    }

	@Override
	@Nullable
	public String getFormTitle() {
		return getText("jira-importer-plugin.external.external.import");
	}

	@Override
	public String getWizardActiveSection() {
		return "admin_system_menu/top_system_section/import_export_section";
	}

	@Override
	public String getWizardActiveTab() {
		return "external_import";
	}
	
	public boolean shouldDisplayJelly() {
		return isSystemAdministrator();
	}
}
