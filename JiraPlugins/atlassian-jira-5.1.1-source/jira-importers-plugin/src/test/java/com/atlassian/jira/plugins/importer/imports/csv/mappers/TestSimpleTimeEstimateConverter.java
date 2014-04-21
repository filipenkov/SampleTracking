/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleTimeEstimateConverter;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.TimeEstimateConverter;
import junit.framework.TestCase;

/**
 *
 */
public class TestSimpleTimeEstimateConverter extends TestCase {
	public void testConverterWorks() {
		TimeEstimateConverter timeEstimateConverter = new SimpleTimeEstimateConverter();
		assertEquals(new Long(60000), timeEstimateConverter.convertEstimate("60000"));
		try {
			timeEstimateConverter.convertEstimate("hello");
			fail("The converter should explode on non numbers");
		}
		catch (NumberFormatException nfe) {
			// Woo hoo we exploded
		}
	}
}
