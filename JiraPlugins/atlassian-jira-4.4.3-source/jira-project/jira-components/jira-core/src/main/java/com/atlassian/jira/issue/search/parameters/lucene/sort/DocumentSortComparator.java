package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.jira.issue.search.LuceneFieldSorter;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Comparator;

public class DocumentSortComparator implements SortComparator
{
    private final LuceneFieldSorter sorter;

    public DocumentSortComparator(LuceneFieldSorter sorter)
    {
        this.sorter = sorter;
    }

    public int compare(IndexReader indexReader, int docId, int docId2)
    {
        final Comparator comparator = sorter.getComparator();

        Object value1 = getEarliestObjectFromDocument(docId, indexReader);
        Object value2 = getEarliestObjectFromDocument(docId2, indexReader);
        if (value1 == null && value2 == null)
        { // if they are both null, they are the same. Fixes JRA-7003
            return 0;
        }
        if (value1 == null)
        {
            return 1; // null is greater than any value (we want these at the end)
        }
        if (value2 == null)
        {
            return -1; // any value is less than null (we want null at the end)
        }
        return comparator.compare(value1, value2);
    }

    private Object getEarliestObjectFromDocument(int i, final IndexReader indexReader)
    {
        try
        {
            Object earliestValue = null;
            String[] values = indexReader.document(i).getValues(sorter.getDocumentConstant());
            for (int j = 0; j < values.length; j++)
            {
                String value = values[j];
                Object currentValue = sorter.getValueFromLuceneField(value);
                if (earliestValue == null || sorter.getComparator().compare(currentValue, earliestValue) < 1)
                {
                    earliestValue = currentValue;
                }

            }
            return earliestValue;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
