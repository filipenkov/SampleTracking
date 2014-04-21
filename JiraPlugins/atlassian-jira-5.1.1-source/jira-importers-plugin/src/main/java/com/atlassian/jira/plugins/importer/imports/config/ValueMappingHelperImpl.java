/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValueMappingHelperImpl implements ValueMappingHelper {

	private static final Logger log = Logger.getLogger(ValueMappingHelperImpl.class);

	private static final String WORKFLOW_SCHEME_NAME = "workflowScheme";

	private final Set<String> fieldsForValueMapping = Sets.newLinkedHashSet();
	private final MultiKeyMap valueMapping = new MultiKeyMap();

	private Map<String, Collection<String>> distinctValuesCache;
	private MultiKeyMap valueMappingFieldNames;
	private String workflowSchemeName;

	private final WorkflowSchemeManager workflowSchemeManager;
	private final WorkflowManager workflowManager;
	protected final Map<String, ValueMappingDefinition> mappingDefinitions;

	private final ConstantsManager constantsManager;

	public ValueMappingHelperImpl(WorkflowSchemeManager workflowSchemeManager, WorkflowManager workflowManager,
			ValueMappingDefinitionsFactory valueMappingDefinitionsFactory, ConstantsManager constantsManager) {

		this.workflowSchemeManager = workflowSchemeManager;
		this.workflowManager = workflowManager;
		this.constantsManager = constantsManager;
		this.mappingDefinitions = Maps.newLinkedHashMap();

		for (ValueMappingDefinition mappingDefinition : valueMappingDefinitionsFactory.createMappingDefinitions(this)) {
			this.mappingDefinitions.put(mappingDefinition.getExternalFieldId(), mappingDefinition);
		}
		initializeDefaultValues();
		initializeFieldsForValueMapping();
		workflowSchemeName = getDefaultWorkflowName();
	}

	public void initDistinctValuesCache() {
		distinctValuesCache = getDistinctValues(getAvailableFields());
	}

	// ------------------------------------------------------------------------------------- Field to map Value Mappings
	public Collection<ValueMappingDefinition> getAvailableFields() {
		return mappingDefinitions.values();
	}

	// todo: JIM428
	public Long getValueCountForField(String fieldName) {
		Collection distinctValues = getDistinctValuesForField(fieldName);
		if (distinctValues != null) {
			return (long) distinctValues.size();
		} else {
			return null;
		}
	}

	public boolean isMapValueForField(String fieldName) {
		return fieldsForValueMapping.contains(fieldName);
	}

	private void initializeFieldsForValueMapping() {
		fieldsForValueMapping.clear();
		for (ValueMappingDefinition definition : getAvailableFields()) {
			if (definition.isMandatory()) {
				fieldsForValueMapping.add(definition.getExternalFieldId());
			}
		}
	}

	public void populateFieldForValueMappings(Map actionParams) {
		initializeFieldsForValueMapping();
		populateSchemesMappings(actionParams);
		final Collection<ValueMappingDefinition> fields = getAvailableFields();
		for (ValueMappingDefinition definition : fields) {
			final String fieldName = definition.getExternalFieldId();
			final String keyMapping = ParameterUtils.getStringParam(actionParams, fieldName);
			if (BooleanUtils.toBoolean(keyMapping)) {
				fieldsForValueMapping.add(fieldName);
			}
		}
	}

	public Collection<String> getFieldsForValueMapping() {
		return fieldsForValueMapping;
	}

	public Collection<String> getDistinctValuesForField(String fieldName) {
		return distinctValuesCache.get(fieldName);
	}

	public String getValueMappingFieldName(String fieldName, String value) {
		if (valueMappingFieldNames == null) {
			valueMappingFieldNames = new MultiKeyMap();
			for (ValueMappingDefinition fieldForMapping : getAvailableFields()) {
				final String fieldNameForMapping = fieldForMapping.getExternalFieldId();
				final Collection<String> distinctValues = getDistinctValuesForField(fieldNameForMapping);
				if (distinctValues != null) {
                    int idx = 0;
					for (String distinctValue : distinctValues) {
						valueMappingFieldNames.put(fieldNameForMapping, distinctValue,
								"value_mapping_for_" + fieldNameForMapping + "_" + idx++);
					}
				}
			}
		}

		return (String) valueMappingFieldNames.get(fieldName, value);
	}

	public String getValueMapping(String fieldName, String value) {
		return (String) valueMapping.get(fieldName, value);
	}

	public void populateValueMappings(Map actionParams) {
		valueMapping.clear();
		for (String fieldNameForMapping : getFieldsForValueMapping()) {
			final Collection<String> distinctValues = getDistinctValuesForField(fieldNameForMapping);
			if (distinctValues != null) {
				for (String distinctValue : distinctValues) {
					String htmlFieldName = getValueMappingFieldName(fieldNameForMapping, distinctValue);
					String mappedValue = ParameterUtils.getStringParam(actionParams, htmlFieldName);
					valueMapping.put(fieldNameForMapping, distinctValue, mappedValue);
				}
			}
		}
	}

	@Nullable
	public Collection<ValueMappingEntry> getTargetValues(String fieldName) {
		final ValueMappingDefinition mappingDefinition = mappingDefinitions.get(fieldName);
		if (mappingDefinition != null) {
			return mappingDefinition.getTargetValues();
		}
		return null;
	}


	@Nullable
	public ValueMappingDefinition getValueMappingDefinition(String fieldName) {
		return mappingDefinitions.get(fieldName);
	}

	private void initializeDefaultValues() {
		final Collection<ValueMappingDefinition> definitions = getAvailableFields();
		for (ValueMappingDefinition definition : definitions) {
			final String fieldName = definition.getExternalFieldId();
			final Collection<ValueMappingEntry> defaultValues = definition.getDefaultValues();
			for (ValueMappingEntry defaultValue : defaultValues) {
				valueMapping.put(fieldName, defaultValue.getName(), defaultValue.getId());
			}
		}
	}

	public void copyFromProperties(final Map<String, Object> configFile) {
		// Copy the value mappings
		Collection<ValueMappingDefinition> fieldsAvailableForMapValues = getAvailableFields();
		final Map<String, Object> config = (Map<String, Object>) configFile.get(VALUE_CONFIG_PREFIX);

		for (ValueMappingDefinition valueMappingDefinition : fieldsAvailableForMapValues) {
			final String fieldName = valueMappingDefinition.getExternalFieldId();
			final Map<String, String> valueConfig = (Map<String, String>) config.get(fieldName);
			if (valueConfig == null) {
				continue;
			}
			for(Map.Entry<String, String> entry : valueConfig.entrySet()) {
				fieldsForValueMapping.add(fieldName);
				final String mappedValue = entry.getValue();
				valueMapping.put(fieldName, entry.getKey(), "".equals(mappedValue) ? NULL_VALUE : mappedValue);
			}
		}

		if (config.containsKey(WORKFLOW_SCHEME_NAME) && StringUtils
				.isNotEmpty((String) config.get(WORKFLOW_SCHEME_NAME))) {
			workflowSchemeName = (String) config.get(WORKFLOW_SCHEME_NAME);
		} else {
			workflowSchemeName = getDefaultWorkflowName();
		}
	}

	public void copyToNewProperties(final Map<String, Object> configFile) {
		final Map<String, Object> configCopy = Maps.newHashMap();

		configFile.put(VALUE_CONFIG_PREFIX, configCopy);

		for (Object vm : valueMapping.entrySet()) {
			Map.Entry entry = (Map.Entry) vm;
			MultiKey key = (MultiKey) entry.getKey();
			String targetMapping = (String) entry.getValue();
			if (StringUtils.isNotBlank(targetMapping)) {
				if (!configCopy.containsKey(key.getKey(0))) {
					configCopy.put(key.getKey(0).toString(), Maps.<String, String>newHashMap());
				}

				((Map<String, String>) configCopy.get(key.getKey(0))).put(key.getKey(1).toString(),
						NULL_VALUE.equals(targetMapping) ? "" : targetMapping);
			}
		}

		configCopy.put(WORKFLOW_SCHEME_NAME, isWorkflowSchemeDefined() ? getWorkflowSchemeName() : "");
	}


	public String getValueMappingForImport(String fieldName, String value) {
		if (!isMapValueForField(fieldName)) {
			return value;
		}
		String mappedValue = getValueMapping(fieldName, value);
		if (StringUtils.isNotBlank(mappedValue)) {
			if (!NULL_VALUE.equals(mappedValue)) {
				return mappedValue;
			} else {
				return "";
			}
		} else {
			return value;
		}
	}

	public Collection<String> getAvailableWorkflowSchemes() {
		try {
			final List<GenericValue> schemes = workflowSchemeManager.getSchemes();
			ArrayList<String> res = new ArrayList<String>(schemes.size());
			for (GenericValue scheme : schemes) {
				res.add(scheme.getString("name"));
			}
			return res;
		} catch (GenericEntityException e) {
			log.error(e);
			return Collections.emptyList();
		}
	}

	public String getDefaultWorkflowName() {
		return "[none-default-workflow]";
	}

	public String getWorkflowSchemeName() {
		return workflowSchemeName;
	}

	@Nullable
	public JiraWorkflow getSelectedWorkflow() {
		if (getDefaultWorkflowName().equals(workflowSchemeName)) {
			return workflowManager.getWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
		} else {

			try {
				final GenericValue selectedScheme = workflowSchemeManager.getScheme(workflowSchemeName);
				return getWorkflowForBugs(selectedScheme);
			} catch (GenericEntityException e) {
				log.error(e);
				return null;
			}
		}
	}

	@Nullable
	public JiraWorkflow getWorkflowForBugs(GenericValue scheme) {
		final IssueType issueType = getIssueType();
		if (issueType == null) {
			return null;
		}
		return workflowManager.getWorkflowFromScheme(scheme, issueType.getId());
	}

	public boolean isWorkflowSchemeDefined() {
		return !getDefaultWorkflowName().equals(getWorkflowSchemeName());
	}

	public void populateSchemesMappings(Map actionParams) {
		final String workflowScheme = ParameterUtils.getStringParam(actionParams, WORKFLOW_SCHEME_NAME);
		if (workflowScheme != null) {
			workflowSchemeName = workflowScheme;
		}
	}

	@Nullable
	private IssueType getIssueType() {
		final Collection<IssueType> issueTypes = constantsManager.getAllIssueTypeObjects();
		for (IssueType issueType : issueTypes) {
			if ("bug".equalsIgnoreCase(issueType.getName())) {
				return issueType;
			}
		}
		return issueTypes.size() > 0 ? issueTypes.iterator().next() : null;
	}

	private Map<String, Collection<String>> getDistinctValues(final Collection<ValueMappingDefinition> availableFields) {
		Map<String, Collection<String>> distinctValues = Maps.newHashMap();
		for (ValueMappingDefinition valueMappingDefinition : availableFields) {
			distinctValues.put(valueMappingDefinition.getExternalFieldId(), valueMappingDefinition.getDistinctValues());
		}

		return distinctValues;
	}


}
