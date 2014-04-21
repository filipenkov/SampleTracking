/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.List;

public interface ExternalComponentMapper extends ExternalObjectMapper {
	@Nullable
	List<ExternalComponent> buildFromMultiMap(Multimap<String, String> bean);
}
