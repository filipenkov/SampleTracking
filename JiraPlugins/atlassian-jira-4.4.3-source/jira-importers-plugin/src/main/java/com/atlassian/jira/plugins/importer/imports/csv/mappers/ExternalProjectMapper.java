/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.google.common.collect.Multimap;

public interface ExternalProjectMapper extends ExternalObjectMapper {
	ExternalProject buildFromMultiMap(Multimap<String, String> bean);
}
