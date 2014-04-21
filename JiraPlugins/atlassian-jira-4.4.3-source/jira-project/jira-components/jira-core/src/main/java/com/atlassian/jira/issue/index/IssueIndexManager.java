/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import org.apache.lucene.search.IndexSearcher;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Manages Lucene search indexes.
 */
public interface IssueIndexManager extends IndexLifecycleManager
{
    /**
     * Reindex all issues.
     * 
     * @return Reindex time in ms.
     */
    long reIndexAll() throws IndexException;

    /**
     * Reindex an issue (eg. after field updates).
     */
    void reIndex(GenericValue issue) throws IndexException;

    /**
     * Reindex a list of issues, passing an optional event that will be set progress
     * 
     * @param issuesIterable IssuesIterable
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     */
    long reIndexIssues(IssuesIterable issuesIterable, Context context) throws IndexException;

    /**
     * Reindex an issue (eg. after field updates).
     */
    void reIndex(Issue issue) throws IndexException;

    /**
     * Remove an issue from the search index.
     */
    void deIndex(GenericValue issue) throws IndexException;

    /**
     * Reindex a set of issues (GenericValues). Use {@link #reIndexIssueObjects(Collection)} instead when possible.
     * 
     * @param issues The Issue {@link GenericValue}s to reindex.
     * @return Reindex time in ms.
     */
    long reIndexIssues(final Collection<GenericValue> issues) throws IndexException;

    /**
     * Reindex a set of issues.
     * 
     * @param issueObjects Set of {@link com.atlassian.jira.issue.Issue}s to reindex.
     * @return Reindex time in ms.
     */
    long reIndexIssueObjects(final Collection<? extends Issue> issueObjects) throws IndexException;

    /**
     * Get the root path of the index directory for plugins. Any plugin that keeps indexes should create its own sub-directory under this path and
     * create its indexes in its own sub-directory
     */
    String getPluginsRootPath();

    /**
     * Returns a collection of Strings, each one representing the absolute path to the actual <b>existing</b> directory where a plugin keeps its
     * indexes. Each directory in the collection should be a sub-directory under the plugin's index root path. See {@link #getPluginsRootPath()}.
     * <p>
     * If a plugin index root path does not exist, or is empty (no sub-directopries exist) then an empty collection will be returned.
     * </p>
     */
    Collection<String> getExistingPluginsPaths();

    /**
     * Get an {@link IndexSearcher} that can be used to search the issue index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#ISSUE_INDEX} as it is a managed searcher
     * and all the closing semantics are handled for you.
     */
    IndexSearcher getIssueSearcher();

    /**
     * Get an {@link IndexSearcher} that can be used to search the comment index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#COMMENT_INDEX} as it is a managed
     * searcher and all the closing semantics are handled for you.
     */
    IndexSearcher getCommentSearcher();


      /**
     * Get an {@link IndexSearcher} that can be used to search the change history index.
     * <p />
     * Note: This is an unmanaged IndexSearcher. You MUST call {@link IndexSearcher#close()} when you are done with it. Alternatively you should
     * really call {@link SearchProviderFactory#getSearcher(String))} passing in {@link SearchProviderFactory#CHANGE_HISTORY_INDEX} as it is a managed
     * searcher and all the closing semantics are handled for you.
     */
    IndexSearcher getChangeHistorySearcher();
}