package com.atlassian.jira.index;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.config.util.IndexWriterConfiguration.WriterSettings;
import com.atlassian.jira.index.Index.UpdateMode;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;

public class TestDefaultConfiguration extends ListeningTestCase
{
    @Test
    public void testNullDirectory() throws Exception
    {
        try
        {
            new DefaultConfiguration(null, new StandardAnalyzer());
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullAnalyzer() throws Exception
    {
        try
        {
            new DefaultConfiguration(new RAMDirectory(), null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testDirectory() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer());
        assertSame(directory, configuration.getDirectory());
    }

    @Test
    public void testAnalyzer() throws Exception
    {
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), analyzer);
        assertSame(analyzer, configuration.getAnalyzer());
    }

    @Test
    public void testInteractiveDefaults() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final WriterSettings settings = configuration.getWriterSettings(UpdateMode.INTERACTIVE);
        assertEquals(4, settings.getMergeFactor());
        assertEquals(5000, settings.getMaxMergeDocs());
        assertEquals(300, settings.getMaxBufferedDocs());
        assertEquals(1000000, settings.getMaxFieldLength());
    }

    @Test
    public void testBatchDefaults() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final WriterSettings settings = configuration.getWriterSettings(UpdateMode.BATCH);
        assertEquals(50, settings.getMergeFactor());
        assertEquals(Integer.MAX_VALUE, settings.getMaxMergeDocs());
        assertEquals(300, settings.getMaxBufferedDocs());
        assertEquals(1000000, settings.getMaxFieldLength());
    }
}
