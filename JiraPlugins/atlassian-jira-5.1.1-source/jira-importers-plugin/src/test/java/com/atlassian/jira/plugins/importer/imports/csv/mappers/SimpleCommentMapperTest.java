/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;


import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import junitx.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SimpleCommentMapperTest {

	private CsvDateParser csvDateParser = new CsvDateParser() {
		@Override
		public Date parseDate(String translatedValue) throws ParseException {
			return new SimpleDateFormat("yyyyMMddHHmmsszzz").parse(translatedValue);
		}
	};

	@Test
	public void testParsingComments() throws Exception {
		SimpleCommentMapper mapper = new SimpleCommentMapper(csvDateParser);
		ListMultimap<String, String> multiMap = ArrayListMultimap.create();
		multiMap.putAll(IssueFieldConstants.COMMENT, ImmutableList.of(
				"20120202120000+0500; Some User; another comment"));

		final List<ExternalComment> externalComments = mapper.buildFromMultiMap(multiMap, new FullNameUserMapper(null), Mockito.mock(ImportLogger.class));
		Assert.assertEquals(1, externalComments.size());
		final ExternalComment comment1 = externalComments.get(0);
		Assert.assertEquals(new DateTime(2012, 2, 2,  12, 00, 00, 00, DateTimeZone.forOffsetHours(5)).toInstant(), comment1.getCreated().toInstant());
		Assert.assertEquals("someuser", comment1.getAuthor());
		Assert.assertEquals("another comment", comment1.getBody());
	}
}
