package com.atlassian.jira.issue.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.MutableIssue;

/**
 * Performs issue deletion.
 *
 * @since v4.1
 */
public interface IssueDeleteHelper
{
    /**
     * Delete <tt>issue</tt> in context of given <tt>user</tt>.
     *
     * @param user user performing the operation
     * @param issue issue to delete
     * @param eventDispatchOption event dispatching control
     * @param sendMail whether or not to send the email
     * @throws RemoveException if the removal fails
     */
    void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail) throws RemoveException;

    /**
     * Legacy delete issue
     *
     * @param user user
     * @param issue issue
     * @param eventDispatchOption event dispatch control
     * @param sendMail send email
     * @throws RemoveException if removal fails
     *
     * @deprecated use {@link #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)}
     * instead
     * @see #deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)
     */
    void deleteIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail) throws RemoveException;
}
