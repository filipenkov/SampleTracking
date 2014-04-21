/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.model;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.List;

public class ProjectModel {
	public String name;
	public String key;
    public List<IssueTypeModel> issueTypes;

	public ProjectModel() {
	}

	public ProjectModel(String name, String key, List<IssueTypeModel> issueTypes) {
		this.name = name;
		this.key = key;
        this.issueTypes = issueTypes;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj); // for testing
	}
}
