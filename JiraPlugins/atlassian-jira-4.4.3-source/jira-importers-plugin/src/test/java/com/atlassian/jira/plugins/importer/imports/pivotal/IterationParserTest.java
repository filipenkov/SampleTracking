/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.pivotal;

import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ofbiz.core.util.UtilDateTime;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IterationParserTest {

	private final IterationParser parser = new IterationParser();
	private static Element rootElement;

	@BeforeClass
	public static void setUp() throws Exception {
		rootElement = ProjectParserTest.getRootElement("/pivotal/iterations.xml");
	}


	@Test
	public void testParseIteration() {
		final DateTimeZone defaultTimeZone = DateTimeZone.getDefault();
		try {
			DateTimeZone.setDefault(DateTimeZone.UTC);

			PivotalIteration iteration = parser.parseIteration(rootElement.getChild("iteration"));
			assertNotNull(iteration);
			assertEquals(new DateTime(2011, 2, 13, 23, 00, 00, 0, DateTimeZone.UTC).toDate(), iteration.getStart().toDate());
			final DateTime finishDate = new DateTime(2011, 2, 20, 23, 00, 00, 0, DateTimeZone.UTC);
			assertEquals(finishDate.toDate(), iteration.getFinish().toDate());
			assertEquals("1", iteration.getId());

			final Locale defaultLocale = Locale.getDefault();
			try {
				Locale.setDefault(Locale.ENGLISH);
				assertEquals("1 : 13 Feb", iteration.getName());

				assertEquals(1, iteration.getStories().size());
			} finally {
				Locale.setDefault(defaultLocale);
			}
		} finally {
			DateTimeZone.setDefault(defaultTimeZone);
		}
	}

	@Test
	public void testParseIterations() {
		List<PivotalIteration> iterations = parser.parseIterations(rootElement);
		assertNotNull(iterations);
		assertEquals(9, iterations.size());
		assertEquals(0, iterations.get(1).getStories().size());
		assertEquals(1, iterations.get(8).getStories().size());
	}
}
