/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class PivotalTimeShiftsParserTest {
	@Test
	public void testParsingSampleCSV() throws Exception {
		final String in = IOUtils.toString(getClass().getResourceAsStream("/pivotal/timeshift.csv"));
		final Collection<ExternalWorklog> worklog = new PivotalTimeShiftsParser(in, new MockUserNameMapper("Slawek Ginter", "Translated Slawek Ginter")).parseWorklog();

		assertEquals(2, worklog.size());

		final ExternalWorklog item1 = Iterables.get(worklog, 0);
		assertEquals("Translated Slawek Ginter", item1.getAuthor());
		assertEquals("", item1.getComment());
		assertEquals(new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-21")).toInstant(), item1.getStartDate().toInstant());
		assertEquals((Long) TimeUnit.SECONDS.convert(8, TimeUnit.HOURS), item1.getTimeSpent());

		final ExternalWorklog item2 = Iterables.get(worklog, 1);
		assertEquals("Translated Slawek Ginter", item2.getAuthor());
		assertEquals("six hours", item2.getComment());
		assertEquals(new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-22")).toInstant(), item2.getStartDate().toInstant());
		assertEquals((Long) TimeUnit.SECONDS.convert(6, TimeUnit.HOURS), item2.getTimeSpent());

	}
}
