package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.search.HitCollector;

/**
 * A HitCollector that iterates through the documents, and calculates a hash code.
 * <p>
 * This is useful if you wish to detemine if the results of a search have changed, but you don't
 * want to calculate the results each time.
 * 
 * @see com.atlassian.jira.issue.pager.PagerManager
 */
public class DocumentHashingHitCollector extends HitCollector
{
    private int hashCode = 1;

    public void collect(int doc, float score)
    {
        hashCode = hashCode * 31 + doc;
    }

    public int getHashCode()
    {
        return hashCode;
    }
}
