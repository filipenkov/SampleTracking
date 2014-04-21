/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv.web;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalUserMapper;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.ImporterProcessSupport;
import com.atlassian.jira.plugins.importer.web.ImporterProjectMappingsPage;
import com.atlassian.jira.plugins.importer.web.model.ProjectModel;
import com.atlassian.jira.plugins.importer.web.model.ProjectSelectionModel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import webwork.action.ActionContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvProjectMappingsPage extends ImporterProcessSupport.Csv {

	private final ProjectService projectService;
	private final ProjectManager projectManager;
    private final CreateProjectManager createProjectManager;

    private boolean readFromCsv;
	private String userEmailSuffix;
	private String dateImportFormat;
	private ImporterProjectMappingsPage.Helper helper;

	public CsvProjectMappingsPage(UsageTrackingService usageTrackingService, ProjectService projectService,
			ProjectManager projectManager, CreateProjectManager createProjectManager,
			WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
		this.projectService = projectService;
		this.projectManager = projectManager;
        this.createProjectManager = createProjectManager;
        this.helper = new ImporterProjectMappingsPage.Helper(projectService, getLoggedInUser());
	}

	@Override
	public String doDefault() throws Exception {
		String result = super.doDefault();
		if (INPUT.equals(result)) {
			final CsvConfigBean configBean = getConfigBean();
			readFromCsv = configBean.isReadingProjectsFromCsv();

			userEmailSuffix = StringUtils.defaultString(configBean.getStringValue(CsvConfigBean.USER_EMAIL_SUFFIX),
					ExternalUserMapper.DEFAULT_EMAIL_SUFFIX);

			dateImportFormat = StringUtils.defaultIfEmpty(configBean.getStringValue(CsvConfigBean.DATE_IMPORT_FORMAT),
					CsvConfigBean.DEFAULT_DATE_FORMAT);
		}
		return result;
	}

	@Override
	@RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

		final CsvConfigBean configBean = getConfigBean();
		if (configBean == null) {
			return;
		}

		if (isPreviousClicked()) {
			return;
		}

		final Map actionParams = ActionContext.getParameters();
		final String projectName = ParameterUtils.getStringParam(actionParams, "CSV_project_name");
		final String projectKey = ParameterUtils.getStringParam(actionParams, "CSV_project_key");
		final String projectLead = StringUtils.defaultIfEmpty(ParameterUtils.getStringParam(actionParams, "CSV_project_lead"),
				getLoggedInUser().getName());

		if (!readFromCsv) {
			// check mandatory project fields (name, key, lead) have been populated
			// this.errors will be automatically populated through JiraServiceContext
			final Project jiraProject = projectManager.getProjectObjByKey(projectKey);
			if (jiraProject != null) {
				if (!jiraProject.getKey().equals(projectKey) || !jiraProject.getName().equals(projectName)) {
					addError("project", getText("jira-importer-plugin.project.key.or.name.already.used"));
				}
			} else if (isCreateProjectEnabled()) {
				if (StringUtils.isBlank(projectKey) || StringUtils.isBlank(projectName) || StringUtils.isBlank(projectLead)) {
					addError("project", "Please select a valid project.");
				} else {
					final ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
							getLoggedInUser(), projectName, projectKey, configBean.getProjectDescription(), projectLead,
							configBean.getProjectUrl(), null);
					if (!result.isValid()) {
						addError("project", StringUtils.join(result.getErrorCollection().getErrors().values(), " "));
					}
				}
			} else {
               addError("project", getText("jira-importer-plugin.not.allowed.to.create.projects"));
            }
        } else if (!isCreateProjectEnabled()) {
            addErrorMessage(getText("jira-importer-plugin.not.allowed.to.read.from.csv"));
        }

		if (StringUtils.isNotBlank(dateImportFormat)) {
			try {
				new SimpleDateFormat(dateImportFormat);
			} catch(IllegalArgumentException e) {
				addError("dateImportFormat",
						getText("jira-importer-plugin.csv.misc.page.invalid.date.format", e.getMessage()));
			}
		}

		// need to copy them before validation because code
		// handling PROJECT_OPTION_MAP_FROM_CSV needs config bean to
		// have all needed values set
		getConfigBean().populateProjectMapping(readFromCsv, projectName, projectKey, projectLead);
		getConfigBean().setValue(CsvConfigBean.DATE_IMPORT_FORMAT, dateImportFormat);
		getConfigBean().setValue(CsvConfigBean.USER_EMAIL_SUFFIX, userEmailSuffix);
	}

    public boolean isCreateProjectEnabled() {
        return createProjectManager.canCreateProjects(getLoggedInUser());
    }

	public String getProjectSuggestionsModel() {
		List<ProjectModel> suggestions = Lists.newArrayList(Iterables.transform(getApplicableProjects(), new Function<Project, ProjectModel>() {
			@Override
			public ProjectModel apply(Project input) {
				return new ProjectModel(input.getName(), input.getKey(), input.getLeadUserName(), false);
			}
		}));

        if (createProjectManager.canCreateProjects(getLoggedInUser())) {
            suggestions.addAll(Collections2.transform(getSuggestedNewProjects(), new Function<ExternalProject, ProjectModel>() {
                @Override
                public ProjectModel apply(ExternalProject input) {
                    return new ProjectModel(input.getName(), input.getKey(), input.getLead(), true);
                }
            }));
        }

		try {
			return new ObjectMapper().writeValueAsString(suggestions);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getProjectMappingModel() {
		final ProjectSelectionModel model = new ProjectSelectionModel();

		final CsvConfigBean configBean = getConfigBean();
		model.key = configBean.getProjectKey();
		model.selected = !configBean.isReadingProjectsFromCsv();
		model.id = "CSV";
		try {
			return new ObjectMapper().writeValueAsString(model);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<Project> getApplicableProjects() {
		return helper.getExistingProjects();
	}

	@SuppressWarnings("unused")
	public Collection<ExternalProject> getSuggestedNewProjects() {
		final CsvConfigBean configBean = getConfigBean();
		final String projectKey = configBean.getProjectKey("CSV");
		final String projectName = configBean.getProjectName("CSV");
		final String projectLead = StringUtils.defaultIfEmpty(configBean.getProjectLead("CSV"),
				getLoggedInUser().getName());

		if (StringUtils.isBlank(projectKey) || StringUtils.isBlank(projectName)) {
			return Collections.emptyList();
		}

		final Collection<ExternalProject> suggestedKeys = Lists.newArrayList(
				new ExternalProject(projectName, projectKey, projectLead));
		final Set<String> existingProjectKeys = helper.getExistingProjectKeys();
		return Collections2.filter(suggestedKeys, new Predicate<ExternalProject>() {
			@Override
			public boolean apply(ExternalProject suggestedProject) {
				final String key = suggestedProject.getKey();
				return StringUtils.isNotBlank(key) && !existingProjectKeys.contains(key);
			}
		});
	}

	public String getReadFromCsv() {
		return Boolean.toString(readFromCsv);
	}

	@SuppressWarnings("unused")
	public void setReadFromCsv(String readFromCsv) {
		this.readFromCsv = Boolean.parseBoolean(readFromCsv);
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.wizard.projectmappings.title");
	}

	public String getUserEmailSuffix() {
		return userEmailSuffix;
	}

	public void setUserEmailSuffix(String userEmailSuffix) {
		this.userEmailSuffix = userEmailSuffix;
	}

	public String getDateImportFormat() {
		return dateImportFormat;
	}

	public void setDateImportFormat(String dateImportFormat) {
		this.dateImportFormat = dateImportFormat;
	}
}
