package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;

import java.io.IOException;
import java.util.Collection;

/**
 * A HitCollector that creates a doc -> term mapping.  This is useful for collecting documents where there are a
 * limited number of terms.  The caching also ensures that if multiple searches sort on the same terms, the doc -> term
 * mapping is maintained.
 * <p/>
 * This HitCollector can be quite memory intensive, however the cache is stored with a weak reference, so it will
 * be garbage collected.
 *
 * @since v3.11
 */
public class TwoDimensionalTermHitCollector extends HitCollector
{
    private final TwoDimensionalStatsMap statsMap;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ReaderCache readerCache;
    private final LuceneFieldSorter aggregateField;
    private final Collection[] docToXTerms;
    private final Collection[] docToYTerms;
    private final Collection[] docToValueTerms;
    private Collection<String>[] projectDocToTerms;
    private Collection<String>[] issueTypeDocToTerms;
    private final String xFieldName;
    private final String yFieldName;


    public TwoDimensionalTermHitCollector(final TwoDimensionalStatsMap statsMap, final IndexReader indexReader,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache)
    {
        this(statsMap, indexReader, fieldVisibilityManager, readerCache, null);
    }

    /**
     * Update a statsMap, using the values from the <code>aggregateField</code>.  Example, you can sum the votes.
     *
     * @param statsMap       stats map
     * @param indexReader    index reader
     * @param fieldVisibilityManager used to determine if the stat fields are visible
     * @param readerCache used to cache stats values at the reader level
     * @param aggregateField lucene field sorter
     */
    public TwoDimensionalTermHitCollector(final TwoDimensionalStatsMap statsMap, final IndexReader indexReader,
            final FieldVisibilityManager fieldVisibilityManager,final ReaderCache readerCache,
            final LuceneFieldSorter aggregateField)
    {
        this.statsMap = statsMap;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.readerCache = readerCache;
        this.aggregateField = aggregateField;
        this.docToXTerms = getDocToXTerms(indexReader, statsMap);
        this.docToYTerms = getDocToYTerms(indexReader, statsMap);

        final HitCollectorUtil hitCollectorUtil = new HitCollectorUtil();
        this.xFieldName = hitCollectorUtil.getFieldId(statsMap.getxAxisMapper().getDocumentConstant());
        this.yFieldName = hitCollectorUtil.getFieldId(statsMap.getyAxisMapper().getDocumentConstant());

        this.docToValueTerms = getDocToValueTerms(aggregateField, indexReader);
        this.projectDocToTerms = getDocToValueTerms(SystemSearchConstants.forProject().getIndexField(), indexReader);
        this.issueTypeDocToTerms = getDocToValueTerms(SystemSearchConstants.forIssueType().getIndexField(), indexReader);
    }

    public void collect(int i, float v)
    {
        adjustForValues(i);
    }

    private void adjustForValues(int docId)
    {
        final Collection xValues = docToXTerms[docId];
        final Collection yValues = docToYTerms[docId];
        int incrementValue = (docToValueTerms == null) ? 1 : getFieldValue(docId);
        boolean xFieldNotVisible = false;
        boolean yFieldNotVisible = false;
        // We know there will always be a project and issue type for each issue
        final Long projectId = new Long(projectDocToTerms[docId].iterator().next());
        final String issueTypeId = issueTypeDocToTerms[docId].iterator().next();
        if (xValues == null)
        {
            // Need to check to see if this is because the field is not visible or if it is because we have not value
            if (fieldVisibilityManager.isFieldHidden(projectId, xFieldName, issueTypeId))
            {
                xFieldNotVisible = true;
            }
        }
        if (yValues == null)
        {
            // Need to check to see if this is because the field is not visible or if it is because we have not value
            if (fieldVisibilityManager.isFieldHidden(projectId, yFieldName, issueTypeId))
            {
                yFieldNotVisible = true;
            }
        }
        if (xFieldNotVisible || yFieldNotVisible)
        {
            statsMap.adjustMapForIrrelevantValues(xValues, xFieldNotVisible ,yValues, yFieldNotVisible, incrementValue);
        }
        else
        {
            statsMap.adjustMapForValues(xValues, yValues, incrementValue);
        }
    }

    private int getFieldValue(int i)
    {
        final Number value = getValue(docToValueTerms[i]);
        return value != null ? value.intValue() : 0;
    }

    private Number getValue(Collection values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        else if (values.size() > 1)
        {
            throw new IllegalArgumentException("More than one value stored for statistic \"" + values.toString() + "\".");
        }
        else
        {
            Object o = aggregateField.getValueFromLuceneField((String) values.iterator().next());

            if (o == null)
                return 0;

            if (o instanceof Number)
            {
                return (Number) o;
            }
            else
            {
                throw new IllegalArgumentException("Value stored for statistic was \"" + (o != null ? o.getClass().getName() : "null") + "\".  Expected \"java.lang.Number\"");
            }
        }
    }

    private Collection[] getDocToXTerms(IndexReader indexReader, TwoDimensionalStatsMap statsMap)
    {
        return getDocToValueTerms(statsMap.getxAxisMapper().getDocumentConstant(), indexReader);
    }

    private Collection[] getDocToYTerms(IndexReader indexReader, TwoDimensionalStatsMap statsMap)
    {
        return getDocToValueTerms(statsMap.getyAxisMapper().getDocumentConstant(), indexReader);
    }

    private Collection[] getDocToValueTerms(LuceneFieldSorter aggregateField, IndexReader indexReader)
    {
        if (aggregateField != null)
        {
            return getDocToValueTerms(aggregateField.getDocumentConstant(), indexReader);
        }
        return null;
    }

    private Collection<String>[] getDocToValueTerms(final String documentConstant, final IndexReader indexReader)
    {
        return readerCache.get(indexReader, documentConstant, new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getMatches(indexReader, documentConstant);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }

}