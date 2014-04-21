package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Searchable;

import java.util.Map;

/**
 * A HitCollector that accesses the document directly to get the values for a field.  This HitCollector
 * has low memory usage (it iterates over the documents as neccessary), and is useful when you are doing
 * a collection where there are a limited number of documents, but a large number of terms in the entire index.
 */
public class OneDimensionalDocHitCollector extends DocumentHitCollector
{
    private final String luceneGroupField;
    private final Map result;

    public OneDimensionalDocHitCollector(String luceneGroupField, Map result, Searchable searcher)
    {
        super(searcher);
        this.luceneGroupField = luceneGroupField;
        this.result = result;
    }

    public void collect(Document d)
    {
        String[] values = d.getValues(luceneGroupField);
        adjustMapForValues(result, values);
    }

    private void adjustMapForValues(Map map, String[] values)
    {
        if (values == null)
            return;

        for (int i = 0; i < values.length; i++)
        {
            String value = values[i];
            Integer count = (Integer) map.get(value);

            if (count == null)
                count = new Integer(0);

            map.put(value, new Integer(count.intValue() + 1));
        }
    }
}
