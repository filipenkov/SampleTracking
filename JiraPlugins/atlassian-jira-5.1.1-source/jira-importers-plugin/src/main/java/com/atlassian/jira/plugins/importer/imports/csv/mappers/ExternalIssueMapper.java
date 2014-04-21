/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;

public interface ExternalIssueMapper extends ExternalObjectMapper {
	ExternalIssue buildFromMultiMap(Multimap<String, String> bean, ImportLogger log);

	void setTimeTrackingConverter(TimeEstimateConverter timeEstimateConverter);
}
