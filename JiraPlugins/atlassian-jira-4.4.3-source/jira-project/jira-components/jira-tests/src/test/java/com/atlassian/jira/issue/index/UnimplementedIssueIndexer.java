/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.collect.EnclosedIterable;

import org.apache.lucene.search.IndexSearcher;

import java.util.List;

public class UnimplementedIssueIndexer implements IssueIndexer
{
    public Result deindexIssues(final EnclosedIterable<Issue> issues, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result indexIssues(final EnclosedIterable<Issue> issues, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result indexIssuesBatchMode(final EnclosedIterable<Issue> issuesIterable, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result reindexIssues(final EnclosedIterable<Issue> issuesIterable, final Context event)
    {
        throw new UnsupportedOperationException();
    }

    public Result optimize()
    {
        throw new UnsupportedOperationException();
    }

    public void deleteIndexes()
    {
        throw new UnsupportedOperationException();
    }

    public void shutdown()
    {
        throw new UnsupportedOperationException();
    }

    public IndexSearcher getIssueSearcher()
    {
        throw new UnsupportedOperationException();
    }

    public IndexSearcher getCommentSearcher()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexSearcher getChangeHistorySearcher()
    {
         throw new UnsupportedOperationException();
    }

    public List<String> getIndexPaths()
    {
        throw new UnsupportedOperationException();
    }

    public void setIndexRootPath(final String path)
    {
        throw new UnsupportedOperationException();
    }

    public String getIndexRootPath()
    {
        throw new UnsupportedOperationException();
    }
}
