/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.external.beans;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.label.Label;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This function intentionally doesn't implement equals and hashCode. We have some caches in {@link com.atlassian.jira.plugins.importer.imports.csv.CsvDataBean}
 * that depend on ExternalIssue missing equals (issue fields are changed during the import in {@link com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter}
 * but the object stays the same).
 */
public class ExternalIssue {
	private static final Logger log = Logger.getLogger(ExternalIssue.class);

	private String externalId;
	private String summary;
	private String reporter;
	private String assignee;
	private String description;
	private String environment;
	private String issueType;
	private String status;
	private String priority;
	private String resolution;
	private DateTime created;
	private DateTime updated;
	private DateTime resolutionDate;
	private DateTime duedate;
	private Long votes;
	private Long originalEstimate;
	private Long timeSpent;
	private Long estimate;

	private final List<String> affectedVersions = Lists.newArrayList();
	private final List<String> fixedVersions = Lists.newArrayList();
	private final List<String> externalComponents = Lists.newArrayList();
	private final List<ExternalComment> externalComments = Lists.newArrayList();
	private final List<ExternalCustomFieldValue> externalCustomFieldValues = Lists.newArrayList();
	private final Set<Label> labels = Sets.newHashSet();
	private final List<ExternalWorklog> worklog = Lists.newArrayList();
	private final List<String> voters = Lists.newArrayList();
	private final List<String> watchers = Lists.newArrayList();
	private final List<ExternalIssue> subtasks = Lists.newArrayList();
	private final List<ExternalAttachment> attachments = Lists.newArrayList();

	public ExternalIssue() {

	}

