/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import javax.mail.Message;

@PublicApi
public interface MailThreadManager
{
    public static final String NOTIFICATION_KEY = "NOTIFICATION_";

    public static final String NOTIFICATION_ISSUE_CREATED = NOTIFICATION_KEY + EventType.ISSUE_CREATED_ID;
    public static final String NOTIFICATION_ISSUE_UPDATED = NOTIFICATION_KEY + EventType.ISSUE_UPDATED_ID;
    public static final String NOTIFICATION_ISSUE_ASSIGNED = NOTIFICATION_KEY + EventType.ISSUE_ASSIGNED_ID;
    public static final String NOTIFICATION_ISSUE_RESOLVED = NOTIFICATION_KEY + EventType.ISSUE_RESOLVED_ID;
    public static final String NOTIFICATION_ISSUE_CLOSED = NOTIFICATION_KEY + EventType.ISSUE_CLOSED_ID;
    public static final String NOTIFICATION_ISSUE_COMMENTED = NOTIFICATION_KEY + EventType.ISSUE_COMMENTED_ID;
    public static final String NOTIFICATION_ISSUE_COMMENT_EDITED = NOTIFICATION_KEY + EventType.ISSUE_COMMENTED_ID;
    public static final String NOTIFICATION_ISSUE_REOPENED = NOTIFICATION_KEY + EventType.ISSUE_REOPENED_ID;
    public static final String NOTIFICATION_ISSUE_DELETED = NOTIFICATION_KEY + EventType.ISSUE_DELETED_ID;
    public static final String NOTIFICATION_ISSUE_WORKLOGGED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOGGED_ID;
    public static final String NOTIFICATION_ISSUE_WORKLOG_UPDATED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOG_UPDATED_ID;
    public static final String NOTIFICATION_ISSUE_WORKLOG_DELETED = NOTIFICATION_KEY + EventType.ISSUE_WORKLOG_DELETED_ID;   
    public static final String NOTIFICATION_ISSUE_MOVED = NOTIFICATION_KEY + EventType.ISSUE_MOVED_ID;
    public static final String NOTIFICATION_ISSUE_WORKSTARTED = NOTIFICATION_KEY + EventType.ISSUE_WORKSTARTED_ID;
    public static final String NOTIFICATION_ISSUE_WORKSTOPPED = NOTIFICATION_KEY + EventType.ISSUE_WORKSTOPPED_ID;
    public static final String NOTIFICATION_ISSUE_GENERICEVENT = NOTIFICATION_KEY + EventType.ISSUE_GENERICEVENT_ID;

    public static final String ISSUE_CREATED_FROM_EMAIL = "ISSUE_CREATED_FROM_EMAIL";
    public static final String ISSUE_COMMENTED_FROM_EMAIL = "ISSUE_COMMENTED_FROM_EMAIL";

    public void createMailThread(String type, Long source, String emailAddress, String messageId);

    public void threadNotificationEmail(Email email, Long issueId);

    public String getThreadType(Long eventTypeId);


    /**
     * Looks for an issue associated with given message. "In-Reply-To" header of the message is analysed for
     * the the original message id. Such message id has to be associted first with an issue by {@link #createMailThread(String, Long, String, String)}
     * method
     * @param message message to analyse
     * @return associated issue or null if no issue has been associated with this message.
     */
    @Nullable
    public Issue getAssociatedIssueObject(Message message);

    /**
     * Looks for an issue associated with given message. "In-Reply-To" header of the message is analysed for
     * the the original message id. Such message id has to be associted first with an issue by {@link #createMailThread(String, Long, String, String)}
     * method
     * @param message message to analyse
     * @return associated issue or null if no issue has been associated with this message.
     *
     * @deprecated use instead {@link #getAssociatedIssueObject} method.
     */
    @Deprecated
    public GenericValue getAssociatedIssue(Message message);

    public int removeAssociatedEntries(Long issueId);
}
