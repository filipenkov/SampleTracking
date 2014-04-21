package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Searchable;

import java.io.IOException;

public abstract class FieldableDocumentHitCollector extends HitCollector
{
    protected final Searchable searcher;

    protected FieldableDocumentHitCollector(Searchable searcher)
    {
        this.searcher = searcher;
    }

    public void collect(int i, float v)
    {
        try
        {
            final Document d = searcher.doc(i, getFieldSelector());
            collect(d);
        }
        catch (IOException e)
        {
            //do nothing
        }
    }

    /**
     * This method should cache up the FieldSelector, such that overhead will be as small as possible when
     * this method is called from the {@link #collect(int,float)} method.
     *
     * @return a {@link org.apache.lucene.document.FieldSelector}
     */
    protected abstract FieldSelector getFieldSelector();

    public abstract void collect(Document d);
}
