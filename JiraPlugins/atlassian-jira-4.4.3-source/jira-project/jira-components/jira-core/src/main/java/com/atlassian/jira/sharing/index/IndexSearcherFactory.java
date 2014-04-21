package com.atlassian.jira.sharing.index;

import org.apache.lucene.search.Searcher;

/**
 * Responsible for getting a {@link Searcher}. These should be closed after use in a finally block.
 */
interface IndexSearcherFactory
{
    Searcher get();
}
