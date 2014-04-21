package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.Index;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.EnclosedIterable;
import net.jcip.annotations.GuardedBy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;

import java.util.List;

public interface IssueIndexer
{
    public static class Analyzers
    {
        public static final Analyzer SEARCHING = JiraAnalyzer.ANALYZER_FOR_SEARCHING;
        public static final Analyzer INDEXING = JiraAnalyzer.ANALYZER_FOR_INDEXING;
    }

    /**
     * Add documents for the supplied issues.
     * 
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    Index.Result indexIssues(@NotNull EnclosedIterable<Issue> issues, @NotNull Context context);

    /**
     * Delete any existing documents for the supplied issues.
     * 
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    Index.Result deindexIssues(@NotNull EnclosedIterable<Issue> issues, @NotNull Context context);

    /**
     * Re-index the given issues, delete any existing documents and add new ones.
     * 
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    Index.Result reindexIssues(@NotNull EnclosedIterable<Issue> issues, @NotNull Context context);

    /**
     * Index the given issues, use whatever is in your arsenal to do it as FAST as possible.
     * 
     * @param issues An iterable of issues to index.
     * @param context for showing the user the current status.
     */
    @GuardedBy("external indexing lock")
    Index.Result indexIssuesBatchMode(@NotNull EnclosedIterable<Issue> issues, @NotNull Context context);

    @GuardedBy("external indexing lock")
    Index.Result optimize();

    // @TODO maybe return result?
    void deleteIndexes();

    void shutdown();

    IndexSearcher getIssueSearcher();

    IndexSearcher getCommentSearcher();

    IndexSearcher getChangeHistorySearcher();

    List<String> getIndexPaths();

    String getIndexRootPath();
}
