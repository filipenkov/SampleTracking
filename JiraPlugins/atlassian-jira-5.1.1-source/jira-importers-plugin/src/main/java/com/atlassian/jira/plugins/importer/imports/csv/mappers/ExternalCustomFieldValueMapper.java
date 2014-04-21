/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;

import java.util.List;

public interface ExternalCustomFieldValueMapper extends ExternalObjectMapper {
	List<ExternalCustomFieldValue> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log);
}
