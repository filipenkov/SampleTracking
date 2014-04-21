/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.external;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A special hack to ignore JIRA core (or actually OS Workflow via IssueCreateFunction) calling setKey()
 * with auto-generated issue key per project.
 * It delegates everything to MutableIssue
 */
class FixedKeyMutableIssue implements MutableIssue {
	private final MutableIssue issue;

	public FixedKeyMutableIssue(MutableIssue issue, String key) {
		this.issue = issue;
		issue.setKey(key);
	}

	@Override
	public void setProject(GenericValue project) {
		issue.setProject(project);
	}

	@Override
	public void setProjectObject(Project project) {
		issue.setProjectObject(project);
	}

	@Override
	public void setProjectId(Long projectId) throws IllegalArgumentException {
		issue.setProjectId(projectId);
	}

	@Override
	public void setIssueType(GenericValue issueType) {
		issue.setIssueType(issueType);
	}

	@Override
	public void setIssueTypeObject(IssueType issueType) {
		issue.setIssueTypeObject(issueType);
	}

	@Override
	public void setIssueTypeId(String issueTypeId) {
		issue.setIssueTypeId(issueTypeId);
	}

	@Override
	public void setSummary(String summary) {
		issue.setSummary(summary);
	}

	@Override
	public void setAssignee(User assignee) {
		issue.setAssignee(assignee);
	}

	@Override
	public void setComponents(Collection<GenericValue> components) {
		issue.setComponents(components);
	}

	@Override
	public void setComponentObjects(Collection<ProjectComponent> components) {
		issue.setComponentObjects(components);
	}

	@Override
	public void setAssigneeId(String assigneeId) {
		issue.setAssigneeId(assigneeId);
	}

	@Override
	public void setReporter(User reporter) {
		issue.setReporter(reporter);
	}

	@Override
	public void setReporterId(String reporterId) {
		issue.setReporterId(reporterId);
	}

	@Override
	public void setDescription(String description) {
		issue.setDescription(description);
	}

	@Override
	public void setEnvironment(String environment) {
		issue.setEnvironment(environment);
	}

	@Override
	public void setAffectedVersions(Collection<Version> affectedVersions) {
		issue.setAffectedVersions(affectedVersions);
	}

	@Override
	public void setFixVersions(Collection<Version> fixVersions) {
		issue.setFixVersions(fixVersions);
	}

	@Override
	public void setDueDate(Timestamp dueDate) {
		issue.setDueDate(dueDate);
	}

	@Override
	public void setSecurityLevelId(Long securityLevelId) {
		issue.setSecurityLevelId(securityLevelId);
	}

	@Override
	public void setSecurityLevel(GenericValue securityLevel) {
		issue.setSecurityLevel(securityLevel);
	}

	@Override
	public void setPriority(@Nullable GenericValue priority) {
		issue.setPriority(priority);
	}

	@Override
	public void setPriorityObject(@Nullable Priority priority) {
		issue.setPriorityObject(priority);
	}

	@Override
	public void setPriorityId(String priorityId) {
		issue.setPriorityId(priorityId);
	}

	@Override
	public void setResolution(GenericValue resolution) {
		issue.setResolution(resolution);
	}

	@Override
	public void setResolutionObject(Resolution resolution) {
		issue.setResolutionObject(resolution);
	}

	@Override
	public void setKey(String key) {
		if (key == null) {
			issue.setKey(key);
		}
	}

	@Override
	public void setVotes(Long votes) {
		issue.setVotes(votes);
	}

	@Override
	public void setWatches(Long votes) {
		issue.setWatches(votes);
	}

	@Override
	public void setCreated(Timestamp created) {
		issue.setCreated(created);
	}

	@Override
	public void setUpdated(Timestamp updated) {
		issue.setUpdated(updated);
	}

	@Override
	public void setResolutionDate(Timestamp resolutionDate) {
		issue.setResolutionDate(resolutionDate);
	}

	@Override
	public void setWorkflowId(Long workflowId) {
		issue.setWorkflowId(workflowId);
	}

	@Override
	public void setCustomFieldValue(CustomField customField, Object value) {
		issue.setCustomFieldValue(customField, value);
	}

	@Override
	public void setStatus(GenericValue status) {
		issue.setStatus(status);
	}

	@Override
	public void setStatusObject(Status status) {
		issue.setStatusObject(status);
	}

	@Override
	public void setStatusId(String statusId) {
		issue.setStatusId(statusId);
	}

	@Override
	public void resetModifiedFields() {
		issue.resetModifiedFields();
	}

	@Override
	public void setOriginalEstimate(Long estimate) {
		issue.setOriginalEstimate(estimate);
	}

	@Override
	public void setTimeSpent(Long timespent) {
		issue.setTimeSpent(timespent);
	}

	@Override
	public void setEstimate(Long estimate) {
		issue.setEstimate(estimate);
	}

	@Override
	public void setExternalFieldValue(String fieldId, Object newValue) {
		issue.setExternalFieldValue(fieldId, newValue);
	}

	@Override
	public void setExternalFieldValue(String fieldId, Object oldValue, Object newValue) {
		issue.setExternalFieldValue(fieldId, oldValue, newValue);
	}

	@Override
	public void setParentId(Long parentId) {
		issue.setParentId(parentId);
	}

	@Override
	public void setParentObject(Issue parentIssue) {
		issue.setParentObject(parentIssue);
	}

