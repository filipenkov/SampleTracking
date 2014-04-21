package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.search.LuceneFieldSorter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

import java.io.IOException;
import java.util.Comparator;

/**
 * This Sort Comparator reads through the terms dictionary in lucene, and builds up a list of ordered terms.  It then
 * sorts the documents according to the order that they appear in the terms list.
 * <p/>
 * This approach, whilst very fast, does load the entire term dictionary into memory.  This could be a problem where there
 * are a large number of terms (eg. text fields).
 */
public class MappedSortComparator implements SortComparatorSource
{
    private final LuceneFieldSorter sorter;

    public MappedSortComparator(LuceneFieldSorter sorter)
    {
        this.sorter = sorter;
    }

    public ScoreDocComparator newComparator(final IndexReader reader, final String fieldname) throws IOException
    {
        return getComparatorUsingTerms(fieldname, reader);
    }

    private ScoreDocComparator getComparatorUsingTerms(final String fieldname, final IndexReader reader) throws IOException
    {
        final String field = fieldname.intern();
        final Object[] cachedValues = getLuceneValues(field, reader);
        final Comparator comparator = sorter.getComparator();

        return new ScoreDocComparator()
        {
            public int compare(ScoreDoc i, ScoreDoc j)
            {
                Object value1 = cachedValues[i.doc];
                Object value2 = cachedValues[j.doc];
                if (value1 == null && value2 == null) // if they are both null, they are the same.  Fixes JRA-7003
                {
                    return 0;
                }
                else if (value1 == null)
                {
                    return 1;  //null is greater than any value (we want these at the end)
                }
                else if (value2 == null)
                {
                    return -1; // any value is less than null (we want null at the end)
                }
                else
                {
                    //noinspection unchecked
                    return comparator.compare(value1, value2);
                }
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

    /**
     * This makes a call into the JiraLuceneFieldFinder to retrieve values from the Lucence
     * index.  It returns an array that is the same size as the number of documents in the reader
     * and will have all null values if the field is not present, otherwise it has the values
     * of the field within the document.
     * <p/>
     * Broken out as package level for unit testing reasons.
     *
     * @param field  the name of the field to find
     * @param reader the Lucence index reader
     * @return an non null array of values, which may contain null values.
     * @throws IOException if stuff goes wrong
     */
    Object[] getLuceneValues(final String field, final IndexReader reader) throws IOException
    {
        return JiraLuceneFieldFinder.getInstance().getCustom(reader, field, MappedSortComparator.this);
    }

    /**
     * Returns an object which, when sorted according by the comparator returned from  {@link LuceneFieldSorter#getComparator()} ,
     * will order the Term values in the correct order.
     * <p>For example, if the Terms contained integer values, this method
     * would return <code>new Integer(termtext)</code>.  Note that this
     * might not always be the most efficient implementation - for this
     * particular example, a better implementation might be to make a
     * ScoreDocLookupComparator that uses an internal lookup table of int.
     *
     * @param termtext The textual value of the term.
     * @return An object representing <code>termtext</code> that can be sorted by {@link LuceneFieldSorter#getComparator()}
     * @see Comparable
     * @see ScoreDocComparator
     */
    public Object getComparable(String termtext)
    {
        return sorter.getValueFromLuceneField(termtext);
    }

    public Comparator getComparator()
    {
        return sorter.getComparator();
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

        final MappedSortComparator that = (MappedSortComparator) o;

        return (sorter == null ? that.sorter == null : sorter.equals(that.sorter));
    }

    public int hashCode()
    {
        return (sorter != null ? sorter.hashCode() : 0);
    }
}
