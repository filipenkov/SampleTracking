/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.List;

public interface ExternalVersionMapper extends ExternalObjectMapper {
	@Nullable
	List<ExternalVersion> buildFromMultiMap(Multimap<String, String> bean);
}
