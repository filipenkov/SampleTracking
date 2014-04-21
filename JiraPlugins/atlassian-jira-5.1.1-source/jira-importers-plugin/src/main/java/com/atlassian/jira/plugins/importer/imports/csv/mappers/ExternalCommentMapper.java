/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;

import java.util.List;

public interface ExternalCommentMapper {
	List<ExternalComment> buildFromMultiMap(Multimap<String, String> bean, ExternalUserNameMapper userNameMapper, ImportLogger log);
}
