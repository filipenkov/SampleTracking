/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static com.atlassian.jira.issue.IssueFieldConstants.*;

public class DefaultExternalIssueMapper implements ExternalIssueMapper {

	public static final String SUBTASK_PARENT_ID = "subtask-parent-id";
	public static final String ISSUE_ID = "issue-id";

	private TimeEstimateConverter timeEstimateConverter;
	private final CsvDateParser dateParser;

	public DefaultExternalIssueMapper(TimeEstimateConverter timeEstimateConverter, CsvDateParser dateParser) {
		this.timeEstimateConverter = timeEstimateConverter;
		this.dateParser = dateParser;
	}

	public static final String CLEAR_VALUE_MARKER = "<<!clear!>>";
	public static final Date DATE_CLEAR_VALUE_MARKER = new Date(Long.MAX_VALUE);
	
	private Iterable<String> optionalOp(Multimap<String, String> bean, String fieldName) {
		final Collection<String> values = bean.get(fieldName);
		if (!values.isEmpty()) {
			if (values.contains(CLEAR_VALUE_MARKER)) {
				return Collections.singleton(CLEAR_VALUE_MARKER);
			}
			return Iterables.limit(values, 1);
		}
		return Collections.emptyList();
	}

	private Iterable<Date> optionalDate(Multimap<String, String> bean, String fieldName, ImportLogger log) {
		final Collection<String> values = bean.get(fieldName);
		if (!values.isEmpty()) {
			final String dateStr = values.iterator().next();
			try {
				return Collections.singleton(dateParser.parseDate(dateStr));
			} catch (ParseException e) {
				log.warn("Unable to parse created date: %s", dateStr);
			}
		}
		return Collections.emptyList();
	}

	private Iterable<Date> optionalClearableDate(Multimap<String, String> bean, String fieldName, ImportLogger log) {
		final Collection<String> values = bean.get(fieldName);
		if (!values.isEmpty()) {
			final String dateStr = values.iterator().next();
			try {
				if (CLEAR_VALUE_MARKER.equals(dateStr)) {
					return Collections.singleton(DATE_CLEAR_VALUE_MARKER);
				}
				return Collections.singleton(dateParser.parseDate(dateStr));
			} catch (ParseException e) {
				log.warn("Unable to parse created dateStr: %s", dateStr);
			}
		}
		return Collections.emptyList();
	}


	private <T> Iterable<T> getFirstElementIfPresent(Iterable<T> iterable) {
		return Iterables.limit(iterable, 1);
	}


	@Override
	public ExternalIssue buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		final ExternalIssue issue = new ExternalIssue();

		if (!bean.get(ISSUE_ID).isEmpty()) {
			issue.setExternalId(Iterables.getFirst(bean.get(ISSUE_ID), null));
		}

		for (final String val : optionalOp(bean, REPORTER)) {
			issue.setReporter(val);
		}
		for (final String val : optionalOp(bean, ASSIGNEE)) {
			issue.setAssignee(val);
		}
		for (final String val : optionalOp(bean, ISSUE_TYPE)) {
			issue.setIssueType(val);
		}
		for (final String val : optionalOp(bean, PRIORITY)) {
			issue.setPriority(val);
		}
		for (final String val : optionalOp(bean, SUMMARY)) {
			issue.setSummary(val);
		}

		for (final String val: getFirstElementIfPresent(bean.get(ISSUE_KEY))) {
			issue.setKey(val);
		}

		for (final String val : optionalOp(bean, DESCRIPTION)) {
			issue.setDescription(val);
		}

		for (final String val : optionalOp(bean, ENVIRONMENT)) {
			issue.setEnvironment(val);
		}

		for (final String val : optionalOp(bean, RESOLUTION)) {
			issue.setResolution(val);
		}

		for (final String val: getFirstElementIfPresent(bean.get(STATUS))) {
			issue.setStatus(val);
		}

		// Deal with dates
		for (final Date val : optionalDate(bean, CREATED, log)) {
			issue.setCreated(val);
		}

		for (final Date val : optionalDate(bean, UPDATED, log)) {
			issue.setUpdated(val);
		}

		for (final Date val : optionalClearableDate(bean, RESOLUTION_DATE, log)) {
			issue.setResolutionDate(val);
		}

		for (final Date val : optionalClearableDate(bean, DUE_DATE, log)) {
			issue.setDuedate(val);
		}

		if (!bean.get("timeoriginalestimate").isEmpty()) {
			try {
				final String originalEstimateParam = bean.get("timeoriginalestimate").iterator().next();
				final Long originalEstimate = timeEstimateConverter.convertEstimate(originalEstimateParam);

//				log.log("Setting original estimate: value=%d", originalEstimate);
				issue.setOriginalEstimate(originalEstimate);

				// Set estimate with original estimate by default (null, not set already).
				// This will be overriden in the next block if an estimate is set as well.
				if (issue.getEstimate() == null) {
					issue.setEstimate(originalEstimate);
				}
			}
			catch (Exception e) {
				log.warn("Unable to parse original estimate: %s", bean.get("timeoriginalestimate"));
			}
		}

		if (!bean.get("timeestimate").isEmpty()) {
			try {
				String estimateParam = (String) ((Collection) bean.get("timeestimate")).iterator().next();
				Long estimate = timeEstimateConverter.convertEstimate(estimateParam);

//				log.log("Setting estimate: value=%d", estimate);
				issue.setEstimate(estimate);
			}
			catch (Exception e) {
				log.warn("Unable to parse estimate: %s", bean.get("timeestimate"));
			}
		}

		if (!bean.get("timespent").isEmpty()) {
			try {
				String timeSpentParam = (String) ((Collection) bean.get("timespent")).iterator().next();
				Long timeSpent = timeEstimateConverter.convertEstimate(timeSpentParam);

//				log.log("Setting time spent: value=%d", timeSpent);
				issue.setTimeSpent(timeSpent);
			}
			catch (Exception e) {
				log.warn("Unable to parse time spent: %s", bean.get("timespent"));
			}
		}

		for (final String val : optionalOp(bean, VOTES)) {
			try {
				issue.setVotes(CLEAR_VALUE_MARKER.equals(val) ? 0 : Long.valueOf(val));
			} catch (NumberFormatException e) {
				log.warn("Unable to parse votes: %s", bean.get("votes"));
			}
		}

		if (!bean.get("comments").isEmpty()) {
			issue.setComments(new ArrayList(bean.get("comments")));
		}

		return issue;
	}

	public void setTimeTrackingConverter(TimeEstimateConverter timeEstimateConverter) {
		this.timeEstimateConverter = timeEstimateConverter;
	}
}
