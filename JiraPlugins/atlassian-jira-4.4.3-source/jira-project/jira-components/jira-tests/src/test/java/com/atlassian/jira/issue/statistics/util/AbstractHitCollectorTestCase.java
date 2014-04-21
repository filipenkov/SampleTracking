package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractHitCollectorTestCase extends ListeningTestCase
{
    protected void index(Document doc, String fieldName, String fieldValue)
    {
        doc.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.UN_TOKENIZED));
    }

    protected IndexReader writeToRAM(Collection docs) throws IOException
    {
        RAMDirectory ramDirectory = new RAMDirectory();

        IndexWriter writer = new IndexWriter(ramDirectory, DefaultIndexManager.ANALYZER_FOR_INDEXING, true);

        for (Iterator iterator = docs.iterator(); iterator.hasNext();)
        {
            Document doc = (Document) iterator.next();
            writer.addDocument(doc);
        }
        writer.close();

        return IndexReader.open(ramDirectory);
    }
}
