/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import au.com.bytecode.opencsv.CSVReader;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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

	public PivotalTimeShiftsParser(String csv) {
		this.csv = csv;
	}

	public List<ExternalWorklog> parseWorklog() throws IOException {
		final CSVReader reader = new CSVReader(new StringReader(csv), ',', '"');
		final String[] header = StringUtils.stripAll(reader.readNext());
		verifyHeader(header);
		final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
		final List<ExternalWorklog> result = Lists.newArrayList();
		for(;;) {
			final ExternalWorklog worklogItem = new ExternalWorklog();
			final String[] line;
			try {
				line = StringUtils.stripAll(reader.readNext());
				if (line == null) {
					break;
				}
			} catch (EOFException e) {
				break;
			}

			worklogItem.setStartDate(dateFormat.parseDateTime(line[0]).toDate());
			worklogItem.setAuthor(StringUtils.strip(line[2] + " " + line[1]));
			worklogItem.setTimeSpent((long) (UNITS_PER_HOUR * Double.parseDouble(line[4])));
			worklogItem.setComment(line[5]);

			result.add(worklogItem);
		}

		return result;
	}

	private void verifyHeader(String[] header) {
		Validate.isTrue(header.length >= 6, "Expecting CSV file with at least 6 columns"); // let's be somewhat tolerant to format change

		final Iterable<String> columns = Iterables.limit(Arrays.asList(header), 6);
		final List<String> expectedColumns = Arrays.asList("Date", "Last Name", "Given Name", "Project", "Hours", "Description");
		final boolean columnsAsExpected = Iterables.elementsEqual(columns, expectedColumns);
		Validate.isTrue(columnsAsExpected, "Expecting CSV with following columns: " + expectedColumns);
	}
}
