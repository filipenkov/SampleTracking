package com.atlassian.jira.config.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.NotNull;
import org.apache.lucene.index.IndexWriter;

import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Controls how the Lucene IndexWriter will be set up.
 *
 * @since v4.0
 */
public interface IndexWriterConfiguration
{
    static final class Default
    {
        // use the Lucene IndexWriter default for this, as the default inside ILuceneConnection.DEFAULT_CONFIGURATION is HUGE!!!
        static final int MAX_FIELD_LENGTH = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

        public static final WriterSettings BATCH = new WriterSettings()
        {
            public int getMergeFactor()
            {
                return 50;
            }

            public int getMaxMergeDocs()
            {
                return Integer.MAX_VALUE;
            }

            public int getMaxBufferedDocs()
            {
                return 300;
            }

            public int getMaxFieldLength()
            {
                return MAX_FIELD_LENGTH;
            }
        };

        public static final WriterSettings INTERACTIVE = new WriterSettings()
        {
            public int getMergeFactor()
            {
                return 4;
            }

            public int getMaxMergeDocs()
            {
                return 5000;
            }

            public int getMaxBufferedDocs()
            {
                return 300;
            }

            public int getMaxFieldLength()
            {
                return MAX_FIELD_LENGTH;
            }
        };
    }

    WriterSettings getInteractiveSettings();

    WriterSettings getBatchSettings();

    interface WriterSettings
    {
        int getMergeFactor();

        int getMaxMergeDocs();

        int getMaxFieldLength();

        int getMaxBufferedDocs();
    }

    public static class PropertiesAdaptor implements IndexWriterConfiguration
    {
        private final ApplicationProperties properties;

        public PropertiesAdaptor(final @NotNull ApplicationProperties properties)
        {
            this.properties = notNull("properties", properties);
        }

        private final WriterSettings batch = new WriterSettings()
        {
            public int getMaxBufferedDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MAX_BUFFERED_DOCS, Default.BATCH.getMaxBufferedDocs());
            }

            public int getMergeFactor()
            {

                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MERGE_FACTOR, Default.BATCH.getMergeFactor());
            }

            public int getMaxMergeDocs()
            {

                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MAX_MERGE_DOCS, Default.BATCH.getMaxMergeDocs());
            }

            public int getMaxFieldLength()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.MAX_FIELD_LENGTH, Default.INTERACTIVE.getMaxFieldLength());
            }
        };

        private final WriterSettings interactive = new WriterSettings()
        {
            public int getMergeFactor()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MERGE_FACTOR, Default.INTERACTIVE.getMergeFactor());
            }

            public int getMaxMergeDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MAX_MERGE_DOCS, Default.INTERACTIVE.getMaxMergeDocs());
            }

            public int getMaxBufferedDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MAX_BUFFERED_DOCS,
                    Default.INTERACTIVE.getMaxBufferedDocs());
            }

            public int getMaxFieldLength()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.MAX_FIELD_LENGTH, Default.INTERACTIVE.getMaxFieldLength());
            }

        };

        public WriterSettings getBatchSettings()
        {
            return batch;
        }

        public WriterSettings getInteractiveSettings()
        {
            return interactive;
        }
    }
}
