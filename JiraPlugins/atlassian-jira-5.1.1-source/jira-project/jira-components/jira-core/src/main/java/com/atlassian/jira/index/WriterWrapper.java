package com.atlassian.jira.index;

import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link Writer} implementation that actually writes to an {@link IndexWriter}.
 */
class WriterWrapper implements Writer
{
    private final IndexWriter writer;
    private final Configuration configuration;

    // for testing, can't make this accept an IndexWriter without making main constructor throw IOException
    WriterWrapper(final Supplier<IndexWriter> writerFactory, final @NotNull Configuration configuration, @NotNull final Index.UpdateMode mode)
    {
        this.configuration = notNull("configuration", configuration);
        writer = writerFactory.get();
    }

    WriterWrapper(final @NotNull Configuration configuration, final Index.UpdateMode mode)
    {
        this(new Supplier<IndexWriter>()
                {
                    public IndexWriter get()
                    {
                        try
                        {
                            IndexWriterConfiguration.WriterSettings writerSettings = configuration.getWriterSettings(mode);
                            IndexWriterConfig luceneConfig = writerSettings.getWriterConfiguration(configuration.getAnalyzer());
                            return new IndexWriter(configuration.getDirectory(), luceneConfig);
                        }
                        ///CLOVER:OFF
                        catch (final IOException e)
                        {
                            throw new RuntimeIOException(e);
                        }
                        ///CLOVER:ON
                    }
                }, configuration, mode);
    }

    public void addDocuments(@NotNull final Collection<Document> documents) throws IOException
    {
        for (final Document document : documents)
        {
            writer.addDocument(notNull("document", document));
        }
    }

    public void deleteDocuments(final @NotNull Term identifyingTerm) throws IOException
    {
        writer.deleteDocuments(notNull("identifyingTerm", identifyingTerm));
    }

    public void updateDocuments(final @NotNull Term identifyingTerm, final @NotNull Collection<Document> documents) throws IOException
    {
        if (documents.size() == 1)
        {
            writer.updateDocument(identifyingTerm, documents.iterator().next());
        }
        else
        {
            writer.deleteDocuments(identifyingTerm);
            for (final Document document : documents)
            {
                writer.addDocument(document);
            }
        }
    }

    public void optimize() throws IOException
    {
        writer.optimize();
    }

    public void commit()
    {
        try
        {
            writer.commit();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public void close()
    {
        try
        {
            writer.close();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }
}