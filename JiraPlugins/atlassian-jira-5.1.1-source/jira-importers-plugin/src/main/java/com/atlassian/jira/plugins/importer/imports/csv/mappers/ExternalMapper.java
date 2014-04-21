/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;

public interface ExternalMapper<T> {
	Iterable<T> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log);
}
