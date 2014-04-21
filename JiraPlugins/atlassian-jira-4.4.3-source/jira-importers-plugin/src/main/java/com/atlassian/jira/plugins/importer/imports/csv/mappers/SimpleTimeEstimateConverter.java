/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

/**
 * Simple time estimate converter that assumes the string represents a long value.
 */
public class SimpleTimeEstimateConverter implements TimeEstimateConverter {
	/**
	 * Converts the estimate string into a long
	 *
	 * @param estimate the string representation of a long that is the number of seconds.
	 * @return a java.lang.Long representation of the estimate
	 * @throws NumberFormatException thrown if the passed in string is not a valid long
	 */
	public Long convertEstimate(String estimate) {
		return new Long(estimate);
	}
}
