/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.external.beans;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestExternalIssue {
	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-403
	 *
	 * Settings resolution to empty string caused {@link com.atlassian.jira.issue.history.ChangeLogUtils} to break
	 * with a NullPointerException in case Oracle was used (because Oracle stores empty strings as NULL)
	 *
	 * We know intentionally change empty strings to null in ExternalIssue
	 */
	@Test
	public void testEmptyStrings() {
		ExternalIssue i = new ExternalIssue();
		i.setStatus("");
		assertNull(i.getStatus());

		i.setResolution("");
		assertNull(i.getResolution());

		i.setAssignee("");
		assertNull(i.getAssignee());

		i.setDescription("");
		assertNull(i.getDescription());

		i.setEnvironment("");
		assertNull(i.getEnvironment());

		i.setIssueType("");
		assertNull(i.getIssueType());

		i.setSummary("");
		assertNull(i.getSummary());
	}

	@Test
	public void testCopyingConstructorForRegressions() throws Exception {
		final String created = "2011-05-06 12:00";
		final String updated = "2011-05-06 13:00";
		final String duedate = "2011-05-07 13:00";
		final String resoulutionDate = "2011-05-08 13:00";


		final ExternalIssue orig = new ExternalIssue();
		orig.setCreated(dateFormat.parseDateTime(created).toDate());
		orig.setUpdated(dateFormat.parseDateTime(updated).toDate());
		orig.setDuedate(dateFormat.parseDateTime(duedate).toDate());
		orig.setResolutionDate(dateFormat.parseDateTime(resoulutionDate).toDate());

		final ExternalIssue copy = new ExternalIssue(orig);

		assertEquals(created, dateFormat.print(copy.getCreated()));
		assertEquals(updated, dateFormat.print(copy.getUpdated()));
		assertEquals(duedate, dateFormat.print(copy.getDuedate()));
		assertEquals(resoulutionDate, dateFormat.print(copy.getResolutionDate()));
	}
}
