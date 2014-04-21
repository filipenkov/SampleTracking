package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

/**
 * Main issue interface. Historically, JIRA has just passed around {@link GenericValue}s describing issue records. Issue
 * is essentially a GenericValue wrapper, with setters, getters and a {@link #store} operation for persisting changes
 * through the underlying GenericValue.
 * <p/>
 * Amongst other means, Issue objects may be obtained with {@link IssueManager#getIssueObject(String)}, or converted
 * using {@link IssueFactory}.
 */
public interface Issue extends OfBizValueWrapper, IssueContext
{
    Long getId();

    /**
     * @deprecated please use getProjectObject (this still implicitly relies on the GV), but it is a start!
     */
    @Deprecated
    GenericValue getProject();

    /**
     * Gets the Project for this Issue.
     *
     * @return The Project for this Issue.
     */
    Project getProjectObject();

    /**
     * Gets the IssueType for this Issue.
     *
     * @return The IssueType for this Issue.
     *
     * @deprecated Please use {@link #getIssueTypeObject()}. Deprecated since v4.0
     */
    @Deprecated
    GenericValue getIssueType();

    /**
     * Gets the IssueType for this Issue.
     *
     * @return The IssueType for this Issue.
     */
    IssueType getIssueTypeObject();

    String getSummary();

    /**
     * Returns the Assignee User.
     *
     * If there is no assignee it returns null, else it is guaranteed to return a non-null User.
     * If the User is no longer available, it will create a dummy User object based on the username.
     *
     * @return the Assignee User.
     * @since 4.3
     */
    User getAssigneeUser();

    /**
     * Returns the Assignee User.
     * @return the Assignee User.
     * @deprecated use {@link #getAssigneeUser()} Since 4.3
     */
    com.opensymphony.user.User getAssignee();

    String getAssigneeId();

    /**
     * @return A collection of component GenericValues.
     */
    Collection<GenericValue> getComponents();

    /**
     * @since 4.2
     * @return collection of project components (as objects) that this issue is assigned to
     */
    Collection<ProjectComponent> getComponentObjects();

    /**
     * Returns the Reporter User.
     *
     * This will return a non-null User object even if the User has been deleted.
     *
     * @return the Reporter User.
     * @since 4.3
     */
    User getReporterUser();

    /**
     * Returns the Reporter User.
     * @return the Reporter User.
     * @deprecated use {@link #getReporterUser()} Since 4.3
     */
    com.opensymphony.user.User getReporter();

    String getReporterId();

    String getDescription();

    String getEnvironment();

    /**
     * @return a collection of 'affects' {@link com.atlassian.jira.project.version.Version} objects.
     */
    Collection<Version> getAffectedVersions();

    /**
     * @return a collection of fix-for {@link com.atlassian.jira.project.version.Version} objects.
     */
    Collection<Version> getFixVersions();

    Timestamp getDueDate();

    GenericValue getSecurityLevel();

    Long getSecurityLevelId();

    GenericValue getPriority();

    Priority getPriorityObject();

    GenericValue getResolution();

    Resolution getResolutionObject();

    String getKey();

    Long getVotes();

    Long getWatches();

    Timestamp getCreated();

    Timestamp getUpdated();

    /**
     * Returns the datetime that an issue was resolved on.  Will be null if it hasn't been resolved yet, or if an issue
     * has been returned to the 'unresolved' state.
     *
     * @return Timestamp of when an issue was resolved, or null
     */
    Timestamp getResolutionDate();

    Long getWorkflowId();

    /**
     * @param customField the CustomField
     * @return A custom field's value. Will be a List, User, Timestamp etc, depending on custom field type.
     */
    Object getCustomFieldValue(CustomField customField);

    /**
     * @deprecated Use {@link #getStatusObject} instead.
     */
    @Deprecated
    GenericValue getStatus();

    Status getStatusObject();

    /**
     * This is the "original estimate" of work to be performed on this issue, in milliseconds.
     *
     * @return the "original estimate" of work to be performed on this issue, in milliseconds.
     */
    Long getOriginalEstimate();

    /**
     * This is the "remaining estimate" of work left to be performed on this issue, in milliseconds.
     * <p/>
     * A better name would be getRemainingEstimate but for historical reasons it is called what it is called.
     *
     * @return the "remaining estimate" of work left to be performed on this issue, in milliseconds.
     */
    Long getEstimate();

    /**
     * This is the "total time spent" working on this issue, in milliseconds.
     *
     * @return the "total time spent" working on this issue, in milliseconds.
     */
    Long getTimeSpent();

    Object getExternalFieldValue(String fieldId);

    boolean isSubTask();

    Long getParentId();

    boolean isCreated();

    /**
     * If this issue is a subtask, return its parent.
     *
     * @return The parent Issue, or null if the issue is not a subtask.
     */
    Issue getParentObject();

    /**
     * @deprecated Use {@link #getParentObject()} instead.
     */
    @Deprecated
    GenericValue getParent();

    /**
     * @deprecated Use {@link #getSubTaskObjects()}
     */
    @Deprecated
    Collection<GenericValue> getSubTasks();

    /**
     * Gets all the issue's subtasks.
     *
     * @return A collection of {@link MutableIssue}s
     */
    Collection<Issue> getSubTaskObjects();

    boolean isEditable();

    IssueRenderContext getIssueRenderContext();

    /**
     * @return A collection of {@link com.atlassian.jira.issue.attachment.Attachment} objects
     */
    Collection<Attachment> getAttachments();

    /**
     * Returns a set of all the labels for this issue or an empty set if none exist yet.
     *
     * @return a set of all the labels for this issue or an empty set if none exist yet
     */
    Set<Label> getLabels();
}
