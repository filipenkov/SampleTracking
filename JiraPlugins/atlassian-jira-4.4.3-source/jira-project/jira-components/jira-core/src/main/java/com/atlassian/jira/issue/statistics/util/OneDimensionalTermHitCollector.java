package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.index.IndexReader;

import java.util.Collection;
import java.util.Map;

/**
 * A HitCollector that creates a doc -> term mapping.  This is useful for collecting documents where there are a
 * limited number of terms.  The caching also ensures that if multiple searches sort on the same terms, the doc -> term
 * mapping is maintained.
 * <p>
 * This HitCollector can be quite memory intensive, however the cache is stored with a weak reference, so it will
 * be garbage collected. 
 */
public class OneDimensionalTermHitCollector extends AbstractOneDimensionalHitCollector
{
    private final Map<String,Integer> result;

    public OneDimensionalTermHitCollector(final String fieldId, final Map<String, Integer> result,
            final IndexReader indexReader, final FieldVisibilityManager fieldVisibilityManager,
            final ReaderCache readerCache)
    {
        super(fieldId, indexReader, fieldVisibilityManager, readerCache);
        this.result = result;
    }

    protected void collectIrrelevant(final int docId)
    {
        // Do nothing we just want the count
    }

    protected void collectWithTerms(final int docId, final Collection<String> terms)
    {
        if (terms == null)
        {
            incrementCount(null, result);
        }
        else
        {
            for (String term : terms)
            {
                incrementCount(term, result);
            }
        }
    }

    private void incrementCount(final String key, final Map<String, Integer> map)
    {
        Integer count = map.get(key);

        if (count == null)
        {
            count = 1;
        }
        else
        {
            count++;
        }
        map.put(key, count);
    }

}
