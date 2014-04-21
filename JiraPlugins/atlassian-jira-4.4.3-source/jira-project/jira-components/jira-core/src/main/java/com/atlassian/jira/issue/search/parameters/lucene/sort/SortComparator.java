package com.atlassian.jira.issue.search.parameters.lucene.sort;

import org.apache.lucene.index.IndexReader;

/**
 * A means of comparing two documents in an index, using Lucene.  Although implementations of this interface
 * are free to look up the issue from the database for sorting, this is an order of magnitude slower than
 * accessing the index directly.
 */
public interface SortComparator
{
    public int compare(IndexReader indexReader, int docId, int docId2);
}
