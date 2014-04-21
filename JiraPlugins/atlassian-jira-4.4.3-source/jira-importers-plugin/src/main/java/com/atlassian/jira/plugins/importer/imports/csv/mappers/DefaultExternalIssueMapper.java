/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultExternalIssueMapper implements ExternalIssueMapper {

	public static final String SUBTASK_PARENT_ID = "subtask-parent-id";
	public static final String ISSUE_ID = "issue-id";

	private TimeEstimateConverter timeEstimateConverter;
	private final CsvConfigBean configBean;

	public DefaultExternalIssueMapper(TimeEstimateConverter timeEstimateConverter, CsvConfigBean configBean) {
		this.timeEstimateConverter = timeEstimateConverter;
		this.configBean = configBean;
	}

	@Override
	public ExternalIssue buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		final ExternalIssue issue = new ExternalIssue();
		if (!bean.get(ISSUE_ID).isEmpty()) {
			issue.setExternalId(Iterables.getFirst(bean.get(ISSUE_ID), null));
		}
		if (!bean.get("reporter").isEmpty())
			issue.setReporter((String) ((Collection) bean.get("reporter")).iterator().next());
		if (!bean.get("assignee").isEmpty())
			issue.setAssignee((String) ((Collection) bean.get("assignee")).iterator().next());
		if (!bean.get(IssueFieldConstants.ISSUE_TYPE).isEmpty()) {
			issue.setIssueType((String) ((Collection) bean.get(IssueFieldConstants.ISSUE_TYPE)).iterator().next());
		}
		if (!bean.get(IssueFieldConstants.SUMMARY).isEmpty())
			issue.setSummary((String) ((Collection) bean.get(IssueFieldConstants.SUMMARY)).iterator().next());
		if (!bean.get("description").isEmpty())
			issue.setDescription((String) ((Collection) bean.get("description")).iterator().next());
		if (!bean.get("environment").isEmpty())
			issue.setEnvironment((String) ((Collection) bean.get("environment")).iterator().next());
		if (!bean.get("priority").isEmpty())
			issue.setPriority((String) ((Collection) bean.get("priority")).iterator().next());
		if (!bean.get("resolution").isEmpty())
			issue.setResolution((String) ((Collection) bean.get("resolution")).iterator().next());
		if (!bean.get("status").isEmpty()) issue.setStatus((String) ((Collection) bean.get("status")).iterator().next());

		// Deal with dates
		if (!bean.get("created").isEmpty()) {
			try {
				String createdDate = (String) ((Collection) bean.get("created")).iterator().next();
				issue.setCreated(configBean.parseDate(createdDate));
			}
			catch (ParseException e) {
				log.warn("Unable to parse created date: " + bean.get("created"));
			}
		}

		if (!bean.get("updated").isEmpty()) {
			try {
				String updatedDate = (String) ((Collection) bean.get("updated")).iterator().next();
				issue.setUpdated(configBean.parseDate(updatedDate));
			}
			catch (ParseException e) {
				log.warn("Unable to parse updated date: " + bean.get("updated"));
			}
		}

		if (!bean.get("resolutiondate").isEmpty()) {
			try {
				String resolvedDate = (String) ((Collection) bean.get("resolutiondate")).iterator().next();
				issue.setResolutionDate(configBean.parseDate(resolvedDate));
			}
			catch (ParseException e) {
				log.warn("Unable to parse resolution date: " + bean.get("resolutiondate"));
			}
		}

		if (!bean.get("duedate").isEmpty()) {
			try {
				String dueDate = (String) ((Collection) bean.get("duedate")).iterator().next();
				issue.setDuedate(configBean.parseDate(dueDate));
			}
			catch (ParseException e) {
				log.warn("Unable to parse due date: " + bean.get("duedate"));
			}
		}

		if (!bean.get("timeoriginalestimate").isEmpty()) {
			try {
				String originalEstimateParam = (String) ((Collection) bean.get("timeoriginalestimate")).iterator()
						.next();
				Long originalEstimate = timeEstimateConverter.convertEstimate(originalEstimateParam);

				log.log("Setting original estimate: value=" + originalEstimate);
				issue.setOriginalEstimate(originalEstimate);

				// Set estimate with original estimate by default (null, not set already).
				// This will be overriden in the next block if an estimate is set as well.
				if (issue.getEstimate() == null) {
					issue.setEstimate(originalEstimate);
				}
			}
			catch (Exception e) {
				log.warn("Unable to parse original estimate: " + bean.get("timeoriginalestimate"));
			}
		}

		if (!bean.get("timeestimate").isEmpty()) {
			try {
				String estimateParam = (String) ((Collection) bean.get("timeestimate")).iterator().next();
				Long estimate = timeEstimateConverter.convertEstimate(estimateParam);

				log.log("Setting estimate: value=" + estimate);
				issue.setEstimate(estimate);
			}
			catch (Exception e) {
				log.warn("Unable to parse estimate: " + bean.get("timeestimate"));
			}
		}

		if (!bean.get("timespent").isEmpty()) {
			try {
				String timeSpentParam = (String) ((Collection) bean.get("timespent")).iterator().next();
				Long timeSpent = timeEstimateConverter.convertEstimate(timeSpentParam);

				log.log("Setting time spent: value=" + timeSpent);
				issue.setTimeSpent(timeSpent);
			}
			catch (Exception e) {
				log.warn("Unable to parse time spent: " + bean.get("timespent"));
			}
		}

		if (!bean.get(IssueFieldConstants.VOTES).isEmpty()) {
			try {
				issue.setVotes(new Long((String) ((Collection) bean.get(IssueFieldConstants.VOTES)).iterator().next()));
			}
			catch (NumberFormatException e) {
				log.warn("Unable to parse votes: " + bean.get("votes"));
			}
		}

		if (!bean.get("comments").isEmpty()) {
			issue.setExternalComments(new ArrayList(bean.get("comments")));
		}

		return issue;
	}

	public void setTimeTrackingConverter(TimeEstimateConverter timeEstimateConverter) {
		this.timeEstimateConverter = timeEstimateConverter;
	}
}
