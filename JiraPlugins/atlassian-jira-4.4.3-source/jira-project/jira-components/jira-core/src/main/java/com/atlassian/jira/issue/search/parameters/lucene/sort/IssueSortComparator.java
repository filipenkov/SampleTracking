package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.search.IssueComparator;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

public class IssueSortComparator implements SortComparator
{
    private final IssueComparator issueComparator;
    private final IssueFactory issueFactory;

    public IssueSortComparator(IssueComparator issueComparator)
    {
        this(issueComparator, (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class));
    }

    public IssueSortComparator(IssueComparator issueComparator, IssueFactory issueFactory)
    {
        if (issueComparator == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires an instance of " + IssueComparator.class.getName());
        }
        this.issueComparator = issueComparator;

        if (issueFactory == null)
        {
            throw new NullPointerException(this.getClass().getName() + " requires an instance of " + IssueFactory.class.getName());
        }
        this.issueFactory = issueFactory;
    }

    public int compare(IndexReader indexReader, int docId, int docId2)
    {
        try
        {
            Issue issue1 = getIssueFromDocument(indexReader.document(docId));
            Issue issue2 = getIssueFromDocument(indexReader.document(docId2));
            if (issue1 == null && issue2 == null) // if they are both null, they are the same.  Fixes JRA-7003
            {
                return 0;
            }
            else if (issue1 == null)
            {
                return 1;  //null is greater than any value (we want null at the end)
            }
            else if (issue2 == null)
            {
                return -1; // any value is less than null (we want null at the end)
            }
            else
            {
                return issueComparator.compare(issue1, issue2);
            }
        }
        catch (IOException e)
        {
            return 0;
        }
    }

    private Issue getIssueFromDocument(Document document)
    {
        return issueFactory.getIssue(document);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final IssueSortComparator that = (IssueSortComparator) o;

        return (issueComparator != null ? issueComparator.equals(that.issueComparator) : that.issueComparator == null);

    }

    public int hashCode()
    {
        return (issueComparator != null ? issueComparator.hashCode() : 0);
    }

}
