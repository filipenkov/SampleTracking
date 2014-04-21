package com.atlassian.crowd.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A batch mutation operation result representing the collection of entities
 * that were successfully processed and the collection of entities that failed
 * processing.
 */
public class BatchResult<T>
{
    private final List<T> successfulEntities;
    private final List<T> failedEntities;

    public BatchResult(int totalEntities)
    {
        successfulEntities = new ArrayList<T>(totalEntities);
        failedEntities = new ArrayList<T>();
    }

    public void addSuccess(T entity)
    {
        successfulEntities.add(entity);
    }

    public void addSuccesses(Collection<? extends T> entities)
    {
        successfulEntities.addAll(entities);
    }

    public void addFailure(T entity)
    {
        failedEntities.add(entity);
    }

    public void addFailures(Collection<? extends T> entities)
    {
        failedEntities.addAll(entities);
    }

    public boolean hasFailures()
    {
        return !failedEntities.isEmpty();
    }

    public int getTotalAttempted()
    {
        return successfulEntities.size() + failedEntities.size();
    }

    public List<T> getSuccessfulEntities()
    {
        return successfulEntities;
    }

    public List<T> getFailedEntities()
    {
        return failedEntities;
    }

    public int getTotalSuccessful()
    {
        return successfulEntities.size();
    }
}
