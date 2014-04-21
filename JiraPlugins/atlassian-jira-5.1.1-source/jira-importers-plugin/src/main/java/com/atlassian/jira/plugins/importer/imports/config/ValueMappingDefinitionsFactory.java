/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.config;

import java.util.List;

public interface ValueMappingDefinitionsFactory {
	List<ValueMappingDefinition> createMappingDefinitions(final ValueMappingHelper valueMappingHelper);
}
