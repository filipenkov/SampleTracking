/*
 * Copyright (C) 2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

public class DateTimeMatcher extends TypeSafeMatcher<DateTime> {

	private final Interval interval;
	private final DateTime targetDate;
	private final Duration tolerance;

	public DateTimeMatcher(DateTime targetDate) {
		this(targetDate, Duration.standardSeconds(1));
	}

	public DateTimeMatcher(DateTime targetDate, Duration tolerance) {
		this.targetDate = targetDate;
		this.tolerance = tolerance;
		interval = new Interval(tolerance.plus(1), targetDate.plusMillis(1)); // pad by 1 ms so now() contains now()
	}

	public static DateTimeMatcher ago(Period ago) {
		return new DateTimeMatcher(DateTime.now().minus(ago));
	}

	public static DateTimeMatcher ago(Period ago, int toleranceInSeconds) {
		return new DateTimeMatcher(DateTime.now().minus(ago), Duration.standardSeconds(toleranceInSeconds));
	}

	@Override
	public boolean matchesSafely(DateTime dateTime) {
		return interval.contains(dateTime);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(targetDate).appendText(" , with tolerance of ").appendValue(tolerance);
	}
}
