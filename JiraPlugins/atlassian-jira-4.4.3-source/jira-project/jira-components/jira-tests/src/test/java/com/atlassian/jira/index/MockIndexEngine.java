package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.Operation;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

///CLOVER:OFF
class MockIndexEngine implements DefaultIndex.Engine
{
    public void clean()
    {
        throw new UnsupportedOperationException();
    }

    public void close()
    {
        throw new UnsupportedOperationException();
    }

    public IndexSearcher getSearcher()
    {
        throw new UnsupportedOperationException();
    }

    public void write(final Operation operation) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
///CLOVER:ON

