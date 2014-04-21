package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractHitCollectorTestCase extends ListeningTestCase
{
    protected void index(Document doc, String fieldName, String fieldValue)
    {
        doc.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    protected IndexReader writeToRAM(Collection docs) throws IOException
    {
        RAMDirectory ramDirectory = new RAMDirectory();

        IndexWriterConfig conf = new IndexWriterConfig(DefaultIndexManager.LUCENE_VERSION, DefaultIndexManager.ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(ramDirectory, conf);

        for (Iterator iterator = docs.iterator(); iterator.hasNext();)
        {
            Document doc = (Document) iterator.next();
            writer.addDocument(doc);
        }
        writer.close();

        return IndexReader.open(ramDirectory);
    }
}
