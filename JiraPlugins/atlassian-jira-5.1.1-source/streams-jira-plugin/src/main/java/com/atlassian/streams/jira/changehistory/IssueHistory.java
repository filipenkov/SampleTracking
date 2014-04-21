package com.atlassian.streams.jira.changehistory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.google.common.collect.ImmutableCollection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class holds an issue and its change history.
 */
public final class IssueHistory
{
    private final Issue issue;
    private final ImmutableCollection<ChangeHistory> changeHistories;

    IssueHistory(Issue issue, ImmutableCollection<ChangeHistory> changeHistories)
    {
        this.issue = checkNotNull(issue, "issue");
        this.changeHistories = checkNotNull(changeHistories, "histories");
    }

    /**
     * Returns the issue.
     *
     * @return an Issue
     */
    public Issue issue()
    {
        return issue;
    }

    /**
     * Returns all ChangeHistory items for {@code issue}.
     *
     * @return an immutable collection of ChangeHistory
     */
    public ImmutableCollection<ChangeHistory> changeHistories()
    {
        return changeHistories;
    }
}
