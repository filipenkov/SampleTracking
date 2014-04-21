package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.UpdateMode;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for implementing the policy to deal with {@link OutOfMemoryError}.
 * <p>
 * This is necessary to deal with https://issues.apache.org/jira/browse/LUCENE-1429
 */
class WriterOutOfMemory implements Writer
{
    interface Handler
    {
        void handle(OutOfMemoryError error);
    }

    static final Handler NULL_HANDLER = new Handler()
    {
        public void handle(final OutOfMemoryError error)
        {}
    };

    /**
     * The default handler, responsible for calling the inner handler and then cleaning up the write-lock.
     */
    final Handler handler = new Handler()
    {
        public void handle(final OutOfMemoryError error)
        {
            try
            {
                hitOOM.set(true);
                innerHandler.handle(error);
                throw error;
            }
            finally
            {
                writer.clearWriteLock();
            }
        }
    };

    private final Writer writer;
    private final Handler innerHandler;
    private final AtomicBoolean hitOOM = new AtomicBoolean();

    WriterOutOfMemory(final Writer writer, final Handler handler)
    {
        this.writer = writer;
        innerHandler = handler;
    }

    public void addDocuments(final Collection<Document> document) throws IOException
    {
        check();
        try
        {
            writer.addDocuments(document);
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    public void close()
    {
        check();
        try
        {
            writer.close();
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    public void deleteDocuments(final Term identifyingTerm) throws IOException
    {
        check();
        try
        {
            writer.deleteDocuments(identifyingTerm);
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    public void updateDocuments(final Term identifyingTerm, final Collection<Document> document) throws IOException
    {
        check();
        try
        {
            writer.updateDocuments(identifyingTerm, document);
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    public void commit()
    {
        check();
        try
        {
            writer.commit();
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    public void optimize() throws IOException
    {
        check();
        try
        {
            writer.optimize();
        }
        catch (final OutOfMemoryError e)
        {
            handler.handle(e);
        }
    }

    //
    // simple delegate methods
    //

    public void setMode(final UpdateMode mode)
    {
        writer.setMode(mode);
    }

    public void clearWriteLock()
    {
        writer.clearWriteLock();
    }

    private void check()
    {
        if (hitOOM.get())
        {
            throw new IllegalStateException("Cannot use this Writer after it has caught an OutOfMemoryError!");
        }
    }
}
