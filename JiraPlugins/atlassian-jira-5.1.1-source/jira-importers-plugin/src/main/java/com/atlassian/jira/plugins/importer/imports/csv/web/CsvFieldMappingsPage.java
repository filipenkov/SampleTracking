/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv.web;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.customfields.SupportedCustomFieldPredicate;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalCustomFieldValueMapper;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalIssueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.plugins.importer.web.AddCustomFieldDialog;
import com.atlassian.jira.plugins.importer.web.ImporterProcessSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import webwork.action.ActionContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class CsvFieldMappingsPage extends ImporterProcessSupport.Csv {

	private final JiraAuthenticationContext authenticationContext;
	private final ProjectService projectService;
	private final CustomFieldManager customFieldManager;
	private final ExternalUtils utils;

	public CsvFieldMappingsPage(UsageTrackingService usageTrackingService,
			JiraAuthenticationContext authenticationContext,
			ProjectService projectService, CustomFieldManager customFieldManager,
			WebInterfaceManager webInterfaceManager, ExternalUtils utils,
			PluginAccessor pluginAccessor) {
		super(usageTrackingService, webInterfaceManager, pluginAccessor);
		this.authenticationContext = authenticationContext;
		this.projectService = projectService;
		this.customFieldManager = customFieldManager;
		this.utils = utils;
	}

	@Override
	@RequiresXsrfCheck
	protected void doValidation() {
		super.doValidation();

		if (!isNextClicked()) {
			return;
		}

		final ImporterController controller = getController();
		final CsvConfigBean configBean = getConfigBean();
		if (configBean == null || controller == null) {
			return;
		}
		final Collection<FieldMapping> map;
		try {
			map = new ObjectMapper().readValue(model, new TypeReference<Collection<FieldMapping>>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Map<String, String> pops = Maps.newHashMap();
		final Set<String> fieldsWithValueMappings = Sets.newHashSet();
		for (String fieldName : configBean.getHeaderRow()) {
			final String htmlFieldName = getFieldName(fieldName);

			for (FieldMapping fieldMapping : map) {
				if (fieldMapping.id.equals(htmlFieldName)) {
					if (fieldMapping.imported && StringUtils.isNotBlank(fieldMapping.targetField)) {
						final String realFieldName = fieldMapping.id.replaceFirst(htmlFieldName, configBean
								.getFieldName(fieldName));
						if (fieldMapping.customFieldModel != null) {
							final String fieldDescr;
							if (StringUtils.isBlank(fieldMapping.customFieldModel.id)) {
								// new custom field
								fieldDescr = DefaultExternalCustomFieldValueMapper.CUSTOMFIELD_PREFIX
										+ fieldMapping.customFieldModel.name
										+ ":"
										+ fieldMapping.customFieldModel.type;
							} else {
								// existing one
								fieldDescr  = fieldMapping.customFieldModel.id;
							}
							pops.put(realFieldName, CsvConfigBean.EXISTING_CUSTOM_FIELD);
							pops.put(realFieldName + CsvConfigBean.EXISTING_CUSTOM_FIELD, fieldDescr);
						} else {
							pops.put(realFieldName, fieldMapping.targetField);
						}
						if (fieldMapping.manualMapping) {
							fieldsWithValueMappings.add(StringUtils.replaceOnce(realFieldName, "field.", ""));
						}
					}
				}
			}
		}

		configBean.populateFieldMappings(pops);
		configBean.setMapValues(fieldsWithValueMappings.toArray(new String[0]));

		if (configBean.isReadingProjectsFromCsv()) {
			validateProjectRequiredFields(configBean, controller);
		}

		//check a summary field has been selected upon clicking next
		if (!configBean.containsFieldWithValue("summary")) {
			addErrorMessage(getText("jira-importer-plugin.csv.field.mappings.page.must.have.summary"));
		}

		if (configBean.containsFieldWithValue(DefaultExternalIssueMapper.SUBTASK_PARENT_ID)) {

			if (!configBean.containsFieldWithValue(DefaultExternalIssueMapper.ISSUE_ID)) {
				addErrorMessage(getText("jira-importer-plugin.csv.mappings.subtasks.mustincludeissueid"));
			}
			if (!configBean.containsFieldWithValue(IssueFieldConstants.ISSUE_TYPE)) {
				addErrorMessage(getText("jira-importer-plugin.csv.mappings.subtasks.mustmaptype", getText("issue.field.issuetype")));
			}
		}

		if (!isAttachmentsEnabled() && configBean.containsFieldWithValue("attachment")) {
			addErrorMessage(getText("jira-importer-plugin.csv.field.mappings.page.attachments.disabled"),
					getText("jira-importer-plugin.importer.attachments.can.be.enabled",
					ActionContext.getContext().getRequestImpl().getContextPath()
							+ "/secure/admin/jira/ViewAttachmentSettings.jspa"));
		}
	}

	protected void validateProjectRequiredFields(CsvConfigBean configBean, ImporterController controller) {
		// check if all required fields have a mapping defined
		// then check if values are valid
		final boolean hasProjectKey = configBean != null && configBean.containsFieldWithValue("project.key");
		final boolean hasProjectName = configBean != null && configBean.containsFieldWithValue("project.name");

		if (!hasProjectKey) {
			addErrorMessage(getText("jira-importer-plugin.csv.field.mappings.page.must.have.project.key"));
		}

		if (!hasProjectName) {
			addErrorMessage(getText("jira-importer-plugin.csv.field.mappings.page.must.have.project.name"));
		}

		if (hasProjectKey && hasProjectName) {
			try {
				ImportDataBean dataBean = controller.createDataBean();
				for (ExternalProject project : dataBean.getAllProjects(ConsoleImportLogger.INSTANCE)) {
					final Project jiraProject = utils.getProject(project);

					if (jiraProject != null) {
						if (!jiraProject.getKey().equals(project.getKey()) || !jiraProject.getName()
								.equals(project.getName())) {
							addErrorMessage(
									getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.key",
											project.getKey(),
											getText("jira-importer-plugin.project.key.or.name.already.used")));
						}
					} else {
						// we don't validateProjectRequiredFields the lead - it doesn't probably exist yet
						ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
								authenticationContext.getLoggedInUser(),
								project.getName(), project.getKey(), project.getDescription(),
								authenticationContext.getLoggedInUser().getName(), project.getUrl(), null);
						if (!result.isValid()) {
							addErrorMessages(project, result.getErrorCollection().getErrors());
						}
					}
				}
			} catch (Exception e) {
				addErrorMessage(e.getMessage());
			}
		}
	}

	private void addErrorMessages(ExternalProject project, Map<String, String> errors) {
		for (Map.Entry<String, String> field : errors.entrySet()) {
			if ("projectName".equals(field.getKey())) {
				addErrorMessage(
						getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.name", project.getName(),
								field.getValue()));
			} else if ("projectKey".equals(field.getKey())) {
				addErrorMessage(
						getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.key", project.getKey(),
								field.getValue()));
			} else if ("projectUrl".equals(field.getKey())) {
				addErrorMessage(
						getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.url", project.getUrl(),
								field.getValue()));
			} else if ("projectDescription".equals(field.getKey())) {
				addErrorMessage(getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.description",
						project.getDescription(), field.getValue()));
			} else if ("projectLead".equals(field.getKey())) {
				addErrorMessage(
						getText("jira-importer-plugin.csv.field.mappings.page.invalid.project.lead", project.getLead(),
								field.getValue()));
			}
		}
	}

	public static String getFieldName(String fieldName) {
		return "field-" + DigestUtils.md5Hex(fieldName);
	}

	public static String getExistingCfName(String fieldName) {
		return getFieldName(fieldName) + CsvConfigBean.EXISTING_CUSTOM_FIELD;
	}

	public static String getNewCfName(String fieldName) {
		return getFieldName(fieldName) + CsvConfigBean.NEW_CUSTOM_FIELD;
	}

	public static String getNewCfType(String fieldName) {
		return getFieldName(fieldName) + CsvConfigBean.NEW_CUSTOM_FIELD_TYPE;
	}

	/**
	 * Only return the custom fields that are within the scope of the selected 'import project option'.
	 */
	public Collection<CustomField> getCustomFields() {
		final Collection<CustomField> allCustomFields = customFieldManager.getCustomFieldObjects();
		final Collection<CustomField> availableCustomFields = Lists.newArrayList();

        final Project staticProject = utils.getProjectManager().getProjectObjByKey(getConfigBean().getProjectKey("CSV"));
		if (!getConfigBean().isReadingProjectsFromCsv() && staticProject != null) {
			final String projectKey = getConfigBean().getProjectKey("CSV");
			final Project project = utils.getProjectManager().getProjectObjByKey(projectKey);

			for (CustomField customFieldObject : allCustomFields) {
				if (customFieldObject.isGlobal() || customFieldObject.getAssociatedProjects().contains(project.getGenericValue())) {
					availableCustomFields.add(customFieldObject);
				}
			}
		} else {
			for (CustomField customFieldObject : allCustomFields) {
				if (customFieldObject.isGlobal())
					availableCustomFields.add(customFieldObject);
			}
		}

		return Lists.newArrayList(Iterables.filter(availableCustomFields, new SupportedCustomFieldPredicate()));
	}

	@Override
	public String getFormTitle() {
		return getText("jira-importer-plugin.set.up.field.mappings");
	}


	private String model;

	public void setModel(String model) {
		this.model = model;
	}


	Collection<FieldMapping> getModelImpl() {
		final CsvConfigBean configBean = getConfigBean();
		if (configBean == null) {
			return null;
		}
		Collection<FieldMapping> res = Lists.newArrayList();
		// by default all fields are marked as to be imported
		for (String s : configBean.getHeaderRow()) {
			final String storedMapping = configBean.getFieldMapping(s);
			final String fieldId = StringUtils.equals(storedMapping, CsvConfigBean.EXISTING_CUSTOM_FIELD) ?
					configBean.getExistingCfNameValue(s) : storedMapping;
			final FieldMapping fieldMapping = new FieldMapping(getFieldName(s), configBean.isFieldMapped(s), fieldId,
					configBean.isInMapValues(s));
			if (CsvConfigBean.NEW_CUSTOM_FIELD.equals(storedMapping)) {
				fieldMapping.customFieldModel = new CustomFieldModel();
				fieldMapping.customFieldModel.name = configBean.getNewCfNameValue(s);
				fieldMapping.customFieldModel.type = configBean.getNewCfTypeValue(s);
			}

			res.add(fieldMapping);
		}
		return res;
	}

	public String getModel() {
        try {
            return new ObjectMapper().configure(SerializationConfig.Feature.INDENT_OUTPUT, true).writeValueAsString(getModelImpl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public static class CustomFieldModel {
		public String id;
		public String name;
		public String type;
	}

	public String getCustomFieldsModel() {
		final Collection<CustomField> customFields = getCustomFields();
		List<CustomFieldModel> model = Immutables.transformThenCopyToList(customFields, new Function<CustomField, CustomFieldModel>() {
			@Override
			public CustomFieldModel apply(CustomField input) {
				final CustomFieldModel model = new CustomFieldModel();
				model.id = input.getId();
				model.name = input.getName();
				return model;
			}
		});
		try {
			return new ObjectMapper()
					.configure(SerializationConfig.Feature.INDENT_OUTPUT, true)
					.writeValueAsString(model);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FieldMapping {
		public String id;
		public boolean imported;
		public String targetField;
		public CustomFieldModel customFieldModel;
		public boolean manualMapping;

		public FieldMapping() {
		}

		public FieldMapping(String id, boolean imported, String targetField, boolean isManualMapping) {
			this.imported = imported;
			this.targetField = targetField;
			this.manualMapping = isManualMapping;
			this.id = id;
		}


		@Override
		public String toString() {
			return Objects.toStringHelper(this).
					add("id", id).
					add("imported", imported).
					add("targetField", targetField).
					add("manualMapping", manualMapping).
					toString();
		}

	}
}
