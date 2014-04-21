/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;

/**
 * Interface which defines a how to extract ExternalUser objects from data in the CSV file.
 * <p>
 * The default implementation is {@link com.atlassian.jira.plugins.importer.imports.csv.mappers.FullNameUserMapper}.
 * Other mappers may be dynamically loaded via reflection and used depending on configuration settings.
 * All classes must have a UserMapper(String userField, String defaultEmailSuffix) constructor for dynamic construction.
 * </p>
 * <p/>
 * TODO: This cannot handle multi-selects currently.
 *
 * @see com.atlassian.jira.plugins.importer.imports.csv.CsvConfiguration#getCustomUserMappers() For dynamic construction of implementors.
 */
public interface ExternalUserMapper extends ExternalObjectMapper {
	String DEFAULT_EMAIL_SUFFIX = "@example.com";

	@Nullable
	ExternalUser buildFromMultiMap(Multimap<String, String> bean);
}
