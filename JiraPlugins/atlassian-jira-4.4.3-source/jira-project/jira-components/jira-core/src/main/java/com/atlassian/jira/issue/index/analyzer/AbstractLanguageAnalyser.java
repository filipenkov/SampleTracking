package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
abstract class AbstractLanguageAnalyser extends Analyzer
{
    private final boolean indexing; //or searching

    public AbstractLanguageAnalyser(boolean indexing)
    {
        this.indexing = indexing;
    }

    public boolean isIndexing()
    {
        return indexing;
    }

    protected TokenStream wrapStreamForIndexing(TokenStream result)
    {
        if (isIndexing())
        {
            return new SubtokenFilter(result);
        }
        else
        {
            return result;
        }
    }
}
