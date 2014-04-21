package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A HitCollector that creates a doc -> object mapping.  This is useful for collecting documents where there are a
 * limited number of terms.  The caching also ensures that if multiple searches sort on the same terms, the doc ->
 * object mapping is maintained.
 * <p/>
 * This HitCollector can be quite memory intensive, however the cache is stored with a weak reference, so it will be
 * garbage collected.
 * <p/>
 * This HitCollector differs from {@link OneDimensionalTermHitCollector} in that it performs the term -> object
 * conversion here, rather than later.  This is more expensive, but useful for StatisticsMappers that perform some sort
 * of runtime conversion / translation (eg a StatisticsMapper that groups dates by Month, or groups users by email
 * domain name).
 */
public class OneDimensionalObjectHitCollector extends HitCollector
{
    private StatisticsMapper statisticsMapper;
    private final Map<Object, Integer> result;
    private Collection<String>[] docToTerms;

    public OneDimensionalObjectHitCollector(StatisticsMapper statisticsMapper, Map result, IndexReader indexReader)
    {
        //noinspection unchecked
        this.result = result;
        this.statisticsMapper = statisticsMapper;
        try
        {
            docToTerms = JiraLuceneFieldFinder.getInstance().getMatches(indexReader, statisticsMapper.getDocumentConstant());
        }
        catch (IOException e)
        {
            //ignore
        }
    }

    public void collect(int i, float v)
    {
        adjustMapForValues(result, docToTerms[i]);
    }

    private void adjustMapForValues(Map<Object, Integer> map, Collection<String> terms)
    {
        if (terms == null)
        {
            return;
        }
        for (String term : terms)
        {
            Object object = statisticsMapper.getValueFromLuceneField(term);
            Integer count = map.get(object);

            if (count == null)
            {
                count = 0;
            }
            map.put(object, count + 1);
        }
    }

}
