/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.mock.web.util;

import com.atlassian.jira.web.util.OutlookDate;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Simple mock for the outlook date.
 *
 * @since v3.13
 */

public class MockOutlookDate extends OutlookDate {
	private final DateFormat format;

	public MockOutlookDate(Locale locale) {
		super(locale, null, null, null);

		format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
	}

	public String format(Date date) {
		return format.format(date);
	}

	public void flushCache() {
		//do nothing.
	}

	public String formatDateTimePicker(final Date date) {
		return format.format(date);
	}

	@Override
	public String formatDMYHMS(final Date date) {
		return format(date);
	}
}
