/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.junit.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleExternalWorklogMapperTest {

	public static final DateTime THE_DATE = new DateTime(2012, 2, 2, 12, 00, 00, 00, DateTimeZone.forOffsetHours(5));
	private CsvDateParser csvDateParser = new CsvDateParser() {
			@Override
			public Date parseDate(String translatedValue) throws ParseException {
				return new SimpleDateFormat("yyyyMMddHHmmsszzz").parse(translatedValue);
			}
		};
	private ImportLogger logger;
	private SimpleExternalWorklogMapper mapper;
	private FullNameUserMapper userMapper;


	@Before
	public void setUp() throws Exception {
		logger = Mockito.mock(ImportLogger.class);
		mapper = new SimpleExternalWorklogMapper(csvDateParser);
		userMapper = new FullNameUserMapper(null);
	}

	@Test
	public void testParseWorklogWithDateAndUser() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("20120202120000+0500; Some User; 120",userMapper, logger);
		verifyWorklog(worklog, "someuser", 120, THE_DATE, SimpleExternalWorklogMapper.DEFAULT_COMMENT);
	}

	@Test
	public void testParseWorklogWithDateOnly() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("20120202120000+0500;;120", userMapper, logger);
		verifyWorklog(worklog, null, 120, THE_DATE, SimpleExternalWorklogMapper.DEFAULT_COMMENT);
	}

	@Test
	public void testParseWorklogWithUserOnly() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("Some User;120",userMapper, logger);
		verifyWorklog(worklog, "someuser", 120, null, SimpleExternalWorklogMapper.DEFAULT_COMMENT);
	}

	@Test
	public void testParseWorklogWithComment() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("Some Comment;20120202120000+0500;Some User;120", userMapper, logger);
		verifyWorklog(worklog, "someuser", 120, THE_DATE, "Some Comment");
	}

	@Test
	public void testParseWorklogWithCommentWithSemicolons() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog(" Some; Comment ;20120202120000+0500;Some User;120", userMapper, logger);
		verifyWorklog(worklog, "someuser", 120, THE_DATE, " Some; Comment ");
	}

	@Test
	public void testParseWorklogWithNoDate() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("Some Comment;;Some User;120", userMapper, logger);
		verifyWorklog(worklog, "someuser", 120, null, "Some Comment");

		final ExternalWorklog worklog1 = mapper.parseWorklog("Some; Comment; ;Some User;120", userMapper, logger);
		verifyWorklog(worklog1, "someuser", 120, null, "Some; Comment");
	}

	@Test
	public void testParseWorklogWithCommentOnly() throws Exception {
		final ExternalWorklog worklog = mapper.parseWorklog("Some\n Comment;;;120", userMapper, logger);
		verifyWorklog(worklog, null, 120, null, "Some\n Comment");

		final ExternalWorklog worklog1 = mapper.parseWorklog("Some; Comment; ; ;120", userMapper, logger);
		verifyWorklog(worklog1, null, 120, null, "Some; Comment");
	}

	private void verifyWorklog(ExternalWorklog worklog, String user, int timeSpent, DateTime startDate, String comment) {
		Mockito.verifyZeroInteractions(logger);
		Assert.assertNotNull("worklog", worklog);
		Assert.assertEquals("author", user, worklog.getAuthor());
		Assert.assertEquals("timeSpent", Long.valueOf(timeSpent), worklog.getTimeSpent());
		if (startDate != null) {
			final DateTime worklogStartDate = worklog.getStartDate();
			Assert.assertNotNull("startDate", worklogStartDate);
			Assert.assertEquals("startDate", startDate.toInstant(), worklogStartDate.toInstant());
		} else {
			Assert.assertNull("startDate", worklog.getStartDate());
		}
		Assert.assertEquals("comment", comment, worklog.getComment());
	}

}
