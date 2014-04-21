package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class ExternalHistoryItem {
	private final String fieldType;
	private final String field;
	private final String oldValue;
	private final String oldDisplayValue;
	private final String newValue;
	private final String newDisplayValue;

	@JsonCreator
	public ExternalHistoryItem(@JsonProperty("fieldType") String fieldType, @JsonProperty("field") String field,
			@JsonProperty("from") String oldValue, @JsonProperty("fromString") String oldDisplayValue,
			@JsonProperty("to") String newValue, @JsonProperty("toString") String newDisplayValue) {
		this.fieldType = fieldType;
		this.field = field;
		this.oldValue = oldValue;
		this.oldDisplayValue = oldDisplayValue;
		this.newValue = newValue;
		this.newDisplayValue = newDisplayValue;
	}

	public String getFieldType() {
		return fieldType;
	}

	public String getField() {
		return field;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getOldDisplayValue() {
		return oldDisplayValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public String getNewDisplayValue() {
		return newDisplayValue;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
