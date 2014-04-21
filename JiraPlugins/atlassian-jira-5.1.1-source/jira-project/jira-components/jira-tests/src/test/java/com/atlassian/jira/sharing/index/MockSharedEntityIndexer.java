/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.MockResult;

import java.util.Collection;

/**
 * @since v3.13
 */
public class MockSharedEntityIndexer implements SharedEntityIndexer
{
    public Index.Result index(final SharedEntity entity)
    {
        return new MockResult();
    }

    public Index.Result deIndex(final SharedEntity entity)
    {
        return new MockResult();
    }

    public <S extends SharedEntity> SharedEntitySearcher<S> getSearcher(final SharedEntity.TypeDescriptor<S> type)
    {
        return null;
    }

    public long optimize(final TypeDescriptor<?> type)
    {
        return 0;
    }

    public String clear(final TypeDescriptor<?> type)
    {
        return null;
    }

    public void shutdown(final TypeDescriptor<?> type)
    {}

    public Collection<String> getAllIndexPaths()
    {
        return null;
    }

    public void recreate(final TypeDescriptor<?> type)
    {}
}
