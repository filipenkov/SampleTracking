package com.atlassian.jira.index;

import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.NotNull;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.Collection;

/**
 * Partial interface of IndexWriter that contains only the methods we actually need.
 * <p>
 * Allows us to delegate much more easily and makes testing simpler. Also hides the
 * implementation details of IndexWriter interaction.
 */
interface Writer extends Closeable
{
    void addDocuments(@NotNull Collection<Document> document) throws IOException;

    void deleteDocuments(@NotNull Term identifyingTerm) throws IOException;

    void updateDocuments(@NotNull Term identifyingTerm, @NotNull Collection<Document> document) throws IOException;

    void optimize() throws IOException;

    void close();

    void commit();

    void setMode(final Index.UpdateMode mode);

    /**
     * Clears the write lock, should be used by special {@link OutOfMemoryError} handling code only.
     */
    void clearWriteLock();
}
