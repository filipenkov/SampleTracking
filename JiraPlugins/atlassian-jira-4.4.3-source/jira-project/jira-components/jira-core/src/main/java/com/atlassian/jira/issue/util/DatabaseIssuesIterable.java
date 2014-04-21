package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PagedDatabaseIterable;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;

/**
 * This {@link EnclosedIterable} is used to iterate over all issues in the database.
 * <p>
 * This implementation is superseded by the more flexible {@link DatabaseIterable} or 
 * {@link PagedDatabaseIterable}.
 */
public class DatabaseIssuesIterable implements IssuesIterable
{
    private final OfBizDelegator delegator;
    private final IssueFactory issueFactory;

    public DatabaseIssuesIterable(final OfBizDelegator delegator, final IssueFactory issueFactory)
    {
        if (delegator == null)
        {
            throw new NullPointerException(this.getClass().getName() + " needs a not null " + OfBizDelegator.class.getName() + " instance");
        }
        if (issueFactory == null)
        {
            throw new NullPointerException(this.getClass().getName() + " needs a not null " + IssueFactory.class.getName() + " instance");
        }

        this.delegator = delegator;
        this.issueFactory = issueFactory;
    }

    public void foreach(final Consumer<Issue> sink)
    {
        final DatabaseIssuesIterator iterator = new DatabaseIssuesIterator(delegator, issueFactory);
        try
        {
            CollectionUtil.foreach(iterator, sink);
        }
        finally
        {
            iterator.close();
        }
    }

    /**
     * You cannot rely on this size after you have started iterating through the issues
     */
    public int size()
    {
        return (int) delegator.getCount("Issue");
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + ": All issues in the database.";
    }
}