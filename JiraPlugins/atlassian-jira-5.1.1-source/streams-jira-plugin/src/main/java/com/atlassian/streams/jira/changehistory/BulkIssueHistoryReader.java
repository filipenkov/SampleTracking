package com.atlassian.streams.jira.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Issue history reader that bulk-fetches change histories for all the specified issues in one shot. Compatible with
 * JIRA 5.1 and above.

 * @since v5.1.2
 */
class BulkIssueHistoryReader implements IssueHistoryReader
{
    /**
     * The JIRA ChangeHistoryManager implementation.
     */
    private final ChangeHistoryManager changeHistoryManager;

    /**
     * The method in ChangeHistoryManager that is used to read the histories. This needs to be done via reflection
     * because we use the same binary in both JIRA 5.0 and 5.1.
     */
    private final Method bulkReadMethod;

    /**
     * Creates a new BulkIssueHistoryReader backed by the given ChangeHistoryManager.
     *
     * @param changeHistoryManager a ChangeHistoryManager
     * @throws NoSuchMethodException if the currently running JIRA does not support bulk issue history reading
     */
    public BulkIssueHistoryReader(ChangeHistoryManager changeHistoryManager) throws NoSuchMethodException
    {
        this.changeHistoryManager = changeHistoryManager;
        bulkReadMethod = ChangeHistoryManager.class.getMethod("getChangeHistoriesForUser", Iterable.class, User.class);
    }

    @Override
    public Iterable<IssueHistory> getChangeHistoriesForUser(final Iterable<Issue> issues, final User remoteUser)
    {
        return new Iterable<IssueHistory>()
        {
            @Override
            public Iterator<IssueHistory> iterator()
            {
                try
                {
                    return new BulkIterator(checkNotNull(issues, "issues"), remoteUser);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Error creating BulkIterator", e);
                }
            }
        };
    }

    /**
     * Calls {@code getChangeHistoriesForUser} using reflection.
     */
    private Multimap<Issue, ChangeHistory> doGetChangeHistoriesForUser(Iterable<Issue> issues, User remoteUser)
            throws IllegalAccessException, InvocationTargetException
    {
        @SuppressWarnings ("unchecked")
        List<ChangeHistory> histories = (List<ChangeHistory>) bulkReadMethod.invoke(changeHistoryManager, issues, remoteUser);

        // index the change histories by issue
        return Multimaps.index(histories, new ByIssueIndexer());
    }

    /**
     * Iterates through the specified issues, fetching all change histories in one hit at construction time.
     */
    private class BulkIterator implements Iterator<IssueHistory>
    {
        private final Iterator<Issue> issues;
        private final Multimap<Issue, ChangeHistory> changeHistories;

        private BulkIterator(Iterable<Issue> issues, User remoteUser)
                throws InvocationTargetException, IllegalAccessException
        {
            this.changeHistories = doGetChangeHistoriesForUser(issues, remoteUser);
            this.issues = changeHistories.keySet().iterator();
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
            Collection<ChangeHistory> nextChangeHistories = changeHistories.get(nextIssue);

            return new IssueHistory(nextIssue, ImmutableList.copyOf(nextChangeHistories));
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class ByIssueIndexer implements Function<ChangeHistory, Issue>
    {
        @Override
        public Issue apply(ChangeHistory changeHistory)
        {
            return changeHistory != null ? changeHistory.getIssue() : null;
        }
    }
}
