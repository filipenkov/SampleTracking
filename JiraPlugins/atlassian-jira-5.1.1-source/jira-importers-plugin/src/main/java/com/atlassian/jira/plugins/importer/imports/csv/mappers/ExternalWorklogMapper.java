/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.List;

public interface ExternalWorklogMapper extends ExternalObjectMapper {
	@Nullable
    List<ExternalWorklog> buildFromMultiMap(Multimap<String, String> bean, ExternalUserNameMapper userNameMapper, ImportLogger log);
}
