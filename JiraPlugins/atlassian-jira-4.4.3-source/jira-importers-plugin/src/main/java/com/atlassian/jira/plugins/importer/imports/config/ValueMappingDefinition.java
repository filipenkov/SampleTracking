/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.config;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Interface that defines what a field being mapped in an importer looks like
 */
public interface ValueMappingDefinition {
	/**
	 * The name of the field being mapped, usually corresponds to the field name from the data source
	 *
	 * @return String name of field
	 */
	String getExternalFieldId();

	@Nullable
	String getDescription();

	/**
	 * Returns the number of unique values for this field
	 *
	 * @return Number of unique values. If null, then the count will be "Unknown". This may be done for perfomance reasons.
	 */
	@Nullable
	Set<String> getDistinctValues();

	/**
	 * If the field the mapping definition will map to in JIRA is known, else null
	 *
	 * @return Returns the id of the field being mapped. This should match {@link com.atlassian.jira.issue.fields.Field#getId()} of the corresponding field
	 */
	@Nullable
	String getJiraFieldId();

	@Nullable
	Collection<ValueMappingEntry> getTargetValues();

	Collection<ValueMappingEntry> getDefaultValues();

	boolean canBeBlank();

	boolean canBeImportedAsIs();

	boolean canBeCustom();

	boolean isMandatory();

}
