/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.web;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalSchemeManager;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.ImporterProjectMappingsPage;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;

public class PivotalProjectMappingsPage extends ImporterProjectMappingsPage {
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD")
	private final PivotalSchemeManager pivotalSchemeManager;

	public PivotalProjectMappingsPage(UsageTrackingService usageTrackingService,
			ExternalUtils utils,
			ProjectService projectService, PivotalSchemeManager pivotalSchemeManager,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor, CreateProjectManager createProjectManager) {
		super(usageTrackingService, utils, projectService, webInterfaceManager, pluginAccessor, createProjectManager);
		this.pivotalSchemeManager = pivotalSchemeManager;
	}

	@Override
	protected void validateProject(String fieldId, ExternalProject project) {
		final Project existingProject = getProjectManager().getProjectObjByKey(project.getKey());
		if (existingProject != null && !pivotalSchemeManager.isPTCompatible(existingProject)) {
			addError(fieldId,
					getText("jira-importer-plugin.pivotal.incompatibleSchema"));
		}
	}

	@Override
	public Collection<Project> getApplicableProjects() {
		return Collections2.filter(super.getApplicableProjects(), new Predicate<Project>() {
			@Override
			public boolean apply(Project input) {
				return pivotalSchemeManager.isPTCompatible(input);
			}
		});
	}

	@Override
	public String getFormDescription() {
		return super.getFormDescription() + " " + getText("jira-importer-plugin.wizard.projectmappings.pivotal.additionalDescription");
	}
}
