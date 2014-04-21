package com.atlassian.jira.util.searchers;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * Handy methods for creating searcher instances in tests
 */
public class MockSearcherFactory
{
    public static Directory getCleanRAMDirectory()
    {
        try
        {
            final Directory directory = new RAMDirectory();
            new IndexWriter(directory, new StandardAnalyzer(), true).close();
            return directory;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static IndexSearcher getSearcher(final Directory directory)
    {
        try
        {
            return new IndexSearcher(directory);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static IndexSearcher getCleanSearcher()
    {
        return getSearcher(getCleanRAMDirectory());
    }
}
