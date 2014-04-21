package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

import java.io.IOException;

/**
 * This Sort Comparator loops through each document, and calls {@link SortComparator#compare(org.apache.lucene.index.IndexReader,int,int)}
 * for each document it encounters.
 * <p/>
 * Whilst slower than the MappedSortComparator, it allows for less memory overhead, and also allows for comparisons
 * to be made
 */
public class DocumentSortComparatorSource implements SortComparatorSource
{
    private final SortComparator sortComparator;

    public DocumentSortComparatorSource(SortComparator sortComparator)
    {
        this.sortComparator = sortComparator;
    }

    public ScoreDocComparator newComparator(final IndexReader indexReader, String s) throws IOException
    {
        return new ScoreDocComparator()
        {
            public int compare(ScoreDoc i, ScoreDoc j)
            {
                //JRA-12665: Get the indexreader from the threadlocal rather than passing in the indexReader
                //    param.  This avoids leaking an indexReader into a value of the FieldSortedHitQueue.Comparators
                //    weakhashmap, that is also keyed by that indexReader (thus can never be GCed).
                return sortComparator.compare(ThreadLocalSearcherCache.getReader(), i.doc, j.doc);
            }

            public Comparable sortValue(ScoreDoc i)
            {
                return null; //we won't be able to pull the values from the sort
            }

            public int sortType()
            {
                return SortField.CUSTOM;
            }
        };
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

        final DocumentSortComparatorSource that = (DocumentSortComparatorSource) o;

        return (sortComparator != null ? sortComparator.equals(that.sortComparator) : that.sortComparator == null);

    }

    public int hashCode()
    {
        return (sortComparator != null ? sortComparator.hashCode() : 0);
    }
}
