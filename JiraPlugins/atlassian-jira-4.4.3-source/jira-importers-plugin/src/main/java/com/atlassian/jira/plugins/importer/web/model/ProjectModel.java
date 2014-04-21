/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web.model;

import org.apache.commons.lang.builder.EqualsBuilder;

public class ProjectModel {
	public String name;
	public String key;
	public String lead;
	public boolean editable;

	public ProjectModel() {
	}

	public ProjectModel(String name, String key, String lead, boolean editable) {
		this.name = name;
		this.key = key;
		this.lead = lead;
		this.editable = editable;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj); // for testing
	}
}
