package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.version.Version;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Facade for an issue's {@link GenericValue}. After calling any 'setter' method, you will need to call {@link #store}
 * to persist the change to the database. Note that this is a 'shallow' store - only fields directly part of the issue
 * (in the database schema) are persisted.
 *
 * @see IssueManager
 */
public interface MutableIssue extends Issue
{

    void setProject(GenericValue project);

    /**
     * Sets the project by id. This has the same outcome as calling {@link #setProject(org.ofbiz.core.entity.GenericValue)}
     *
     * @param projectId The id of the project
     *
     * @throws IllegalArgumentException If no project exists for the given projectId.
     */
    void setProjectId(Long projectId) throws IllegalArgumentException;

    void setIssueType(GenericValue issueType);

    void setIssueTypeId(String issueTypeId);

    void setSummary(String summary);

    void setAssignee(User assignee);

    /**
     * @deprecated Call {@link #setAssignee(com.atlassian.crowd.embedded.api.User)}. Since v4.3
     * @param assignee
     */
    void setAssignee(com.opensymphony.user.User assignee);

    void setComponents(Collection<GenericValue> components);

    void setAssigneeId(String assigneeId);

    void setReporter(User reporter);

    /**
     * @deprecated Call {@link #setReporter(com.atlassian.crowd.embedded.api.User)}. Since v4.3
     * @param reporter
     */
    void setReporter(com.opensymphony.user.User reporter);

    /**
     * Sets the reporter in this issue.
     *
     * @param reporterId username of the desired reporter.
     *
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if the user with the given username does not exist.
     * @see #setReporter(User)
     */
    void setReporterId(String reporterId);

    void setDescription(String description);

    void setEnvironment(String environment);

    /**
     * @param affectedVersions A collection of 'affects' {@link com.atlassian.jira.project.version.Version} objects.
     */
    void setAffectedVersions(Collection<Version> affectedVersions);

    /**
     * @param fixVersions A collection of fix-for {@link com.atlassian.jira.project.version.Version} objects.
     */
    void setFixVersions(Collection<Version> fixVersions);

    void setDueDate(Timestamp dueDate);

    void setSecurityLevelId(Long securityLevelId);

    void setSecurityLevel(GenericValue securityLevel);

    void setPriority(GenericValue priority);

    void setPriorityId(String priorityId);

    void setResolution(GenericValue resolution);

    void setKey(String key);

    void setVotes(Long votes);

    void setWatches(Long votes);

    void setCreated(Timestamp created);

    void setUpdated(Timestamp updated);

    void setResolutionDate(Timestamp resolutionDate);

    void setWorkflowId(Long workflowId);

    /**
     * Sets a custom field value on this Issue Object, but does <em>not write it to the database</em>. This is highly
     * misleading. <br> To actually set a custom field value, use {@link CustomField#updateIssue(com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem,
     * MutableIssue, java.util.Map)}
     *
     * @param customField the CustomField
     * @param value       the value.
     */
    void setCustomFieldValue(CustomField customField, Object value);

    void setStatus(GenericValue status);

    /**
     * Set issue's status by status id ("1", "2" etc).
     *
     * @param statusId the new StatusId.
     */
    void setStatusId(String statusId);

    /**
     * Reset the internal list of modified fields.
     *
     * @see #getModifiedFields()
     */
    void resetModifiedFields();

    void setOriginalEstimate(Long estimate);

    void setTimeSpent(Long timespent);

    void setEstimate(Long estimate);


    /**
     * This can be used bya  field to "place" a custom object into the MutableIssue so that it can retrived it by key at
     * a later point via the {@link #getModifiedFields()} and {@link #getExternalFieldValue(String)}.
     * <p/>
     * The passed in newValue  will be wrapped in the {@link ModifiedValue#getNewValue()} The {@link
     * ModifiedValue#getOldValue()} will be null.
     *
     * @param fieldId  the field id to use as a key
     * @param newValue the new value to place in a {@link ModifiedValue}
     */
    void setExternalFieldValue(String fieldId, Object newValue);

    /**
     * This can be used bya  field to "place" a custom object into the MutableIssue so that it can retrived it by key at
     * a later point via the {@link #getModifiedFields()} and {@link #getExternalFieldValue(String)}.
     * <p/>
     * The passed in newValue will be wrapped in the {@link ModifiedValue#getNewValue()} and the oldValue will be placed
     * in {@link ModifiedValue#getOldValue()}
     *
     * @param fieldId  the field id to use as a key
     * @param oldValue the old value to place in a {@link ModifiedValue}
     * @param newValue the new value to place in a {@link ModifiedValue}
     */
    void setExternalFieldValue(String fieldId, Object oldValue, Object newValue);

    /**
     * Sets the ParentId of this Issue.
     *
     * @param parentId The new parentId.
     *
     * @see #setParentObject(Issue)
     */
    void setParentId(Long parentId);

    /**
     * Sets the parent Issue Object for this Issue (subtask). <p> Normally a subtask just has the ID of the parent set
     * using <code>setParentId()</code>. In this case, the getParentObject() method will look up the parent object from
     * the DB (or cache). However, when you are editing a parent and its subtask within a transaction (eg a Bulk Move),
     * it is useful to be able to link the subtask to the <em>pending</em> parent object in order that it can see the
     * <em>new</em> values for the parent. </p>
     *
     * @param parentIssue the required parent Issue for this subtask.
     *
     * @see #setParentId(Long)
     * @see #getParentObject()
     */
    void setParentObject(Issue parentIssue);

    void setResolutionId(String resolutionId);

    /**
     * Set the labels for this issue.
     *
     * @param labels the labels for this issue
     */
    void setLabels(Set<Label> labels);
    
    /**
     * Retrieve a map of issue fields whose values have been set (since object creation or last {@link
     * #resetModifiedFields()} call.
     *
     * @return A Map of key -> ModifiedValue pairs, where keys are defined in {@link IssueFieldConstants} and the value
     *         objects in the ModifiedValue are field-specific.
     */
    Map<String, ModifiedValue> getModifiedFields();
}
