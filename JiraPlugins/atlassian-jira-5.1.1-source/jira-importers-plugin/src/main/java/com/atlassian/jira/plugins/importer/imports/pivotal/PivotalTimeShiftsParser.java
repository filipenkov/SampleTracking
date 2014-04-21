/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mindprod.csv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class PivotalTimeShiftsParser {
	private static final long UNITS_PER_HOUR = 3600;
	private final String csv;
	private final UserNameMapper userNameMapper;

	public PivotalTimeShiftsParser(String csv, UserNameMapper userNameMapper) {
		this.csv = csv;
		this.userNameMapper = userNameMapper;
	}

	public List<ExternalWorklog> parseWorklog() throws IOException {
		final CSVReader reader = new CSVReader(new StringReader(csv), ',', '"', "#", true, true, true, true, false);
		final String[] header = reader.getAllFieldsInLine();
		verifyHeader(header);
		final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
		final List<ExternalWorklog> result = Lists.newArrayList();
		for(;;) {
			final ExternalWorklog worklogItem = new ExternalWorklog();
			final String[] line;
			try {
				line = reader.getAllFieldsInLine();
			} catch (EOFException e) {
				break;
			}

			worklogItem.setStartDate(dateFormat.parseDateTime(line[0]).toDate());
			worklogItem.setAuthor(userNameMapper.getUsernameForLoginName(StringUtils.strip(line[2] + " " + line[1])));
			worklogItem.setTimeSpent((long) (UNITS_PER_HOUR * Double.parseDouble(line[5])));
			worklogItem.setComment(line[6]);

			result.add(worklogItem);
		}

		return result;
	}

	private void verifyHeader(String[] header) {
		Validate.isTrue(header.length >= 7, "Expecting CSV file with at least 7 columns"); // let's be somewhat tolerant to format change

		final Iterable<String> columns = Iterables.limit(Arrays.asList(header), 7);
		final List<String> expectedColumns = Arrays.asList("Date", "Last Name", "Given Name", "Person ID", "Project", "Hours", "Description");
		final boolean columnsAsExpected = Iterables.elementsEqual(columns, expectedColumns);
		Validate.isTrue(columnsAsExpected, "Expecting CSV with following columns: " + expectedColumns + ", got " + columns);
	}
}
