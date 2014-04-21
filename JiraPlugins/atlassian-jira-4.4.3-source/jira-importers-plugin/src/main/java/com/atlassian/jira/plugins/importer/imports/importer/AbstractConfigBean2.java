/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelperImpl;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ParameterUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigBean2 extends AbstractConfigBean {
	public static final String PROJECT_SELECTION_PREFIX = "projectSelection";
	public static final String PROJECT_KEY_CONFIG_PREFIX = "projectKey";
    public static final String PROJECT_NAME_CONFIG_PREFIX = "projectName";
    public static final String PROJECT_LEAD_CONFIG_PREFIX = "projectLead";
	public static final String FIELD_CONFIG_PREFIX = "field";
	public static final String LINK_CONFIG_PREFIX = "link";

	protected final ExternalUtils utils;

	protected final Map<String, String> projectKeyMapping = Maps.newLinkedHashMap();
    protected final Map<String, String> projectNameMapping = Maps.newLinkedHashMap();
	protected final Map<String, String> projectLeadMapping = Maps.newLinkedHashMap();
	protected final Map<String, String> fieldMapping = Maps.newLinkedHashMap();
	protected final Map<String, String> linkMapping = Maps.newLinkedHashMap();

	private final Map<String, String> projectSelectionMapping = new HashMap<String, String>();

    protected ValueMappingHelperImpl valueMappingHelper;

    private static final String ISSUE_FIELD_MAPPING = "issue-field:";


	private final CustomFieldManager customFieldManager;

	public AbstractConfigBean2(ExternalUtils utils) {
		super(utils.getAuthenticationContext());

		this.utils = utils;
		this.customFieldManager = utils.getCustomFieldManager();
	}

    protected Map<String, String> getAvailableCustomFieldMappings(final ExternalCustomField customField) {
        Map<String, String> fieldMappings = Maps.newLinkedHashMap();

		ArrayList<CustomField> customFields = Lists.newArrayList(
				Iterables.filter(utils.getCustomFieldManager().getGlobalCustomFieldObjects(),
						new Predicate<CustomField>() {
							public boolean apply(@Nullable CustomField input) {
								return input != null ? input.getCustomFieldType().getKey()
										.equals(customField.getTypeKey()) : false;
							}
						}));

		for(CustomField cf : customFields) {
			fieldMappings.put(cf.getName(), cf.getName());
		}

		if (!fieldMappings.containsKey(customField.getName())) {
			fieldMappings.put(customField.getName(), customField.getName());
		}

		return fieldMappings;
    }

	public Map<String, Map<String, String>> getAvailableFieldMappings(final ExternalCustomField customField) {
        Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();

        fieldMappings.put(getI18n().getText("admin.csv.import.mappings.custom.fields.header"),
                getAvailableCustomFieldMappings(customField));

        return fieldMappings;
	}

	@Nullable
	public String getProjectKey(String projectName) {
		return projectKeyMapping.get(projectName);
	}

    public String getProjectName(String projectName) {
		return StringUtils.defaultIfEmpty(projectNameMapping.get(projectName), projectName);
	}

	@Nullable
	public String getProjectLead(String projectName) {
		return projectLeadMapping.get(projectName);
	}

	protected void initializeProjectSelectionMapping() {
		final List<String> projectNames = getProjectNamesFromDb();
		for (String projectName : projectNames) {
			projectSelectionMapping.put(projectName, Boolean.TRUE.toString());
		}
	}

	public boolean isProjectSelected(String projectName) {
		if (projectSelectionMapping.isEmpty()) {
			initializeProjectSelectionMapping();
		}

		final String res = projectSelectionMapping.get(projectName);
		return res != null ? Boolean.valueOf(res) : false;
	}


	public void populateFieldMappings(Map actionParams, ErrorCollection errors) {
		for (final ExternalCustomField field : getCustomFields()) {
			String keyMapping = ParameterUtils.getStringParam(actionParams, field.getId());
			fieldMapping.put(field.getId(), keyMapping);
		}
	}

	public String getLinkMapping(String linkName) {
		return linkMapping.get(linkName);
	}

	public void populateLinkMappings(Map actionParams) {
		for (final String linkName : getLinkNamesFromDb()) {
			String keyMapping = ParameterUtils.getStringParam(actionParams, linkName);
			linkMapping.put(linkName, keyMapping);
		}
	}

	public Map<String, String> getAvailableLinkMappings() {
		List<IssueLinkType> list = new ArrayList<IssueLinkType>(utils.getIssueLinkTypeManager().getIssueLinkTypes());
		Collections.sort(list, new Comparator<IssueLinkType>() {
			public int compare(final IssueLinkType o1, final IssueLinkType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		Map<String, String> result = new HashMap<String, String>();
		for (IssueLinkType ilt : list) {
			result.put(ilt.getName(), ilt.getName());
		}
		return result;
	}


	public abstract List<String> getProjectNamesFromDb();

	public abstract List<ExternalCustomField> getCustomFields();

	public abstract List<String> getLinkNamesFromDb();

    /**
     * Resets ValueMappingHelper implementation used internally by the bean
     */
    public abstract void initializeValueMappingHelper();

	public ValueMappingHelper getValueMappingHelper() {
        if (valueMappingHelper == null) {
            initializeValueMappingHelper();
        }
        return valueMappingHelper;
    }

	public void validateJustBeforeImport(ErrorCollection errors) {
		// you can override it
	}

	protected void validateWorkflowSchemes(ErrorCollection errors) {
		for (String projectName : projectKeyMapping.keySet()) {
			if (isProjectSelected(projectName)) {
				final String projectKey = getProjectKey(projectName);
				final Project project = projectKey == null ? null : utils.getProjectManager().getProjectObjByKey(projectKey);
				if (project != null) { // project already exists - lets check some basic consistency
					if (!validateSelectedScheme(project)) {
						errors.addErrorMessage(
								getI18n().getText("jira-importer-plugin.importer.bugzilla.workflowscheme.violation",
										project.getName()));
					}
				}
			}
		}
	}

	private boolean validateSelectedScheme(Project project) {
		try {
			final GenericValue workflowScheme = utils.getWorkflowSchemeManager()
					.getWorkflowScheme(project.getGenericValue());
			if (workflowScheme == null) {
				if (valueMappingHelper.isWorkflowSchemeDefined()) {
					return false;
				}
			} else {
				if ((!valueMappingHelper.getWorkflowSchemeName().equals(workflowScheme.get("name")))) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public void copyFromProperties(InputStream is) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> configCopy = mapper.readValue(is, Map.class);

		copyFromProperties(configCopy, PROJECT_SELECTION_PREFIX, projectSelectionMapping);
		copyFromProperties(configCopy, PROJECT_KEY_CONFIG_PREFIX, projectKeyMapping);
		copyFromProperties(configCopy, PROJECT_NAME_CONFIG_PREFIX, projectNameMapping);
		copyFromProperties(configCopy, PROJECT_LEAD_CONFIG_PREFIX, projectLeadMapping);
		copyFromProperties(configCopy, FIELD_CONFIG_PREFIX, fieldMapping);
		copyFromProperties(configCopy, LINK_CONFIG_PREFIX, linkMapping);

		getValueMappingHelper().copyFromProperties(configCopy);
	}

	@Override
	public void copyToNewProperties(final Map<String, Object> configFile) {
		configFile.put(PROJECT_SELECTION_PREFIX, ImmutableMap.copyOf(projectSelectionMapping));
		configFile.put(PROJECT_KEY_CONFIG_PREFIX, ImmutableMap.copyOf(projectKeyMapping));
        configFile.put(PROJECT_NAME_CONFIG_PREFIX, ImmutableMap.copyOf(projectNameMapping));
        configFile.put(PROJECT_LEAD_CONFIG_PREFIX, ImmutableMap.copyOf(projectLeadMapping));
		configFile.put(FIELD_CONFIG_PREFIX, ImmutableMap.copyOf(fieldMapping));
		configFile.put(LINK_CONFIG_PREFIX, ImmutableMap.copyOf(linkMapping));

		getValueMappingHelper().copyToNewProperties(configFile);
	}

	private void copyFromProperties(Map<String, Object> configFile, String prefix, Map<String, String> mappings) {
		if (configFile.containsKey(prefix)) {
			mappings.putAll((Map<? extends String,? extends String>) configFile.get(prefix));
		}
	}

	public boolean isSelectedFieldMapping(String fieldName, String mappedField) {
		final String currentlySelected = getFieldMapping(fieldName);

		return currentlySelected != null && currentlySelected.equals(mappedField);
	}

    public String mapToIssueFieldValue(String mappedField) {
        return ISSUE_FIELD_MAPPING + mappedField;
    }

    public boolean isFieldMappedToIssueField(String fieldName) {
        return fieldMapping.containsKey(fieldName) ? StringUtils.defaultString(fieldMapping.get(fieldName), "").startsWith(ISSUE_FIELD_MAPPING) : false;
    }

    /**
     * @param fieldName
     * @return name of issue field that field is mapped to, null if field is not mapped to issue field
     */
    @Nullable
    public String getIssueFieldMapping(String fieldName) {
        return isFieldMappedToIssueField(fieldName)
                ? fieldMapping.get(fieldName).substring(ISSUE_FIELD_MAPPING.length()) : null;
    }

	public String getFieldMapping(String fieldName) {
		return fieldMapping.get(fieldName);
	}

    public void populateProjectKeyMappings(Map<String, ExternalProject> projectKeyMappings) {
        projectLeadMapping.clear();
        projectKeyMapping.clear();
        projectSelectionMapping.clear();
        projectNameMapping.clear();

        for(String projectName : getProjectNamesFromDb()) {
            boolean isChecked = projectKeyMappings.containsKey(projectName);
            projectSelectionMapping.put(projectName, Boolean.toString(isChecked));

            if (isChecked) {
                ExternalProject project = projectKeyMappings.get(projectName);
                projectKeyMapping.put(projectName, project.getKey());
                projectNameMapping.put(projectName, project.getName());
                projectLeadMapping.put(projectName, project.getLead());
            }
        }
    }

	private static final Predicate<CustomField> IS_GH_RANKING = new Predicate<CustomField>() {
		@Override
		public boolean apply(CustomField input) {
			return CustomFieldConstants.GH_RANKING_FIELD_TYPE.equals(input.getCustomFieldType().getKey());
		}
	};

	@Nullable
	public CustomField getCustomFieldNameForRanking() {
		final CustomField ranking = customFieldManager.getCustomFieldObjectByName("Rank");
		if (ranking != null && ranking.isGlobal() && IS_GH_RANKING.apply(ranking)) {
			return ranking;
		}

		final List<CustomField> globalCustomFields = customFieldManager.getGlobalCustomFieldObjects();
		return Iterables.find(globalCustomFields, IS_GH_RANKING, null);
	}
}
