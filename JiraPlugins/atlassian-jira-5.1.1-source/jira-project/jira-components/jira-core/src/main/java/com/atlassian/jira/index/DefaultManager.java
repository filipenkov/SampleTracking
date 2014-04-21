package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.RuntimeIOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

class DefaultManager implements Manager
{
    private final Configuration configuration;
    private final DefaultIndex.Engine actor;
    private final CloseableIndex index;

    DefaultManager(final @NotNull Configuration configuration, final @NotNull DefaultIndex.Engine actor, final @NotNull CloseableIndex index)
    {
        this.configuration = notNull("configuration", configuration);
        this.actor = notNull("actor", actor);
        this.index = notNull("index", index);
    }

    public Index getIndex()
    {
        return index;
    }

    public int getNumDocs()
    {
        return getSearcher().getIndexReader().numDocs();
    }

    public IndexSearcher getSearcher()
    {
        return actor.getSearcher();
    }

    public void deleteIndexDirectory()
    {
        actor.clean();
    }

    public void close()
    {
        index.close();
    }

    public boolean isIndexCreated()
    {
        try
        {
            return IndexReader.indexExists(configuration.getDirectory());
        }
        catch (final IOException e)
        {
            ///CLOVER:OFF
            throw new RuntimeIOException(e);
            ///CLOVER:ON
        }
    }
}
