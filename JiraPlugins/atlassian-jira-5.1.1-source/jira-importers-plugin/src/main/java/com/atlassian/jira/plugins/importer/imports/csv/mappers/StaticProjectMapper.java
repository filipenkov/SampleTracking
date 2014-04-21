/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.google.common.collect.Multimap;

public class StaticProjectMapper implements ExternalProjectMapper {
	ExternalProject project;

	public StaticProjectMapper(ExternalProject project) {
		this.project = project;
	}

	public ExternalProject buildFromMultiMap(Multimap<String, String> bean) {
		return project;
	}
}
