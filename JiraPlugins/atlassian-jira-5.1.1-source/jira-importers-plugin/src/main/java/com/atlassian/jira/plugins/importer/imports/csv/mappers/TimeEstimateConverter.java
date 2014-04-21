/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

/**
 * Converts a raw string to a Long representing the number of seconds the time estimate field contains.
 */
public interface TimeEstimateConverter {
	public Long convertEstimate(String estimate);
}
