package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.DefaultConfiguration;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.index.Indexes;
import com.atlassian.jira.util.Function;
import com.google.common.collect.Lists;
import org.apache.lucene.store.Directory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MockIndexDirectoryFactory implements IndexDirectoryFactory
{
    private final Function<IndexDirectoryFactory.Name, Directory> directorySupplier;
    private String rootPath = "/some/stuff/";

    public MockIndexDirectoryFactory(final Function<Name, Directory> directorySupplier)
    {
        this.directorySupplier = directorySupplier;
    }

    public Map<Name, Manager> get()
    {
        final EnumMap<Name, Index.Manager> indexes = new EnumMap<Name, Index.Manager>(Name.class);
        for (final Name type : Name.values())
        {
            indexes.put(type, Indexes.createQueuedIndexManager(type.name(), new DefaultConfiguration(directorySupplier.get(type),
                IssueIndexer.Analyzers.INDEXING)));
        }
        return Collections.unmodifiableMap(indexes);
    }

    public List<String> getIndexPaths()
    {
        return Lists.newArrayList(rootPath + "issues", rootPath + "comments");
    }

    public String getIndexRootPath()
    {
        return rootPath;
    }

    public void setIndexRootPath(final String rootPath)
    {
        this.rootPath = rootPath;
    }

    public void setIndexingMode(final Mode mode)
    {}
}
