package com.atlassian.streams.jira.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

/**
 * This is an abstraction over JIRA's {@link com.atlassian.jira.issue.changehistory.ChangeHistoryManager
 * ChangeHistoryManager}. Clients of this interface are shielded from different implementations of {@code
 * ChangeHistoryManager} that are used in different JIRA versions.
 *
 * @since v5.1.2
 */
public interface IssueHistoryReader
{
    /**
     * Returns IssueHistory items for multiple issues. Depending on the underlying version of JIRA, this method either
     * fetches change histories one issue at a time as they are read or using a bulk fetch strategy (for JIRA 5.1 and
     * later only).
     *
     * @param issues the issues.
     * @param remoteUser the user who is asking.
     * @return a List of ChangeHistory entries.
     * @since 5.1
     */
    Iterable<IssueHistory> getChangeHistoriesForUser(Iterable<Issue> issues, User remoteUser);
}
