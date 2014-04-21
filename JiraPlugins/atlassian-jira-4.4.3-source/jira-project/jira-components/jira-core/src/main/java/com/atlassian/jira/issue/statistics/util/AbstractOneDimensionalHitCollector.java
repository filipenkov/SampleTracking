package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;

import java.io.IOException;
import java.util.Collection;

/**
 * Used to traverse a query and collect if the specified one dimension is relevant, this will keep track of the
 * irrelevant count.
 *
 * @since v4.0
 */
public abstract class AbstractOneDimensionalHitCollector extends HitCollector
{
    private final String fieldId;
    private Collection<String>[] docToTerms;
    private Collection<String>[] projectDocToTerms;
    private Collection<String>[] issueTypeDocToTerms;
    private final FieldVisibilityManager fieldVisibilityManager;
    private long irrelevantCount = 0;

    /**
     * Records the number of times the {@link #collect(int, float)} method was called. The method should be called once
     * for each issue.
     */
    private long hitCount = 0;

    public AbstractOneDimensionalHitCollector(final String fieldId, final IndexReader indexReader,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldId = new HitCollectorUtil().getFieldId(fieldId);
        this.docToTerms = readerCache.get(indexReader, fieldId, new Supplier<Collection<String>[]>()
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
        this.projectDocToTerms = readerCache.get(indexReader, SystemSearchConstants.forProject().getIndexField(), new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getMatches(indexReader, SystemSearchConstants.forProject().getIndexField());
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
        this.issueTypeDocToTerms = readerCache.get(indexReader, SystemSearchConstants.forIssueType().getIndexField(), new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getMatches(indexReader, SystemSearchConstants.forIssueType().getIndexField());
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }

    public void collect(int i, float v)
    {
        ++hitCount;
        Collection<String> terms = docToTerms[i];
        if (terms == null)
        {
            // We know there will always be a project and issue type for each issue
            final Long projectId = new Long(projectDocToTerms[i].iterator().next());
            final String issueTypeId = issueTypeDocToTerms[i].iterator().next();
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

    public long getIrrelevantCount()
    {
        return irrelevantCount;
    }

    /**
     * Returns the number of times the {@link #collect(int, float)} method was called. This should return the number of
     * unique issues that was matched during a search.
     *
     * @return number of times the {@link #collect(int, float)} method was called.
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

}
