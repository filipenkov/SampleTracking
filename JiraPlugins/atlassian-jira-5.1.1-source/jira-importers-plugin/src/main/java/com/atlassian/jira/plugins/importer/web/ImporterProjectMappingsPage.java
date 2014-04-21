/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.model.ProjectModel;
import com.atlassian.jira.plugins.importer.web.model.ProjectSelectionModel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImporterProjectMappingsPage extends ImporterProcessSupport.Database {

	public static class Helper {
		private final ProjectService projectService;
		private final User user;

		public Helper(ProjectService projectService, User user) {
			this.projectService = projectService;
			this.user = user;
		}

		public Collection<Project> getExistingProjects() {
			final ServiceOutcome<List<Project>> allProjectsForAction = projectService
					.getAllProjectsForAction(user, REQUIRED_PROJECT_PERMISSION);
			return allProjectsForAction.getReturnedValue();
		}

		public Set<String> getExistingProjectKeys() {
			return ImmutableSet.copyOf(Iterables.transform(getExistingProjects(), new Function<Project, String>() {
				@Override
				public String apply(Project project) {
					return project.getKey();
				}
			}));
		}
	}

	public static final ProjectAction REQUIRED_PROJECT_PERMISSION = ProjectAction.EDIT_PROJECT_CONFIG;
	protected final ExternalUtils utils;
	protected final ProjectService projectService;
    private final CreateProjectManager createProjectManager;

    private final Helper helper;
	private List<String> projectNamesFromDb = Collections.emptyList();
	private Set<String> selectedProjects = Collections.emptySet();

	private String projectMapping;

	public ImporterProjectMappingsPage(UsageTrackingService usageTrackingService,
			ExternalUtils utils, ProjectService projectService, WebInterfaceManager webInterfaceManager,
			PluginAccessor pluginAccessor, CreateProjectManager createProjectManager) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
		this.utils = utils;
		this.projectService = projectService;
        this.createProjectManager = createProjectManager;
        this.helper = new Helper(projectService, getLoggedInUser());
	}

	@Override
	@RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

		if (isPreviousClicked() || getConfigBean() == null) {
			return;
		}

		populateProjectKeyMappings();
	}

	@Override
	protected void prepareModel() {
		final AbstractConfigBean2 configBean = getConfigBean();
		projectNamesFromDb = configBean.getExternalProjectNames();
		selectedProjects = ImmutableSet.copyOf(Iterables.filter(projectNamesFromDb, new Predicate<String>() {
			@Override
			public boolean apply(@Nullable String input) {
				return configBean.isProjectSelected(input);
			}
		}));
	}

	@Nullable
	Map<String, ProjectSelectionModel> decodeMapping(String mappingJSON) {
		final List<ProjectSelectionModel> model;
		try {
			model = new ObjectMapper()
					.readValue(mappingJSON, new TypeReference<List<ProjectSelectionModel>>() {});
		} catch (IOException e) {
			log.warn("Error decoding project mapping model", e);
			log.trace("Received model: " + mappingJSON);
			return null;
		}

		return Maps.uniqueIndex(model, new Function<ProjectSelectionModel, String>() {
			@Override
			public String apply(ProjectSelectionModel input) {
				return input.id;
			}
		});
	}

	// full override to accomodate old and new form layout
	public void populateProjectKeyMappings() {
		final Map<String, ExternalProject> projectKeyMappings = Maps.newHashMap();
		final Map<String, String> selectedNamesByKey = Maps.newHashMap();
		final Map<String, String> selectedKeysByName = Maps.newHashMap();

		final Map<String, ProjectSelectionModel> projectMappings = decodeMapping(projectMapping);
		if (projectMappings == null) {
			return;
		}

		final List<String> projectNames = getConfigBean().getExternalProjectNames();
		Collections.sort(projectNames);


		for (final String projectName : projectNames) {
			final String fieldId = getProjectFieldId(projectName);
			final ProjectSelectionModel mappingData = projectMappings.get(fieldId);

			if (!mappingData.selected ) {
				continue;
			}
			if (mappingData.projectModel == null) {
				addError(projectName, "Please select a valid project.");
				continue;
			}
			final ExternalProject project = new ExternalProject(
					mappingData.projectModel.name,
					mappingData.projectModel.key,
					StringUtils.defaultIfEmpty(mappingData.projectModel.lead,
							getLoggedInUser().getName()));

			validateProject(projectName, project);

			final Project jiraProject = utils.getProject(project);
			if (jiraProject != null) {
				if (!jiraProject.getKey().equals(project.getKey()) || !jiraProject.getName()
						.equals(project.getName())) {
					addError(projectName, getText("jira-importer-plugin.project.key.or.name.already.used"));
				}
			} else {
				if (StringUtils.isBlank(project.getKey())) {
					addError(projectName, "Please select a valid project.");
				} else {
					final ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
							utils.getAuthenticationContext().getLoggedInUser(),
							project.getName(), project.getKey(), null,
							project.getLead(),
							null, null);

					if (!result.isValid()) {
						addErrorMessages(projectName, result.getErrorCollection().getErrors());
					}

					final String existingName = selectedNamesByKey.get(project.getKey());
					if (existingName != null && !existingName.equals(project.getName())) {
						addError(projectName, getText("jira-importer-plugin.project.key.or.name.already.used"));
					}

					final String existingKey = selectedKeysByName.get(project.getName());
					if (existingKey != null && !existingKey.equals(project.getKey())) {
						addError(projectName, getText("jira-importer-plugin.project.key.or.name.already.used"));
					}
					selectedNamesByKey.put(project.getKey(), project.getName());
					selectedKeysByName.put(project.getName(), project.getKey());
				}
			}

			projectKeyMappings.put(projectName, project);
		}

		getConfigBean().populateProjectKeyMappings(projectKeyMappings);
	}

	/**
	 * Validate single project as defined in the form.
	 *
	 * @param fieldId field id for addError()
	 * @param project ExternalProject to verify
	 */
	protected void validateProject(String fieldId, ExternalProject project) {
	}

	protected void addErrorMessages(String projectName, Map<String, String> errors) {
		addError(projectName, StringUtils.join(errors.values(), " "));
	}

	public static String getProjectFieldId(String projectName) {
		return "P" + DigestUtils.md5Hex(projectName);
	}

	public Collection<Project> getApplicableProjects() {
		return helper.getExistingProjects();
	}

	public Collection<ExternalProject> getSuggestedNewProjects() {
		final AbstractConfigBean2 configBean = getConfigBean();
		final List<String> importedProjectNames = getProjectNamesFromDb();
		final Collection<ExternalProject> suggestedKeys = Collections2
				.transform(importedProjectNames, new Function<String, ExternalProject>() {
					@Override
					public ExternalProject apply(String importedProjectName) {
						final String projectKey = configBean.getProjectKey(importedProjectName);
						final String projectName = configBean.getProjectName(importedProjectName);
						final String projectLead = configBean.getProjectLead(importedProjectName);

						return new ExternalProject(projectName, projectKey, projectLead);
					}
				});
		final Set<String> existingProjectKeys = helper.getExistingProjectKeys();
		return Collections2.filter(suggestedKeys, new Predicate<ExternalProject>() {
			@Override
			public boolean apply(ExternalProject suggestedProject) {
				final String key = suggestedProject.getKey();
				return StringUtils.isNotBlank(key) && !existingProjectKeys.contains(key);
			}
		});
	}

	public String getProjectSuggestionsModel() {
		List<ProjectModel> suggestions = Lists.newArrayList(Iterables.transform(getApplicableProjects(), new Function<Project, ProjectModel>() {
			@Override
			public ProjectModel apply(Project input) {
				 return new ProjectModel(input.getName(), input.getKey(), input.getLeadUserName(), false);
			}
		}));

		suggestions.addAll(Collections2.transform(getSuggestedNewProjects(), new Function<ExternalProject, ProjectModel>() {
			@Override
			public ProjectModel apply(ExternalProject input) {
				return new ProjectModel(input.getName(), input.getKey(), input.getLead(), true);
			}
		}));

		try {
			return new ObjectMapper().writeValueAsString(suggestions);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getProjectMappingModel() {
		List<ProjectSelectionModel> list = Immutables.transformThenCopyToList(getProjectNamesFromDb(), new Function<String, ProjectSelectionModel>() {
			@Override
			public ProjectSelectionModel apply(String name) {
				final ProjectSelectionModel model = new ProjectSelectionModel();
				model.externalName = name;
				model.id = getProjectFieldId(name);
				model.selected = getConfigBean().isProjectSelected(name);
				model.key = getConfigBean().getProjectKey(name);

				return model;
			}
		});

		try {
			return new ObjectMapper().writeValueAsString(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getProjectNamesFromDb() {
		return projectNamesFromDb;
	}

	public boolean isProjectSelected(String project) {
		return selectedProjects.contains(project);
	}

	public void setProjectMapping(String projectMapping) {
		this.projectMapping = projectMapping;
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.wizard.projectmappings.title");
	}

	@Override
	public String getFormDescription() {
		return getText("jira-importer-plugin.wizard.projectmappings.description", getTitle());
	}

    public boolean isCreateProjectsEnabled() {
        return createProjectManager.canCreateProjects(getLoggedInUser());
    }

}
