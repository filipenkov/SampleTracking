package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.GenericValue;

import java.util.NoSuchElementException;

/**
 * This iterator is not synchronized, and should not be accessed by multiple threads
 */
class DatabaseIssuesIterator implements IssueIterator
{
    private final OfBizDelegator delegator;
    private final IssueFactory issueFactory;
    private OfBizListIterator issuesIterator;
    private Issue nextIssue;

    DatabaseIssuesIterator(final OfBizDelegator delegator, final IssueFactory issueFactory)
    {
        this.delegator = delegator;
        this.issueFactory = issueFactory;
    }

    public boolean hasNext()
    {
        // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
        // if there are any results left in the iterator is to iterate over it until null is returned
        // (i.e. not use hasNext() method)
        // The documentation mentions efficiency only - but the functionality is totally broken when using
        // hsqldb JDBC drivers (hasNext() always returns true).
        // So listen to the OfBiz folk and iterate until null is returned.
        populateNextIssueIfNull();
        return nextIssue != null;
    }

    public Issue next()
    {
        populateNextIssueIfNull();
        if (nextIssue == null)
        {
            throw new NoSuchElementException();
        }

        final Issue issue = nextIssue;
        nextIssue = null;
        return issue;
    }

    private void populateNextIssueIfNull()
    {
        if (nextIssue == null)
        {
            pullNextIssue();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Cannot remove an issue from an Issue Iterator");
    }

    @Deprecated
    public Issue nextIssue()
    {
        return next();
    }

    public void close()
    {
        if (issuesIterator != null)
        {
            issuesIterator.close();
        }
    }

    public OfBizListIterator getIssuesIterator()
    {
        //todo - handle after being closed
        if (issuesIterator == null)
        {
            issuesIterator = delegator.findListIteratorByCondition("Issue", null);
        }

        return issuesIterator;
    }

    private void pullNextIssue()
    {
        final GenericValue issueGV = getIssuesIterator().next();
        if (issueGV == null)
        {
            return;
        }
        nextIssue = issueFactory.getIssue(issueGV);
    }
}
