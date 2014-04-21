package com.atlassian.jira.sharing.index;

import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.sharing.SharedEntity;
import org.apache.lucene.store.Directory;

import static com.atlassian.jira.util.LuceneUtils.getDirectory;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation that uses the {@link com.atlassian.jira.config.util.IndexPathManager} as its way of getting a
 * Directory
 */
public class IndexPathDirectoryFactory implements DirectoryFactory
{
    private final IndexPathManager pathManager;

    public IndexPathDirectoryFactory(final IndexPathManager pathManager)
    {
        this.pathManager = notNull("path", pathManager);
    }

    public Directory get(final SharedEntity.TypeDescriptor<?> type)
    {
        return getDirectory(getIndexPath(type));
    }

    String getIndexPath(final SharedEntity.TypeDescriptor<?> type)
    {
        return pathManager.getSharedEntityIndexPath() + "/" + type.getName().toLowerCase();
    }
}
