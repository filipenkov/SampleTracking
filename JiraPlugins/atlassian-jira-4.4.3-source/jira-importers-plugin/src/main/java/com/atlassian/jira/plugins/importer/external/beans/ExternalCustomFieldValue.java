/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;

public class ExternalCustomFieldValue {
	private final Object value;
	private final String fieldType;
	private final String searcherType;
	private final String fieldName;

	public ExternalCustomFieldValue(String fieldName, String fieldType, @Nullable String searcherType, @Nullable Object value) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.searcherType = searcherType;
		this.value = value;
	}

	@Nullable
	public Object getValue() {
		return value;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	@Nullable
	public String getFieldType() {
		return fieldType;
	}

	@Nullable
	public String getSearcherType() {
		return searcherType;
	}

	@Nullable
	public String getFieldName() {
		return fieldName;
	}

}
