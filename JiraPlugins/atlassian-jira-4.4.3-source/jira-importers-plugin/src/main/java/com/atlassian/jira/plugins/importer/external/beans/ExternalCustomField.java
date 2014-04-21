/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Represents a custom field object and its configuration.
 */
public class ExternalCustomField {
	private final String id;
	private final String name;
	private final String typeKey;
	private final String searcherKey;
	private AbstractValueMappingDefinition valueMappingDefinition;
	private Collection<String> valueSet;

	public ExternalCustomField(final String id, final String name, final String typeKey, final String searcherKey) {
		if ((id == null) || (name == null) || (typeKey == null) || (searcherKey == null)) {
			throw new IllegalArgumentException("Can not construct an ExternalCustomField with null arguments.");
		}
		this.id = id;
		this.name = name;
		this.typeKey = typeKey;
		this.searcherKey = searcherKey;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTypeKey() {
		return typeKey;
	}

	public String getSearcherKey() {
		return searcherKey;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public boolean equals(final Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public static ExternalCustomField createText(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.TEXT_FIELD_TYPE,
				CustomFieldConstants.TEXT_FIELD_SEARCHER);
	}

	public static ExternalCustomField createSelect(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.SELECT_FIELD_TYPE,
				CustomFieldConstants.SELECT_FIELD_SEARCHER);
	}

	public static ExternalCustomField createMultiSelect(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.MULTISELECT_FIELD_TYPE,
				CustomFieldConstants.MULTISELECT_FIELD_SEARCHER);
	}

	public static ExternalCustomField createFreeText(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.FREE_TEXT_FIELD_TYPE,
				CustomFieldConstants.TEXT_FIELD_SEARCHER);
	}

	public static ExternalCustomField createDatePicker(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.DATE_PICKER_FIELD_TYPE,
				CustomFieldConstants.DATE_FIELD_SEARCHER);
	}

	public static ExternalCustomField createDatetime(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.DATETIME_FIELD_TYPE,
				CustomFieldConstants.DATETIME_FIELD_SEARCHER);
	}

	public static ExternalCustomField createNumber(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.NUMBER_FIELD_TYPE,
				CustomFieldConstants.NUMBER_FIELD_SEARCHER);
	}

	public static ExternalCustomField createRadio(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.RADIO_FIELD_TYPE,
				CustomFieldConstants.RADIO_FIELD_SEARCHER);
	}

	public static ExternalCustomField createCheckboxes(String id, String name) {
		return new ExternalCustomField(id, name, CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE,
				CustomFieldConstants.MULTICHECKBOXES_FIELD_SEARCHER);
	}

	@Nullable
	public AbstractValueMappingDefinition getValueMappingDefinition() {
		return valueMappingDefinition;
	}

	public void setValueMappingDefinition(@Nullable AbstractValueMappingDefinition valueMappingDefinition) {
		this.valueMappingDefinition = valueMappingDefinition;
	}

	/**
	 * Returns the set of globally configured possible values for this custom field.
	 *
	 * @return set of globally configured values for this custom fields or null if such set is not defined
	 *
	 * @since JIM 2.6.0
	 */
	@Nullable
	public Collection<String> getValueSet() {
		return valueSet;
	}

	public void setValueSet(@Nullable Collection<String> valueSet) {
		this.valueSet = valueSet;
	}

	public ExternalCustomFieldValue createValue(@Nullable Object value) {
		if (value != null) {
			if (CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE.equals(getTypeKey())
					|| CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(getTypeKey())
					|| CustomFieldConstants.VERSION_PICKER_TYPE.equals(getTypeKey())) {
				if (!(value instanceof Collection)) {
					throw new IllegalArgumentException(
							String.format("Unable to create a value for custom field %s: invalid type: %s; must be a Collection",
									getName(), value.getClass().toString()));
				}
			} else if (!(value instanceof String)) {
				throw new IllegalArgumentException(
					String.format("Unable to create a value for custom field %s: invalid type: %s; must be a String",
							getName(), value.getClass().toString()));
			}
		}
		return new ExternalCustomFieldValue(getName(), getTypeKey(), getSearcherKey(), value);
	}
}
