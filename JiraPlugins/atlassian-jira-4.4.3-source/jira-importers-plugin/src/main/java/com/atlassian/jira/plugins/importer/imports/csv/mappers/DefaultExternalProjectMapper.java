/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.Collection;

public class DefaultExternalProjectMapper implements ExternalProjectMapper {
	private static final String PROJECT = "project";

	@Override
	@Nullable
	public ExternalProject buildFromMultiMap(Multimap<String, String> bean) {
		ExternalProject externalProject = new ExternalProject();
		if (!bean.get(PROJECT + SP + "id").isEmpty())
			externalProject.setId((String) ((Collection) bean.get(PROJECT + SP + "id")).iterator().next());
		if (!bean.get(PROJECT + SP + "name").isEmpty())
			externalProject.setName((String) ((Collection) bean.get(PROJECT + SP + "name")).iterator().next());
		if (!bean.get(PROJECT + SP + "lead").isEmpty())
			externalProject.setLead((String) ((Collection) bean.get(PROJECT + SP + "lead")).iterator().next());
        if (!bean.get(PROJECT + SP + "url").isEmpty())
			externalProject.setUrl((String) ((Collection) bean.get(PROJECT + SP + "url")).iterator().next());
		if (!bean.get(PROJECT + SP + "description").isEmpty()) externalProject
				.setDescription((String) ((Collection) bean.get(PROJECT + SP + "description")).iterator().next());
		if (!bean.get(PROJECT + SP + "key").isEmpty())
			externalProject.setKey((String) ((Collection) bean.get(PROJECT + SP + "key")).iterator().next());

		if (externalProject.getId() == null &&
				externalProject.getName() == null &&
				externalProject.getKey() == null) {
			return null;
		} else {
			return externalProject;
		}
	}
}
