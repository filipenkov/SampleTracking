/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExternalProject implements NamedExternalObject {
	private Long jiraId;
	private String id;
	private String externalName;
	private String name;
	private String key;
	private String url;
	private String lead;
	private String description;
	private String projectCategoryName;
	private Long assigneeType;

	public ExternalProject() {
	}

	public ExternalProject getClone() {
		return new ExternalProject().setJiraId(this.jiraId)
				.setId(this.id).setExternalName(this.externalName)
				.setName(this.name).setKey(this.key).setUrl(this.url)
				.setLead(this.lead).setDescription(this.description)
				.setProjectCategoryName(this.projectCategoryName)
				.setAssigneeType(this.assigneeType);
	}

	public ExternalProject(@Nullable String name, @Nullable String key) {
		this.externalName = name;
		this.name = name;
		this.key = key;
	}

	public ExternalProject(@Nullable String name, @Nullable String key, @Nullable String lead) {
		this.externalName = name;
		this.name = name;
		this.key = key;
		this.lead = lead;
	}

	/**
	 * null indicates that default JIRA workflow scheme ("None") should be used
	 */
	@Nullable
	private String workflowSchemeName;

	@Nullable
	public Long getJiraId() {
		return jiraId;
	}

	public ExternalProject setJiraId(@Nullable Long jiraId) {
		this.jiraId = jiraId;
		return this;
	}

	@Nullable
	public String getId() {
		return id;
	}

	public ExternalProject setId(@Nullable String id) {
		this.id = id;
		return this;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public ExternalProject setName(@Nullable String name) {
		this.name = name;
		return this;
	}

	@Nullable
	public String getKey() {
		return key;
	}

	public ExternalProject setKey(@Nullable String key) {
		this.key = key;
		return this;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	public ExternalProject setUrl(@Nullable String url) {
		this.url = url;
		return this;
	}

	@Nullable
	public String getLead() {
		return lead;
	}

	public ExternalProject setLead(@Nullable String lead) {
		this.lead = lead;
		return this;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public ExternalProject setDescription(@Nullable String description) {
		this.description = description;
		return this;
	}

	@Nullable
	public String getProjectCategoryName() {
		return projectCategoryName;
	}

	public ExternalProject setProjectCategoryName(@Nullable String projectCategoryName) {
		this.projectCategoryName = projectCategoryName;
		return this;
	}

	@Nullable
	public Long getAssigneeType() {
		return assigneeType;
	}

	public ExternalProject setAssigneeType(@Nullable final Long assigneeType) {
		this.assigneeType = assigneeType;
		return this;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ExternalProject)) {
			return false;
		}

		final ExternalProject rhs = (ExternalProject) o;
		return new EqualsBuilder()
				.append(getId(), rhs.getId())
				.append(getKey(), rhs.getKey())
				.append(getWorkflowSchemeName(), rhs.getWorkflowSchemeName())
				.append(getName(), rhs.getName())
				.append(getExternalName(), rhs.getExternalName())
				.append(getDescription(), rhs.getDescription())
				.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).
				append(getId()).
				append(getKey()).
				append(getName()).
				append(getExternalName()).
				append(getWorkflowSchemeName()).
				append(getDescription()).
				toHashCode();
	}

	public ExternalProject setWorkflowSchemeName(@Nullable String workflowSchemeName) {
		this.workflowSchemeName = workflowSchemeName;
		return this;
	}

	@Nullable
	public String getWorkflowSchemeName() {
		return workflowSchemeName;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Nullable
	public String getExternalName() {
		return externalName;
	}

	public ExternalProject setExternalName(@Nullable String externalName) {
		this.externalName = externalName;
		return this;
	}

	public void setField(String fieldName, final String value)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method setter = this.getClass().getDeclaredMethod("set" + StringUtils.capitalize(fieldName), String.class);
		setter.invoke(this, value);
	}
}
