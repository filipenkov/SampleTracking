package com.atlassian.jira.issue.statistics.util;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Used to traverse a query and collect if the specified one dimension is relevant, this will keep track of the
 * irrelevant count.
 *
 * @since v4.0
 */
@Internal
public abstract class AbstractOneDimensionalHitCollector extends Collector
{
    private final String fieldId;
    private Collection<String>[] docToTerms;
    private Collection<String>[] docToProject;
    private Collection<String>[] docToIssueType;
    private final FieldVisibilityManager fieldVisibilityManager;
    private long irrelevantCount = 0;

    /**
     * Records the number of times the {@link #collect(int)} method was called. The method should be called once
     * for each issue.
     */
    private long hitCount = 0;
    private int docBase = 0;

    public AbstractOneDimensionalHitCollector(final String fieldId, final IndexReader indexReader,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldId = new HitCollectorUtil().getFieldId(fieldId);
        this.docToTerms = readCachedMultiValueField(indexReader, fieldId, readerCache);
        this.docToProject = readCachedSingleValueField(indexReader, SystemSearchConstants.forProject().getIndexField(), readerCache);
        this.docToIssueType = readCachedSingleValueField(indexReader, SystemSearchConstants.forIssueType().getIndexField(), readerCache);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException
    {
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException
    {
        this.docBase = docBase;
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    public void collect(int i)
    {
        ++hitCount;
        i += docBase;
        Collection<String> terms = docToTerms[i];
        if (terms == null)
        {
            // We know there will always be a project and issue type for each issue
            final Long projectId = new Long(getSingleValue(docToProject[i]));
            final String issueTypeId = getSingleValue(docToIssueType[i]);
            // Determine the I AM NOT SHOWN STATE and the NONE state
            if (fieldVisibilityManager.isFieldHidden(projectId, fieldId, issueTypeId))
            {
                irrelevantCount++;
                collectIrrelevant(i);
            }
            else
            {
                collectWithTerms(i, terms);
            }
        }
        else
        {
            collectWithTerms(i, terms);
        }
    }

    // We should always have a List, but handle it gracefully if somehow we don't.
    // The purpose of this is to avoid the GC churn of creating thousands of
    // ephemeral iterators when get(0) would work.
    private static String getSingleValue(Collection<String> source)
    {
        return (source instanceof List<?>) ? ((List<String>)source).get(0) : source.iterator().next();
    }

    public long getIrrelevantCount()
    {
        return irrelevantCount;
    }

    /**
     * Returns the number of times the {@link #collect(int)} method was called. This should return the number of
     * unique issues that was matched during a search.
     *
     * @return number of times the {@link #collect(int)} method was called.
     */
    public long getHitCount()
    {
        return hitCount;
    }

    /**
     * Implement this if you would like to do something when the hit collector has encountered a docId that contains an
     * irrelevant data match
     *
     * @param docId the match we have found
     */
    protected abstract void collectIrrelevant(final int docId);

    /**
     * Implement this if you would like to do something with the hit we have found.
     *
     * @param docId the doc id of the hit
     * @param terms the terms for the fieldId for this document, pre-calculated so you may not need to call getDocument
     */
    protected abstract void collectWithTerms(int docId, Collection<String> terms);


    private static Collection<String>[] readCachedMultiValueField(final IndexReader indexReader, final String fieldId, final ReaderCache readerCache)
    {
        return readerCache.get(indexReader, fieldId, new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getMatches(indexReader, fieldId);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }

    private static Collection<String>[] readCachedSingleValueField(final IndexReader indexReader, final String fieldId, final ReaderCache readerCache)
    {
        return readerCache.get(indexReader, fieldId, new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getUniqueMatches(indexReader, fieldId);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }
}