	public ExternalIssue(ExternalIssue issue) {
		try {
			BeanUtils.copyProperties(this, issue);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		setAffectedVersions(issue.affectedVersions);
		setFixedVersions(issue.fixedVersions);
		setExternalComponents(issue.externalComponents);
		setExternalComments(issue.externalComments);
		setExternalCustomFieldValues(issue.externalCustomFieldValues);
		setLabels(issue.labels);
		setWorklog(issue.worklog);
		setVoters(issue.voters);
		setWatchers(issue.watchers);
		setAttachments(issue.attachments);
		setSubtasks(Collections2.transform(issue.subtasks, new Function<ExternalIssue, ExternalIssue>() {
			@Override
			public ExternalIssue apply(ExternalIssue input) {
				return new ExternalIssue(input);
			}
		}));
	}

	public void setWorklog(final List<ExternalWorklog> worklog) {
		this.worklog.clear();
		this.worklog.addAll(worklog);
	}

	public List<ExternalWorklog> getWorklog() {
		return ImmutableList.copyOf(this.worklog);
	}

	public void setLabels(final Set<Label> labels) {
		this.labels.clear();
		this.labels.addAll(labels);
	}

	public Set<Label> getLabels() {
		return ImmutableSet.copyOf(this.labels);
	}

	@Nullable
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(@Nullable final String externalId) {
		this.externalId = externalId;
	}

	public List<String> getAffectedVersions() {
		return ImmutableList.copyOf(affectedVersions);
	}

	public void setAffectedVersions(final List<String> affectedVersions) {
		this.affectedVersions.clear();
		this.affectedVersions.addAll(affectedVersions);
	}

	public List<String> getFixedVersions() {
		return ImmutableList.copyOf(fixedVersions);
	}

	public void setFixedVersions(final List<String> fixedVersions) {
		this.fixedVersions.clear();
		this.fixedVersions.addAll(fixedVersions);
	}

	public List<String> getExternalComponents() {
		return ImmutableList.copyOf(externalComponents);
	}

	public void setExternalComponents(final List<String> externalComponents) {
		this.externalComponents.clear();
		this.externalComponents.addAll(externalComponents);
	}

	public List<ExternalComment> getExternalComments() {
		return ImmutableList.copyOf(externalComments);
	}

	public void setExternalComments(final List<ExternalComment> externalComments) {
		this.externalComments.clear();
		this.externalComments.addAll(externalComments);
	}

	public void addExternalCustomFieldValue(ExternalCustomFieldValue externalCustomFieldValue) {
		externalCustomFieldValues.add(externalCustomFieldValue);
	}

	public List<ExternalCustomFieldValue> getExternalCustomFieldValues() {
		return ImmutableList.copyOf(externalCustomFieldValues);
	}

	public void setExternalCustomFieldValues(final List<ExternalCustomFieldValue> externalCustomFieldValues) {
		this.externalCustomFieldValues.clear();
		this.externalCustomFieldValues.addAll(externalCustomFieldValues);
	}

	@Nullable
	public String getSummary() {
		return summary;
	}

	public void setSummary(@Nullable final String summary) {
		this.summary = StringUtils.trimToNull(summary);
	}

	@Nullable
	public String getReporter() {
		return reporter;
	}

	public void setReporter(@Nullable final String reporter) {
		this.reporter = StringUtils.trimToNull(reporter);
	}

	@Nullable
	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(@Nullable final String assignee) {
		this.assignee = StringUtils.trimToNull(assignee);
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable final String description) {
		this.description = StringUtils.trimToNull(description);
	}

	@Nullable
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(@Nullable final String environment) {
		this.environment = StringUtils.trimToNull(environment);
	}

	@Nullable
	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(@Nullable final String issueType) {
		this.issueType = StringUtils.trimToNull(issueType);
	}

	@Nullable
	public String getStatus() {
		return status;
	}

	public void setStatus(@Nullable final String status) {
		this.status = StringUtils.trimToNull(status);
	}

	@Nullable
	public String getPriority() {
		return priority;
	}

	public void setPriority(@Nullable final String priority) {
		this.priority = StringUtils.trimToNull(priority);
	}

	@Nullable
	public String getResolution() {
		return resolution;
	}

	public void setResolution(@Nullable final String resolution) {
		this.resolution = StringUtils.trimToNull(resolution);
	}

	@Nullable
	public DateTime getCreated() {
		return created;
	}

	public void setCreated(@Nullable final Date created) {
		setCreated(created != null ? new DateTime(created) : null);
	}

	public void setCreated(@Nullable final DateTime created) {
		this.created = created;
	}

	@Nullable
	public DateTime getUpdated() {
		return updated;
	}

	public void setUpdated(@Nullable final Date updated) {
		setUpdated(updated != null ? new DateTime(updated) : null);
	}

	public void setUpdated(@Nullable final DateTime updated) {
		this.updated = updated != null ? new DateTime(updated) : null;
	}

	@Nullable
	public DateTime getResolutionDate() {
		return resolutionDate;
	}

	public void setResolutionDate(@Nullable final Date resolutionDate) {
		setResolutionDate(resolutionDate != null ? new DateTime(resolutionDate) : null);
	}

	public void setResolutionDate(@Nullable final DateTime resolutionDate) {
		this.resolutionDate = resolutionDate;
	}

	@Nullable
	public DateTime getDuedate() {
		return duedate;
	}

	public void setDuedate(@Nullable final Date duedate) {
		setDuedate(duedate != null ? new DateTime(duedate) : null);
	}

	public void setDuedate(@Nullable final DateTime duedate) {
		this.duedate = duedate;
	}

	@Nullable
	public Long getVotes() {
		return votes;
	}

	public void setVotes(@Nullable final Long votes) {
		this.votes = votes;
	}

	@Nullable
	public Long getOriginalEstimate() {
		return originalEstimate;
	}

	public void setOriginalEstimate(@Nullable final Long originalEstimate) {
		this.originalEstimate = originalEstimate;
	}

	@Nullable
	public Long getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(@Nullable final Long timeSpent) {
		this.timeSpent = timeSpent;
	}

	@Nullable
	public Long getEstimate() {
		return estimate;
	}

	public void setEstimate(@Nullable final Long estimate) {
		this.estimate = estimate;
	}

	public void setField(String fieldName, @Nullable final Object value) {
		try {
			fieldName = processFieldName(fieldName);

			BeanUtils.setProperty(this, fieldName, value);
		} catch (final Exception e) {
			log.warn("Unable to set field using reflection for :" + fieldName + ":" + value);
		}
	}

	@Nullable
	public String getField(String fieldName) {
		try {
			fieldName = processFieldName(fieldName);
			return BeanUtils.getProperty(this, fieldName);
		}
		catch (final Exception e) {
			log.warn("Unable to get field using reflection for :" + fieldName);
			return null;
		}
	}

	private String processFieldName(String fieldName) {
		if (ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(fieldName)) {
			fieldName = "issueType";
		}
		return fieldName;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("summary", summary).add("issueType", issueType).toString();
	}

	public void setVoters(List<String> strings) {
		this.voters.clear();
		this.voters.addAll(strings);
	}

	public List<String> getVoters() {
		return ImmutableList.copyOf(voters);
	}

	public void setWatchers(List<String> strings) {
		this.watchers.clear();
		this.watchers.addAll(strings);
	}

	public List<String> getWatchers() {
		return ImmutableList.copyOf(watchers);
	}

	public List<ExternalIssue> getSubtasks() {
		return ImmutableList.copyOf(subtasks);
	}

	public void setSubtasks(Collection<ExternalIssue> subtasks) {
		this.subtasks.clear();
		this.subtasks.addAll(subtasks);
	}

	public List<ExternalAttachment> getAttachments() {
		return ImmutableList.copyOf(attachments);
	}

	public void setAttachments(List<ExternalAttachment> attachments) {
		this.attachments.clear();
		this.attachments.addAll(attachments);
	}
}
