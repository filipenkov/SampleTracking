package com.atlassian.jira.issue.index;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.index.Configuration;
import com.atlassian.jira.index.DefaultConfiguration;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.index.Indexes;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Supplier;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Responsible for creating the {@link Directory directories} required for issue and comment indexing.
 *
 * @since v4.0
 */
public interface IndexDirectoryFactory extends Supplier<Map<IndexDirectoryFactory.Name, Index.Manager>>
{
    enum Mode
    {
        DIRECT
        {
            @Override
            Manager createIndexManager(final String name, final Configuration configuration)
            {
                return Indexes.createSimpleIndexManager(configuration);
            }
        },
        QUEUED
        {
            @Override
            Manager createIndexManager(final String name, final Configuration configuration)
            {
                return Indexes.createQueuedIndexManager(name, configuration);
            }
        };

        abstract Index.Manager createIndexManager(String name, Configuration configuration);
    }

    enum Name
    {
        COMMENT
        {
            @Override
            @NotNull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getCommentIndexPath());
            }
        },

        ISSUE
        {
            @Override
            @NotNull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getIssueIndexPath());
            }
        },

        CHANGE_HISTORY
        {
            @Override
            @NotNull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getChangeHistoryIndexPath());
            }
        };

        final @NotNull
        Directory directory(@NotNull final IndexPathManager indexPathManager)
        {
            LuceneDirectoryUtils luceneDirectoryUtils = ComponentAccessor.getComponent(LuceneDirectoryUtils.class);
            return luceneDirectoryUtils.getDirectory(new File(getPath(indexPathManager)));
        }

        final @NotNull
        String verify(final IndexPathManager indexPathManager, final String path) throws IllegalStateException
        {
            if (indexPathManager.getMode() == IndexPathManager.Mode.DISABLED)
            {
                throw new IllegalStateException("Indexing is disabled.");
            }
            return notNull("Index path is null: " + this, path);
        }

        abstract @NotNull
        String getPath(@NotNull IndexPathManager indexPathManager);
    }

    String getIndexRootPath();

    List<String> getIndexPaths();

    /**
     * Sets the Indexing Mode - one of either DIRECT or QUEUED.
     *
     * @param mode the indexing mode.
     */
    void setIndexingMode(@NotNull Mode mode);

    class IndexPathAdapter implements IndexDirectoryFactory
    {
        private final IndexPathManager indexPathManager;
        private final IndexWriterConfiguration writerConfiguration;
        private volatile Mode strategy = Mode.QUEUED;

        public IndexPathAdapter(final @NotNull IndexPathManager indexPathManager, final IndexWriterConfiguration writerConfiguration)
        {
            this.indexPathManager = notNull("indexPathManager", indexPathManager);
            this.writerConfiguration = notNull("writerConfiguration", writerConfiguration);
        }

        public Map<Name, Index.Manager> get()
        {
            final Mode strategy = this.strategy;
            final EnumMap<Name, Index.Manager> indexes = new EnumMap<Name, Index.Manager>(Name.class);
            for (final Name type : Name.values())
            {
                indexes.put(type, strategy.createIndexManager(type.name(), new DefaultConfiguration(type.directory(indexPathManager),
                    IssueIndexer.Analyzers.INDEXING, writerConfiguration)));
            }
            return Collections.unmodifiableMap(indexes);
        }

        public String getIndexRootPath()
        {
            return indexPathManager.getIndexRootPath();
        }

        public List<String> getIndexPaths()
        {
            final List<String> result = new ArrayList<String>(Name.values().length);
            for (final Name indexType : Name.values())
            {
                try
                {
                    result.add(indexType.getPath(indexPathManager));
                }
                catch (final RuntimeException ignore)
                {
                    //probable not setup
                }
            }
            return Collections.unmodifiableList(result);
        }

        public void setIndexingMode(final Mode strategy)
        {
            this.strategy = strategy;
        }
    }
}
