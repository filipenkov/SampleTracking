package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Searchable;

import java.io.IOException;

public abstract class DocumentHitCollector extends HitCollector
{
    protected final Searchable searcher;

    protected DocumentHitCollector(Searchable searcher)
    {
        this.searcher = searcher;
    }

    public void collect(int i,float v)
    {
        try
        {
            Document d = searcher.doc(i);
            collect(d);
        }
        catch (IOException e)
        {
            //do nothing
        }
    }

    public abstract void collect(Document d);
}
