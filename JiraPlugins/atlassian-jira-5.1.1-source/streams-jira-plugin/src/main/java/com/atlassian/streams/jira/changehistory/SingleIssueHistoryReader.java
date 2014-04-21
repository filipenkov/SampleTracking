package com.atlassian.streams.jira.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/**
 * Issue history reader that fetches change history for one issue at a time.
 *
 * @since v5.1.2
 */
class SingleIssueHistoryReader implements IssueHistoryReader
{
    /**
     * The ChangeHistoryManager.
     */
    private final ChangeHistoryManager changeHistoryManager;

    /**
     * Creates a new OneIssueAtATimeChangeHistoryReader that is backed by the given ChangeHistoryManager.
     *
     * @param changeHistoryManager a ChangeHistoryManager
     */
    public SingleIssueHistoryReader(ChangeHistoryManager changeHistoryManager)
    {
        this.changeHistoryManager = changeHistoryManager;
    }

    @Override
    public Iterable<IssueHistory> getChangeHistoriesForUser(final Iterable<Issue> issues, final com.atlassian.crowd.embedded.api.User remoteUser)
    {
        return new Iterable<IssueHistory>()
        {
            @Override
            public Iterator<IssueHistory> iterator()
            {
                return new SingleIssueIterator(issues.iterator(), remoteUser);
            }
        };
    }

    /**
     * Iterates through the given issues, fetching the respective change history as it iterates.
     */
    private class SingleIssueIterator implements Iterator<IssueHistory>
    {
        private final Iterator<Issue> issues;
        private final User remoteUser;

        public SingleIssueIterator(Iterator<Issue> issues, User remoteUser)
        {
            this.issues = issues;
            this.remoteUser = remoteUser;
        }

        @Override
        public boolean hasNext()
        {
            return issues.hasNext();
        }

        @Override
        public IssueHistory next()
        {
            Issue nextIssue = issues.next();
            List<ChangeHistory> nextChangeHistories = changeHistoryManager.getChangeHistoriesForUser(nextIssue, remoteUser);

            return new IssueHistory(nextIssue, ImmutableList.copyOf(nextChangeHistories));
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
