/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.config;

import com.atlassian.jira.workflow.JiraWorkflow;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Map;

public interface ValueMappingHelper {
	static final String VALUE_CONFIG_PREFIX = "value";
	static final String NULL_VALUE = "<<blank>>";

	/**
	 * Initialize the distinct values cache just before using Value Mapping Helper for page rendering.
	 * The intent is to catch potential RuntimeExceptions from underlaying layers where they can be handled, not in VM
	 */
	void initDistinctValuesCache();

	/**
	 * Return {@link com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition} objects for each field that can have their values mapped
	 *
	 * @return List of {@link com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition}. Never null
	 */
	Collection<ValueMappingDefinition> getAvailableFields();

	/**
	 * Returns a count of the number of unique values for that field
	 *
	 * @param fieldName
	 * @return Null if not calculated.
	 */
	Long getValueCountForField(String fieldName);

	/**
	 * Returns wheteher or not the field has been selected for mapping
	 *
	 * @param fieldName
	 * @return true if field is selected for mappping
	 */
	boolean isMapValueForField(String fieldName);

	void populateFieldForValueMappings(Map actionParams);


	/**
	 * Returns a collection of {@link com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition} objects that have been selected for value mappings
	 *
	 * @return Collection of {@link com.atlassian.jira.plugins.importer.imports.config.ValueMappingDefinition}. never null
	 */
	Collection<String> getFieldsForValueMapping();

	/**
	 * Returns a collection of {@link String} objects representing a unique value of the field
	 *
	 * @param fieldName
	 * @return Collection of {@link String}. Never null
	 */
	Collection getDistinctValuesForField(String fieldName);

	/**
	 * Returns the HTML field name for the input area for a given field and value combination
	 *
	 * @param fieldName
	 * @param value
	 * @return a HTML field name
	 */
	String getValueMappingFieldName(String fieldName, String value);

	/**
	 * Gets whatever value the field and value pair is mapped to. {@link #NULL_VALUE} if the field is to be cleared,
	 * blank if it is to be imported as-is.
	 *
	 * @param fieldName
	 * @param value
	 * @return String of the what the pair is mapped to
	 */
	String getValueMapping(String fieldName, String value);

	void populateValueMappings(Map actionParams);

	Collection<ValueMappingEntry> getTargetValues(String fieldName);

	ValueMappingDefinition getValueMappingDefinition(String fieldName);


	void copyFromProperties(final Map<String, Object> configFile);

	void copyToNewProperties(final Map<String, Object> configFile);

	/**
	 * Gets whatever value the field and value pair is mapped to for import. That is, will return an empty string if the
	 * value is cleaered and the original value if no mapping is needed
	 *
	 * @param fieldName
	 * @param value
	 * @return String of the what the pair is mapped to
	 */
	String getValueMappingForImport(String fieldName, String value);

	Collection<String> getAvailableWorkflowSchemes();

	String getDefaultWorkflowName();

	/**
	 * @return selected workflow scheme to be used by the import
	 */
	String getWorkflowSchemeName();

	JiraWorkflow getSelectedWorkflow();

	void populateSchemesMappings(Map actionParams);

	boolean isWorkflowSchemeDefined();

	JiraWorkflow getWorkflowForBugs(GenericValue scheme);

}