	@Override
	public void setResolutionId(String resolutionId) {
		issue.setResolutionId(resolutionId);
	}

	@Override
	public void setLabels(Set<Label> labels) {
		issue.setLabels(labels);
	}

	@Override
	public Map<String, ModifiedValue> getModifiedFields() {
		return issue.getModifiedFields();
	}

	@Override
	public Long getId() {
		return issue.getId();
	}

	@Override
	@Deprecated
	public GenericValue getProject() {
		return issue.getProject();
	}

	@Override
	public Project getProjectObject() {
		return issue.getProjectObject();
	}

	@Override
	@Deprecated
	public GenericValue getIssueType() {
		return issue.getIssueType();
	}

	@Override
	public IssueType getIssueTypeObject() {
		return issue.getIssueTypeObject();
	}

	@Override
	public String getSummary() {
		return issue.getSummary();
	}

	@Override
	public User getAssigneeUser() {
		return issue.getAssigneeUser();
	}

	@Override
	public User getAssignee() {
		return issue.getAssignee();
	}

	@Override
	public String getAssigneeId() {
		return issue.getAssigneeId();
	}

	@Override
	public Collection<GenericValue> getComponents() {
		return issue.getComponents();
	}

	@Override
	public Collection<ProjectComponent> getComponentObjects() {
		return issue.getComponentObjects();
	}

	@Override
	public User getReporterUser() {
		return issue.getReporterUser();
	}

	@Override
	public User getReporter() {
		return issue.getReporter();
	}

	@Override
	public String getReporterId() {
		return issue.getReporterId();
	}

	@Override
	public String getDescription() {
		return issue.getDescription();
	}

	@Override
	public String getEnvironment() {
		return issue.getEnvironment();
	}

	@Override
	public Collection<Version> getAffectedVersions() {
		return issue.getAffectedVersions();
	}

	@Override
	public Collection<Version> getFixVersions() {
		return issue.getFixVersions();
	}

	@Override
	public Timestamp getDueDate() {
		return issue.getDueDate();
	}

	@Override
	public GenericValue getSecurityLevel() {
		return issue.getSecurityLevel();
	}

	@Override
	public Long getSecurityLevelId() {
		return issue.getSecurityLevelId();
	}

	@Override
	@Nullable
	public GenericValue getPriority() {
		return issue.getPriority();
	}

	@Override
	@Nullable
	public Priority getPriorityObject() {
		return issue.getPriorityObject();
	}

	@Override
	public String getResolutionId() {
		return issue.getResolutionId();
	}

	@Override
	public GenericValue getResolution() {
		return issue.getResolution();
	}

	@Override
	public Resolution getResolutionObject() {
		return issue.getResolutionObject();
	}

	@Override
	public String getKey() {
		return issue.getKey();
	}

	@Override
	public Long getVotes() {
		return issue.getVotes();
	}

	@Override
	public Long getWatches() {
		return issue.getWatches();
	}

	@Override
	public Timestamp getCreated() {
		return issue.getCreated();
	}

	@Override
	public Timestamp getUpdated() {
		return issue.getUpdated();
	}

	@Override
	public Timestamp getResolutionDate() {
		return issue.getResolutionDate();
	}

	@Override
	public Long getWorkflowId() {
		return issue.getWorkflowId();
	}

	@Override
	public Object getCustomFieldValue(CustomField customField) {
		return issue.getCustomFieldValue(customField);
	}

	@Override
	@Deprecated
	public GenericValue getStatus() {
		return issue.getStatus();
	}

	@Override
	public Status getStatusObject() {
		return issue.getStatusObject();
	}

	@Override
	public Long getOriginalEstimate() {
		return issue.getOriginalEstimate();
	}

	@Override
	public Long getEstimate() {
		return issue.getEstimate();
	}

	@Override
	public Long getTimeSpent() {
		return issue.getTimeSpent();
	}

	@Override
	public Object getExternalFieldValue(String fieldId) {
		return issue.getExternalFieldValue(fieldId);
	}

	@Override
	public boolean isSubTask() {
		return issue.isSubTask();
	}

	@Override
	public Long getParentId() {
		return issue.getParentId();
	}

	@Override
	public boolean isCreated() {
		return issue.isCreated();
	}

	@Override
	public Issue getParentObject() {
		return issue.getParentObject();
	}

	@Override
	@Deprecated
	public GenericValue getParent() {
		return issue.getParent();
	}

	@Override
	@Deprecated
	public Collection<GenericValue> getSubTasks() {
		return issue.getSubTasks();
	}

	@Override
	public Collection<Issue> getSubTaskObjects() {
		return issue.getSubTaskObjects();
	}

	@Override
	public boolean isEditable() {
		return issue.isEditable();
	}

	@Override
	public IssueRenderContext getIssueRenderContext() {
		return issue.getIssueRenderContext();
	}

	@Override
	public Collection<Attachment> getAttachments() {
		return issue.getAttachments();
	}

	@Override
	public Set<Label> getLabels() {
		return issue.getLabels();
	}

	@Override
	public String getString(String name) {
		return issue.getString(name);
	}

	@Override
	public Timestamp getTimestamp(String name) {
		return issue.getTimestamp(name);
	}

	@Override
	public Long getLong(String name) {
		return issue.getLong(name);
	}

	@Override
	public GenericValue getGenericValue() {
		return issue.getGenericValue();
	}

	@Override
	public void store() {
		issue.store();
	}
}
